package com.pranay.reproai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incident_executions")
data class IncidentExecutionEntity(
    @PrimaryKey val sessionId: String,
    val analysisJson: String,
    val scenarioJson: String,
    val resultJson: String,
    val purpose: String
)
