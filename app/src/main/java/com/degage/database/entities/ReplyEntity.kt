package com.degage.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "replies")
data class ReplyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val modeName: String,           // AppMode.name ou "GLOBAL" pour salutations/formules de fin
    val partType: String = "BODY",  // MessagePart.name : SALUTATION | BODY | ENDING
    val isEnabled: Boolean = true,
    val isCustom: Boolean = false,
    val language: String = "FR"     // FR | DE | IT | EN
)
