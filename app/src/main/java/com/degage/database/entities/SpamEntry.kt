package com.degage.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spam_entries")
data class SpamEntry(
    @PrimaryKey val number: String,
    val source: String = "auto_block",
    val reportCount: Int = 1,
    val firstSeen: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis(),
)
