package com.degage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.degage.database.entities.BlockedCallEntity

@Dao
interface BlockedCallDao {
    @Insert
    suspend fun insert(call: BlockedCallEntity)

    @Query("SELECT * FROM blocked_calls ORDER BY timestamp DESC")
    fun getAll(): Flow<List<BlockedCallEntity>>

    @Query("SELECT COUNT(*) FROM blocked_calls")
    fun getCount(): Flow<Int>

    @Query("DELETE FROM blocked_calls WHERE id = :id")
    suspend fun deleteById(id: Long)
}
