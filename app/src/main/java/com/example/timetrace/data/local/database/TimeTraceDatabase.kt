package com.example.timetrace.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.timetrace.data.local.dao.DiaryDao
import com.example.timetrace.data.local.dao.ScheduleDao
import com.example.timetrace.data.model.Diary
import com.example.timetrace.data.model.Schedule
import com.example.timetrace.data.model.StringListConverter

@Database(entities = [Schedule::class, Diary::class], version = 2, exportSchema = false)
@TypeConverters(StringListConverter::class)
abstract class TimeTraceDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
    abstract fun diaryDao(): DiaryDao
}
