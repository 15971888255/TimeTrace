package com.example.timetrace.data.repository

import com.example.timetrace.data.local.dao.DiaryDao
import com.example.timetrace.data.local.dao.ScheduleDao
import com.example.timetrace.data.model.Diary
import com.example.timetrace.data.model.Schedule
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val scheduleDao: ScheduleDao,
    private val diaryDao: DiaryDao
) {

    // Schedule methods
    fun getAllSchedules(): Flow<List<Schedule>> = scheduleDao.getAllSchedules()

    suspend fun getScheduleById(id: Long): Schedule? = scheduleDao.getScheduleById(id)

    suspend fun insertSchedule(schedule: Schedule) {
        scheduleDao.insert(schedule)
    }

    suspend fun updateSchedule(schedule: Schedule) {
        scheduleDao.update(schedule)
    }

    suspend fun deleteSchedule(schedule: Schedule) {
        scheduleDao.delete(schedule)
    }

    // Diary methods
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
}
