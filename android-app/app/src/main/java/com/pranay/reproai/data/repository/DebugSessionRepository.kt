package com.pranay.reproai.data.repository

import android.util.Log
import com.pranay.reproai.data.local.Converters
import com.pranay.reproai.data.local.dao.DebugEventDao
import com.pranay.reproai.data.local.dao.DebugSessionDao
import com.pranay.reproai.data.local.entity.DebugEventEntity
import com.pranay.reproai.data.local.entity.DebugSessionEntity
import com.pranay.reproai.data.model.DebugEvent
import com.pranay.reproai.data.model.DebugSession
import com.pranay.reproai.data.model.DeviceInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DebugSessionRepository(
    private val sessionDao: DebugSessionDao,
    private val eventDao: DebugEventDao,
    private val converters: Converters = Converters()
) {
    suspend fun insertSession(session: DebugSession) {
        val entity = session.toEntity()
        sessionDao.insertSession(entity)
        Log.d("ReproDB", "Parent session persisted in Room: ${session.id}")
    }

    suspend fun updateSession(session: DebugSession) {
        val entity = session.toEntity()
        sessionDao.updateSession(entity)
    }

    suspend fun insertEvent(event: DebugEvent) {
        val entity = DebugEventEntity(
            id = event.id,
            sessionId = event.sessionId,
            timestamp = event.timestamp,
            elapsedMs = event.elapsedMs,
            type = event.type,
            title = event.title,
            description = event.description,
            metadataJson = converters.fromMetadataMap(event.metadata)
        )
        try {
            eventDao.insertEvent(entity)
        } catch (e: Exception) {
            Log.e("ReproDB", "Event persistence failed for session ${event.sessionId}: ${e.javaClass.simpleName}")
        }
    }

    suspend fun getSession(sessionId: String): DebugSession? {
        val entity = sessionDao.getSessionById(sessionId) ?: return null
        val events = getEventsForSession(sessionId)
        return entity.toDomainModel(events)
    }

    suspend fun getEventsForSession(sessionId: String): List<DebugEvent> {
        return eventDao.getEventsForSession(sessionId).map { entity ->
            entity.toDomainModel()
        }
    }

    fun getRecentSessionsFlow(): Flow<List<DebugSession>> {
        return sessionDao.getAllSessionsFlow().map { entities ->
            entities.map { entity ->
                val events = getEventsForSession(entity.id)
                entity.toDomainModel(events)
            }
        }
    }

    suspend fun getBugContextEvents(
        sessionId: String,
        bugCapturedAt: Long,
        secondsBefore: Int = 30,
        secondsAfter: Int = 5
    ): List<DebugEvent> {
        val startTime = bugCapturedAt - (secondsBefore * 1000L)
        val endTime = bugCapturedAt + (secondsAfter * 1000L)
        return eventDao.getEventsInTimeWindow(sessionId, startTime, endTime).map {
            it.toDomainModel()
        }
    }

    private fun DebugSession.toEntity(): DebugSessionEntity {
        return DebugSessionEntity(
            id = id,
            startedAt = startedAt,
            endedAt = endedAt,
            isActive = isActive,
            issueTitle = issueTitle,
            userDescription = userDescription,
            bugCapturedAt = bugCapturedAt,
            manufacturer = deviceInfo.manufacturer,
            model = deviceInfo.model,
            androidVersion = deviceInfo.androidVersion,
            sdkVersion = deviceInfo.sdkVersion,
            appVersion = deviceInfo.appVersion
        )
    }

    private fun DebugSessionEntity.toDomainModel(events: List<DebugEvent>): DebugSession {
        return DebugSession(
            id = id,
            startedAt = startedAt,
            endedAt = endedAt,
            isActive = isActive,
            issueTitle = issueTitle ?: "Debug Session",
            userDescription = userDescription,
            bugCapturedAt = bugCapturedAt,
            deviceInfo = DeviceInfo(
                manufacturer = manufacturer,
                model = model,
                androidVersion = androidVersion,
                sdkVersion = sdkVersion,
                appVersion = appVersion
            ),
            events = events
        )
    }

    private fun DebugEventEntity.toDomainModel(): DebugEvent {
        return DebugEvent(
            id = id,
            sessionId = sessionId,
            timestamp = timestamp,
            elapsedMs = elapsedMs,
            type = type,
            title = title,
            description = description,
            metadata = converters.toMetadataMap(metadataJson)
        )
    }
}
