package com.degage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.degage.database.entities.WhitelistEntry

@Dao
interface WhitelistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WhitelistEntry)

    @Query("SELECT EXISTS(SELECT 1 FROM whitelist WHERE number = :number LIMIT 1)")
    suspend fun isWhitelisted(number: String): Boolean
}
