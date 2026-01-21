package com.example.timetrace.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.timetrace.data.model.Schedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules WHERE isBirthday = 0 ORDER BY timestamp DESC")
    fun getAllSchedules(): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE isBirthday = 1 ORDER BY timestamp DESC")
    fun getAllBirthdays(): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE timestamp >= :startOfToday AND isCompleted = 0 ORDER BY timestamp ASC")
    fun getUpcomingSchedules(startOfToday: Long): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: Long): Schedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: Schedule)

    @Update
    suspend fun update(schedule: Schedule)

    @Delete
    suspend fun delete(schedule: Schedule)

    @Query("UPDATE schedules SET isCompleted = 0 WHERE isFromRoutine = 1")
    suspend fun resetRoutinesCompletion()
}
