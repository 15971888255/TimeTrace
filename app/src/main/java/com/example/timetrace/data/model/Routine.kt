package com.example.timetrace.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class Routine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val weekdays: List<Int>, // 1 for Monday, 7 for Sunday
    val hour: Int,
    val minute: Int,
    val creationTimestamp: Long = System.currentTimeMillis()
)
