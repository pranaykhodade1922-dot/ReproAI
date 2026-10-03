package com.pranay.reproai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pranay.reproai.data.local.entity.DebugSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebugSessionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSession(session: DebugSessionEntity)

    @Update
    suspend fun updateSession(session: DebugSessionEntity)

    @Query("SELECT * FROM debug_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): DebugSessionEntity?

    @Query("SELECT * FROM debug_sessions ORDER BY startedAt DESC")
    fun getAllSessionsFlow(): Flow<List<DebugSessionEntity>>

    @Query("SELECT * FROM debug_sessions ORDER BY startedAt DESC")
    suspend fun getAllSessions(): List<DebugSessionEntity>

    @Query("DELETE FROM debug_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)
}
