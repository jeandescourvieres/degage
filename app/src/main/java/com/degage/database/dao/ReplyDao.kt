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

    // Corps du message : par mode (POLI, ADMINISTRATIF, etc.)
    @Query("SELECT * FROM replies WHERE modeName = :modeName AND partType = 'BODY'")
    fun getBodyByMode(modeName: String): Flow<List<ReplyEntity>>

    @Query("SELECT * FROM replies WHERE modeName = :modeName AND partType = 'BODY' AND isEnabled = 1")
    suspend fun getEnabledBodyByMode(modeName: String): List<ReplyEntity>

    // Salutations et formules de fin : globales (modeName = 'GLOBAL')
    @Query("SELECT * FROM replies WHERE partType = :partType AND modeName = 'GLOBAL'")
    fun getGlobalByPart(partType: String): Flow<List<ReplyEntity>>

    @Query("SELECT * FROM replies WHERE partType = :partType AND modeName = 'GLOBAL' AND isEnabled = 1")
    suspend fun getEnabledGlobalByPart(partType: String): List<ReplyEntity>

    // Compat ancienne API (utilisé dans les écrans Réponses par mode)
    @Query("SELECT * FROM replies WHERE modeName = :modeName AND partType = 'BODY'")
    fun getByMode(modeName: String): Flow<List<ReplyEntity>>

    @Query("SELECT * FROM replies WHERE modeName = :modeName AND partType = 'BODY' AND isEnabled = 1")
    suspend fun getEnabledByMode(modeName: String): List<ReplyEntity>

    @Query("SELECT COUNT(*) FROM replies WHERE modeName = :modeName AND partType = 'BODY'")
    suspend fun countByMode(modeName: String): Int

    @Query("SELECT * FROM replies WHERE modeName = :modeName AND partType = :partType")
    suspend fun getAllBySection(modeName: String, partType: String): List<ReplyEntity>
}
