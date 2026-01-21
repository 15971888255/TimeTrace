package com.example.timetrace.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timetrace.data.model.Schedule
import com.example.timetrace.data.repository.MainRepository
import com.nlf.calendar.Lunar
import com.nlf.calendar.Solar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import javax.inject.Inject

fun Solar.toCalendar(): Calendar {
    val cal = Calendar.getInstance()
    // 关键：Solar 库的月份是 1-12，而 Java Calendar 是 0-11，需要减 1
    cal.set(this.year, this.month - 1, this.day, 0, 0, 0)
    cal.set(Calendar.MILLISECOND, 0) // 清除毫秒，避免不必要的精度问题
    return cal
}

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {

    val allSchedules: StateFlow<List<Schedule>> = repository.getAllSchedules()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun generateBirthdayInstances(schedules: List<Schedule>): List<Schedule> {
        val birthdayInstances = mutableListOf<Schedule>()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val yearsToGenerate = listOf(currentYear, currentYear + 1)

        schedules.filter { it.isBirthday }.forEach { birthday ->
            val birthdayCalendar = Calendar.getInstance().apply { timeInMillis = birthday.timestamp }

            yearsToGenerate.forEach { year ->
                val currentYearSolar = if (birthday.isLunar) {
                    val originalLunar = Lunar.fromDate(birthdayCalendar.time)
                    val lunarMonth = originalLunar.month
                    val lunarDay = originalLunar.day
                    Lunar.fromYmd(year, lunarMonth, lunarDay).solar
                } else {
                    val originalSolar = Solar.fromDate(birthdayCalendar.time)
                    Solar.fromYmd(year, originalSolar.month, originalSolar.day)
                }

                val finalCalendar = currentYearSolar.toCalendar()
                val newTimestamp = finalCalendar.timeInMillis

                if (newTimestamp >= System.currentTimeMillis()) {
                    birthdayInstances.add(
                        birthday.copy(
                            id = (birthday.id * 10000 + year).toLong(),
                            timestamp = newTimestamp,
                            isCompleted = false
                        )
                    )
                }
            }
        }
        return birthdayInstances
    }

    val schedulesByDate: StateFlow<Map<LocalDate, List<Schedule>>> = allSchedules
        .map {
            val generatedBirthdays = generateBirthdayInstances(it)
            val allActiveSchedules = it.filter { !it.isBirthday && !it.isCompleted } + generatedBirthdays

            allActiveSchedules
                .sortedBy { it.timestamp }
                .groupBy { schedule ->
                    Instant.ofEpochMilli(schedule.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val completedSchedules: StateFlow<List<Schedule>> = allSchedules
        .map { scheduleList -> scheduleList.filter { it.isCompleted } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    fun addSchedule(title: String, timestamp: Long, priority: Int, notes: String?, isLunar: Boolean, isBirthday: Boolean) {
        viewModelScope.launch {
            repository.insertSchedule(Schedule(title = title, timestamp = timestamp, priority = priority, notes = notes, isLunar = isLunar, isBirthday = isBirthday))
        }
    }

    fun toggleScheduleCompletion(schedule: Schedule) {
        viewModelScope.launch {
            repository.updateSchedule(schedule.copy(isCompleted = !schedule.isCompleted))
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }
}
