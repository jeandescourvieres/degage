package com.degage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.degage.database.entities.CallAttemptEntity

@Dao
interface CallAttemptDao {

    @Insert
    suspend fun insert(attempt: CallAttemptEntity)

    @Query("SELECT COUNT(*) FROM call_attempts WHERE number = :number AND timestamp >= :since")
    suspend fun countSince(number: String, since: Long): Int

    @Query("DELETE FROM call_attempts WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("DELETE FROM call_attempts WHERE number = :number")
    suspend fun deleteByNumber(number: String)
}
