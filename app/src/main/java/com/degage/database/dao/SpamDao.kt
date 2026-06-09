package com.degage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.degage.database.entities.SpamEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface SpamDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: SpamEntry)

    @Query("SELECT EXISTS(SELECT 1 FROM spam_entries WHERE number = :number LIMIT 1)")
    suspend fun isKnownSpam(number: String): Boolean

    @Query("UPDATE spam_entries SET reportCount = reportCount + 1, lastSeen = :now WHERE number = :number")
    suspend fun incrementReport(number: String, now: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM spam_entries")
    fun getCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM spam_entries")
    suspend fun getCountOnce(): Int
}
