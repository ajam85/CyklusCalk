package com.example.cykluscalk.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pregnancy_history")
data class PregnancyRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val conceptionDate: String?,
    val doctorDueDate: String?,
    val endDate: String?,
    val milestonesJson: String,
    val note: String = ""
)
