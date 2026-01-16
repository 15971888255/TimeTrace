package com.example.timetrace.data.model

import androidx.room.TypeConverter

class IntListConverter {
    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        return if (value.isEmpty()) emptyList() else value.split(",").map { it.toInt() }
    }
}
