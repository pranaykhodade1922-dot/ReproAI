package com.pranay.reproai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debug_sessions")
data class DebugSessionEntity(
    @PrimaryKey val id: String,
    val startedAt: Long,
    val endedAt: Long?,
    val isActive: Boolean,
    val issueTitle: String?,
    val userDescription: String?,
    val bugCapturedAt: Long?,
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val sdkVersion: Int,
    val appVersion: String
)
