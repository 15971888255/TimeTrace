package com.example.timetrace.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val timestamp: Long,
    val priority: Int,
    val isCompleted: Boolean = false,
    val isLunar: Boolean = false,
    val isBirthday: Boolean = false
)
