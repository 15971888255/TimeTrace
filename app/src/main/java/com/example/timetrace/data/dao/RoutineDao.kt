package com.example.timetrace.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.timetrace.data.model.Routine
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY creationTimestamp DESC")
    fun getAllRoutines(): Flow<List<Routine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine): Long // Return the id of the inserted routine

    @Delete
    suspend fun deleteRoutine(routine: Routine)
}
