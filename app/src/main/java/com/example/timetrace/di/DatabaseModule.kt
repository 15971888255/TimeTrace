package com.example.timetrace.di

import android.content.Context
import androidx.room.Room
import com.example.timetrace.data.local.dao.DiaryDao
import com.example.timetrace.data.local.dao.ScheduleDao
import com.example.timetrace.data.local.database.TimeTraceDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTimeTraceDatabase(@ApplicationContext context: Context): TimeTraceDatabase {
        return Room.databaseBuilder(
            context,
            TimeTraceDatabase::class.java,
            "timetrace_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideScheduleDao(database: TimeTraceDatabase): ScheduleDao {
        return database.scheduleDao()
    }

    @Provides
    @Singleton
    fun provideDiaryDao(database: TimeTraceDatabase): DiaryDao {
        return database.diaryDao()
    }
}
