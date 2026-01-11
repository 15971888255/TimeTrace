package com.example.timetrace.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "diaries")
@TypeConverters(StringListConverter::class)
data class Diary(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val content: String,
    val imagePaths: List<String>
)
