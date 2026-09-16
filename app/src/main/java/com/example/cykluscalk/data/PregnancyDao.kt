package com.example.cykluscalk.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PregnancyDao {
    @Query("SELECT * FROM pregnancy_history ORDER BY id DESC")
    fun getAllPregnancies(): Flow<List<PregnancyRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPregnancy(record: PregnancyRecord)

    @Delete
    suspend fun deletePregnancy(record: PregnancyRecord)

    @Query("DELETE FROM pregnancy_history")
    suspend fun deleteAll()
}
