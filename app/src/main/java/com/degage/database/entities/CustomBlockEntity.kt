package com.degage.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_blocks")
data class CustomBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val value: String,
    val isPrefix: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
)
