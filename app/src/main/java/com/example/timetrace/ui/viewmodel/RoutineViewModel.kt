package com.example.timetrace.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timetrace.data.model.Routine
import com.example.timetrace.data.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {

    val routines: StateFlow<List<Routine>> = repository.getAllRoutines()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addRoutine(title: String, weekdays: List<Int>, hour: Int, minute: Int) {
        viewModelScope.launch {
            if (title.isNotBlank() && weekdays.isNotEmpty()) {
                val newRoutine = Routine(title = title, weekdays = weekdays, hour = hour, minute = minute)
                repository.addRoutineAndGenerateSchedules(newRoutine)
            }
        }
    }

    fun deleteRoutine(routine: Routine) {
        viewModelScope.launch {
            repository.deleteRoutine(routine)
        }
    }
}
