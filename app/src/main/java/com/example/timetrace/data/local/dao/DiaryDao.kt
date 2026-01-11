package com.example.timetrace.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.timetrace.data.model.Diary

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diaries WHERE date = :date LIMIT 1")
    suspend fun getDiaryByDate(date: Long): Diary?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diary: Diary)

    @Update
    suspend fun update(diary: Diary)

    @Delete
    suspend fun delete(diary: Diary)
}
