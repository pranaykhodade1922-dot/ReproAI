package com.pranay.reproai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pranay.reproai.data.local.entity.DebugEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebugEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: DebugEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<DebugEventEntity>)

    @Query("SELECT * FROM debug_events WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getEventsForSessionFlow(sessionId: String): Flow<List<DebugEventEntity>>

    @Query("SELECT * FROM debug_events WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getEventsForSession(sessionId: String): List<DebugEventEntity>

    @Query("SELECT * FROM debug_events WHERE sessionId = :sessionId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    suspend fun getEventsInTimeWindow(sessionId: String, startTime: Long, endTime: Long): List<DebugEventEntity>
}
