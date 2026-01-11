package com.example.timetrace.ui.viewmodel

import android.app.Application
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timetrace.data.model.Schedule
import com.example.timetrace.data.repository.MainRepository
import com.example.timetrace.ui.widget.GlanceScheduleWidget
import com.github.a6tail.lunar.Lunar
import com.github.a6tail.lunar.Solar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: MainRepository,
    private val application: Application
) : ViewModel() {

    val schedules: StateFlow<List<Schedule>> = repository.getAllSchedules()
        .map { schedules ->
            val today = Calendar.getInstance()
            val currentYear = today.get(Calendar.YEAR)
            val processedSchedules = mutableListOf<Schedule>()

            schedules.forEach { schedule ->
                if (!schedule.isBirthday) {
                    processedSchedules.add(schedule)
                } else {
                    // It's a birthday, calculate this year's occurrence
                    val birthdayCalendar = Calendar.getInstance().apply { timeInMillis = schedule.timestamp }

                    val newTimestamp = if (schedule.isLunar) {
                        val lunar = Lunar.fromDate(birthdayCalendar.time)
                        val currentYearLunarBirthday = Lunar.fromYmd(currentYear, lunar.month, lunar.day)
                        currentYearLunarBirthday.solar.calendar.timeInMillis
                    } else {
                        val solar = Solar.fromDate(birthdayCalendar.time)
                        val currentYearSolarBirthday = Solar.fromYmd(currentYear, solar.month, solar.day)
                        currentYearSolarBirthday.calendar.timeInMillis
                    }

                    // Only add if the birthday hasn't passed for this year
                    if (newTimestamp >= today.timeInMillis) {
                         processedSchedules.add(schedule.copy(timestamp = newTimestamp))
                    }
                }
            }
            processedSchedules.sortedBy { it.timestamp }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addSchedule(title: String, timestamp: Long, priority: Int, isLunar: Boolean, isBirthday: Boolean) {
        viewModelScope.launch {
            repository.insertSchedule(
                Schedule(
                    title = title,
                    timestamp = timestamp,
                    priority = priority,
                    isLunar = isLunar,
                    isBirthday = isBirthday
                )
            )
            updateWidget()
        }
    }

    fun toggleScheduleCompletion(schedule: Schedule) {
        viewModelScope.launch {
            // Prevent toggling completion for generated birthday instances
            if (repository.getScheduleById(schedule.id) != null) {
                 val updatedSchedule = schedule.copy(isCompleted = !schedule.isCompleted)
                 repository.updateSchedule(updatedSchedule)
                 updateWidget()
            }
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            // Prevent deleting generated birthday instances, delete original instead
            repository.getScheduleById(schedule.id)?.let { originalSchedule ->
                repository.deleteSchedule(originalSchedule)
                updateWidget()
            }
        }
    }

    private suspend fun updateWidget() {
        GlanceScheduleWidget().updateAll(application)
    }
}
