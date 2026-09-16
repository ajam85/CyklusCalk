package com.example.cykluscalk.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tag_definitions")
data class TagDefinition(
    @PrimaryKey
    val name: String,
    val category: String, // "Symptom" nebo "Sex"
    val colorHex: String? = null // Pro budoucí nastavení barev
)
