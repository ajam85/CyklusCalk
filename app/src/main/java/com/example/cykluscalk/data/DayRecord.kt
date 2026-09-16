package com.example.cykluscalk.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_records")
data class DayRecord(
    @PrimaryKey
    val date: String,
    val note: String = "",
    val tags: List<String> = emptyList(),
    // Rozšíření pro sex
    val sexFrequency: Int = 0,
    val sexTimes: List<String> = emptyList(),
    val sexSunrise: Int = 0,
    val sexDay: Int = 0,
    val sexSunset: Int = 0,
    val sexMoon: Int = 0,
    val sexUnassigned: Int = 0,
    // Bazální teplota a váha
    val basalTemperature: Double? = null,
    val bodyWeight: Double? = null
)
