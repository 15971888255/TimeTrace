package com.example.timetrace.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedules",
    foreignKeys = [ForeignKey(
        entity = Routine::class, 
        parentColumns = ["id"], 
        childColumns = ["routineId"], 
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["routineId"])]
)
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val timestamp: Long,
    val priority: Int,
    val isCompleted: Boolean = false,
    val isLunar: Boolean = false,
    val isBirthday: Boolean = false,
    val isFromRoutine: Boolean = false,
    val routineId: Int? = null
)
