package com.example.cykluscalk.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DayRecordRepository @Inject constructor(
    private val dayRecordDao: DayRecordDao,
    private val tagDao: TagDao,
    private val pregnancyDao: PregnancyDao
) {
    // Day Records
    fun getRecordByDate(date: String): Flow<DayRecord?> = dayRecordDao.getRecordByDate(date)
    fun getAllRecords(): Flow<List<DayRecord>> = dayRecordDao.getAllRecords()
    suspend fun saveRecord(record: DayRecord) = dayRecordDao.insertRecord(record)
    suspend fun deleteAllRecords() = dayRecordDao.deleteAll()

    // Tags
    fun getAllTags(): Flow<List<TagDefinition>> = tagDao.getAllTags()
    suspend fun addTag(tag: TagDefinition) = tagDao.insertTag(tag)
    suspend fun deleteTag(tag: TagDefinition) = tagDao.deleteTag(tag)
    suspend fun deleteAllTags() = tagDao.deleteAll()

    // Pregnancy History
    fun getAllPregnancies(): Flow<List<PregnancyRecord>> = pregnancyDao.getAllPregnancies()
    suspend fun addPregnancy(record: PregnancyRecord) = pregnancyDao.insertPregnancy(record)
    suspend fun deletePregnancy(record: PregnancyRecord) = pregnancyDao.deletePregnancy(record)
    suspend fun deleteAllPregnancies() = pregnancyDao.deleteAll()
}
