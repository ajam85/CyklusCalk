package com.example.cykluscalk.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tag_definitions")
    fun getAllTags(): Flow<List<TagDefinition>>

    @Query("DELETE FROM tag_definitions")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagDefinition)

    @Delete
    suspend fun deleteTag(tag: TagDefinition)
    
    @Query("SELECT * FROM tag_definitions WHERE category = :category")
    fun getTagsByCategory(category: String): Flow<List<TagDefinition>>
}
