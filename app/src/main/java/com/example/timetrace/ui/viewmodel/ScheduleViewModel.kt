package com.example.timetrace.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timetrace.data.model.Schedule
import com.example.timetrace.data.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

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

    val schedulesByDate: StateFlow<Map<LocalDate, List<Schedule>>> = allSchedules
        .map {
            it.filter { !it.isBirthday && !it.isCompleted } // Filter out birthdays and completed
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
        .map { it.filter { it.isCompleted } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val birthdays: StateFlow<List<Schedule>> = allSchedules
        .map { it.filter { it.isBirthday } }
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
