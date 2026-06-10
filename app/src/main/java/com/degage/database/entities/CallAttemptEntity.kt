package com.degage.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Trace les appels reçus de numéros non bloqués, pour détecter les rafales d'appels répétés. */
@Entity(tableName = "call_attempts")
data class CallAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String,
    val timestamp: Long = System.currentTimeMillis(),
)
