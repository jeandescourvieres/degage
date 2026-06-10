package com.degage.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.degage.database.entities.ReplyEntity

@Dao
interface ReplyDao {
    @Insert
    suspend fun insert(reply: ReplyEntity)

    @Update
    suspend fun update(reply: ReplyEntity)

    @Delete
    suspend fun delete(reply: ReplyEntity)

    // Corps du message : par mode (POLI, ADMINISTRATIF, etc.) et par langue
    @Query("SELECT * FROM replies WHERE modeName = :modeName AND partType = 'BODY' AND language = :language")
    fun getBodyByMode(modeName: String, language: String): Flow<List<ReplyEntity>>

    @Query("SELECT * FROM replies WHERE modeName = :modeName AND partType = 'BODY' AND isEnabled = 1 AND language = :language")
    suspend fun getEnabledBodyByMode(modeName: String, language: String): List<ReplyEntity>

    // Salutations et formules de fin : globales (modeName = 'GLOBAL'), par langue
    @Query("SELECT * FROM replies WHERE partType = :partType AND modeName = 'GLOBAL' AND language = :language")
    fun getGlobalByPart(partType: String, language: String): Flow<List<ReplyEntity>>

    @Query("SELECT * FROM replies WHERE partType = :partType AND modeName = 'GLOBAL' AND isEnabled = 1 AND language = :language")
    suspend fun getEnabledGlobalByPart(partType: String, language: String): List<ReplyEntity>

    // Compat ancienne API (utilisé dans les écrans Réponses par mode), toujours en français
    @Query("SELECT * FROM replies WHERE modeName = :modeName AND partType = 'BODY' AND language = 'FR'")
    fun getByMode(modeName: String): Flow<List<ReplyEntity>>

    @Query("SELECT * FROM replies WHERE modeName = :modeName AND partType = 'BODY' AND isEnabled = 1 AND language = 'FR'")
    suspend fun getEnabledByMode(modeName: String): List<ReplyEntity>

    @Query("SELECT COUNT(*) FROM replies WHERE modeName = :modeName AND partType = 'BODY' AND language = 'FR'")
    suspend fun countByMode(modeName: String): Int

    @Query("SELECT * FROM replies WHERE modeName = :modeName AND partType = :partType AND language = :language")
    suspend fun getAllBySection(modeName: String, partType: String, language: String): List<ReplyEntity>
}
