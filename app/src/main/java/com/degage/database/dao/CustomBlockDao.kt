package com.degage.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.degage.database.entities.CustomBlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomBlockDao {

    @Insert
    suspend fun insert(entry: CustomBlockEntity)

    @Delete
    suspend fun delete(entry: CustomBlockEntity)

    @Query("SELECT * FROM custom_blocks ORDER BY createdAt DESC")
    fun getAll(): Flow<List<CustomBlockEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM custom_blocks WHERE isPrefix = 0 AND value = :number LIMIT 1)")
    suspend fun isExactBlocked(number: String): Boolean

    @Query("SELECT value FROM custom_blocks WHERE isPrefix = 1")
    suspend fun getPrefixes(): List<String>

    @Query("DELETE FROM custom_blocks WHERE isPrefix = 0 AND value = :value")
    suspend fun deleteExactByValue(value: String)
}
