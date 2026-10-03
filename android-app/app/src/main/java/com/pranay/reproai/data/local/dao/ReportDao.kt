package com.pranay.reproai.data.local.dao

import androidx.room.*
import com.pranay.reproai.data.local.entity.*

@Dao
interface ReportDao {
    @Query("SELECT * FROM incident_reports WHERE sessionId = :id") suspend fun metadata(id: String): ReportMetadataEntity?
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun saveMetadata(value: ReportMetadataEntity)
    @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun saveExecution(value: ExecutionHistoryEntity)
    @Query("SELECT * FROM execution_history WHERE sessionId = :id ORDER BY finishedAt, executionId") suspend fun executions(id: String): List<ExecutionHistoryEntity>
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun saveAnalysis(value: IncidentAnalysisEntity)
    @Query("SELECT * FROM incident_analyses WHERE sessionId = :id") suspend fun analysis(id: String): IncidentAnalysisEntity?
}
