package com.example.timetrace.data.repository

import android.content.Context
import androidx.room.Transaction
import com.example.timetrace.data.dao.RoutineDao
import com.example.timetrace.data.local.dao.DiaryDao
import com.example.timetrace.data.local.dao.ScheduleDao
import com.example.timetrace.data.model.Diary
import com.example.timetrace.data.model.Routine
import com.example.timetrace.data.model.Schedule
import com.example.timetrace.ui.widget.updateAllWidgets
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    private val scheduleDao: ScheduleDao,
    private val diaryDao: DiaryDao,
    private val routineDao: RoutineDao,
    private val externalScope: CoroutineScope, // Injected global scope
    @ApplicationContext private val context: Context // Injected context
) {

    private var widgetUpdateJob: Job? = null

    // ... (existing schedule and diary methods)
    fun getAllSchedules(): Flow<List<Schedule>> = scheduleDao.getAllSchedules()

    fun getUpcomingSchedules(): Flow<List<Schedule>> = scheduleDao.getUpcomingSchedules(System.currentTimeMillis())

    fun getAllBirthdays(): Flow<List<Schedule>> = scheduleDao.getAllBirthdays()

    suspend fun getScheduleById(id: Long): Schedule? = scheduleDao.getScheduleById(id)

    suspend fun insertSchedule(schedule: Schedule) {
        scheduleDao.insert(schedule)
        triggerWidgetUpdate()
    }

    suspend fun updateSchedule(schedule: Schedule) {
        scheduleDao.update(schedule)
        triggerWidgetUpdate()
    }

    suspend fun deleteSchedule(schedule: Schedule) {
        scheduleDao.delete(schedule)
        triggerWidgetUpdate()
    }

    suspend fun getDiaryByDate(date: Long): Diary? = diaryDao.getDiaryByDate(date)

    suspend fun insertDiary(diary: Diary) {
        diaryDao.insert(diary)
    }

    suspend fun updateDiary(diary: Diary) {
        diaryDao.update(diary)
    }

    suspend fun deleteDiary(diary: Diary) {
        diaryDao.delete(diary)
    }


    // Routine methods
    fun getAllRoutines(): Flow<List<Routine>> = routineDao.getAllRoutines()

    @Transaction
    suspend fun addRoutineAndGenerateSchedules(routine: Routine) {
        val routineId = routineDao.insertRoutine(routine).toInt()

        val calendar = Calendar.getInstance()
        // Generate schedules for the next month
        for (i in 0 until 30) {
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            
            // Convert Calendar's DayOfWeek (Sun=1) to our format (Mon=1)
            val convertedDayOfWeek = if (dayOfWeek == 1) 7 else dayOfWeek - 1 

            if (routine.weekdays.contains(convertedDayOfWeek)) {
                val scheduleTime = Calendar.getInstance().apply {
                    timeInMillis = calendar.timeInMillis
                    set(Calendar.HOUR_OF_DAY, routine.hour)
                    set(Calendar.MINUTE, routine.minute)
                    set(Calendar.SECOND, 0)
                }.timeInMillis

                val newSchedule = Schedule(
                    title = routine.title,
                    timestamp = scheduleTime,
                    priority = 1, // Default priority
                    isFromRoutine = true,
                    routineId = routineId
                )
                scheduleDao.insert(newSchedule)
            }
             calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        triggerWidgetUpdate()
    }

    suspend fun deleteRoutine(routine: Routine) {
        routineDao.deleteRoutine(routine)
        triggerWidgetUpdate()
    }

    suspend fun resetAllRoutines() {
        scheduleDao.resetRoutinesCompletion()
        triggerWidgetUpdate()
    }

    private fun triggerWidgetUpdate() {
        widgetUpdateJob?.cancel()
        widgetUpdateJob = externalScope.launch {
            delay(1000) // Debounce for 1 second
            updateAllWidgets(context)
        }
    }
}
