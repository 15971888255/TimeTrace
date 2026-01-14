package com.example.timetrace.ui.viewmodel

import android.app.Application
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timetrace.data.model.Schedule
import com.example.timetrace.data.repository.MainRepository
import com.example.timetrace.ui.widget.GlanceScheduleWidget
import com.nlf.calendar.Lunar
import com.nlf.calendar.Solar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

// Helper extension function to convert Solar to Calendar
fun Solar.toCalendar(): Calendar {
    val cal = Calendar.getInstance()
    // Note: Solar library's month is 1-12, while Java Calendar is 0-11
    cal.set(this.year, this.month - 1, this.day, this.hour, this.minute, this.second)
    cal.set(Calendar.MILLISECOND, 0)
    return cal
}

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
                    val birthdayCalendar = Calendar.getInstance().apply { timeInMillis = schedule.timestamp }

                    val newTimestamp = if (schedule.isLunar) {
                        val originalLunar = Lunar.fromDate(birthdayCalendar.time)
                        val currentYearSolar = Lunar.fromYmd(currentYear, originalLunar.month, originalLunar.day).solar
                        currentYearSolar.toCalendar().timeInMillis
                    } else {
                        val originalSolar = Solar.fromDate(birthdayCalendar.time)
                        val currentYearSolar = Solar.fromYmd(currentYear, originalSolar.month, originalSolar.day)
                        currentYearSolar.toCalendar().timeInMillis
                    }
                    
                    if (newTimestamp.compareTo(today.timeInMillis) >= 0) {
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
            repository.getScheduleById(schedule.id)?.let { originalSchedule ->
                 val updatedSchedule = originalSchedule.copy(isCompleted = !originalSchedule.isCompleted)
                 repository.updateSchedule(updatedSchedule)
                 updateWidget()
            }
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
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
