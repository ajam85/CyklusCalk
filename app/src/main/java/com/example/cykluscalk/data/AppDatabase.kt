package com.example.cykluscalk.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [DayRecord::class, TagDefinition::class, PregnancyRecord::class], version = 13, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dayRecordDao(): DayRecordDao
    abstract fun tagDao(): TagDao
    abstract fun pregnancyDao(): PregnancyDao
}
