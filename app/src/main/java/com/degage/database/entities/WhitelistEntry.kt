package com.degage.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whitelist")
data class WhitelistEntry(
    @PrimaryKey val number: String,
    val createdAt: Long = System.currentTimeMillis(),
)
