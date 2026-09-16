package com.example.cykluscalk.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DayRecordDao {
    @Query("SELECT * FROM day_records WHERE date = :date")
    fun getRecordByDate(date: String): Flow<DayRecord?>

    @Query("DELETE FROM day_records")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DayRecord)

    @Query("SELECT * FROM day_records")
    fun getAllRecords(): Flow<List<DayRecord>>
}
