package com.pranay.reproai.data.local.dao

import androidx.room.*
import com.pranay.reproai.data.local.entity.IncidentExecutionEntity

@Dao
interface IncidentExecutionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(execution: IncidentExecutionEntity)
    @Query("SELECT * FROM incident_executions WHERE sessionId = :sessionId")
    suspend fun get(sessionId: String): IncidentExecutionEntity?
}
