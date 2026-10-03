package com.pranay.reproai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pranay.reproai.data.model.DebugEventType

@Entity(
    tableName = "debug_events",
    foreignKeys = [
        ForeignKey(
            entity = DebugSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class DebugEventEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val timestamp: Long,
    val elapsedMs: Long,
    val type: DebugEventType,
    val title: String,
    val description: String,
    val metadataJson: String
)
