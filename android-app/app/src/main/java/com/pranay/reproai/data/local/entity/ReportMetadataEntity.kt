package com.pranay.reproai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="incident_reports")
data class ReportMetadataEntity(@PrimaryKey val sessionId: String, val reportId: String, val createdAt: Long,
    val status: String, val likelyOwnerCategory: String)

@Entity(tableName="execution_history")
data class ExecutionHistoryEntity(@PrimaryKey val executionId: String, val sessionId: String, val purpose: String,
    val resultJson: String, val scenarioId: String, val finishedAt: String)

@Entity(tableName="incident_analyses")
data class IncidentAnalysisEntity(@PrimaryKey val sessionId: String, val analysisJson: String)
