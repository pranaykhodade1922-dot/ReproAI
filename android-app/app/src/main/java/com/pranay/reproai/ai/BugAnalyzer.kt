package com.pranay.reproai.ai

import android.util.Log
import com.pranay.reproai.data.model.DebugSession
import com.pranay.reproai.data.repository.DebugSessionRepository
import com.pranay.reproai.tracking.EventSanitizer

class BugAnalyzer(
    private val repository: DebugSessionRepository,
    private val aiProvider: AiProvider = LocalAiProvider()
) {

    suspend fun analyzeSessionBug(
        session: DebugSession,
        userDescription: String
    ): AnalysisResult {
        val bugCapturedAt = session.bugCapturedAt ?: System.currentTimeMillis()

        val contextEvents = repository.getBugContextEvents(
            sessionId = session.id,
            bugCapturedAt = bugCapturedAt,
            secondsBefore = 30,
            secondsAfter = 5
        )

        val rawEvents = if (contextEvents.isNotEmpty()) contextEvents else session.events

        val rawInput = AnalysisInput(
            sessionId = session.id,
            bugCapturedAt = bugCapturedAt,
            userDescription = userDescription,
            deviceInfo = session.deviceInfo,
            events = rawEvents,
            currentNetwork = session.networkType,
            orientation = "Portrait",
            appState = session.appState
        )

        val sanitizedInput = EventSanitizer.sanitizeAnalysisInput(rawInput)

        Log.d("BugAnalyzer", "Extracted ${sanitizedInput.events.size} sanitized events for bug context window.")

        val result = try {
            aiProvider.analyze(sanitizedInput)
        } catch (e: Exception) {
            Log.e("BugAnalyzer", "Analysis fallback: ${e.javaClass.simpleName}")
            RuleBasedFallbackProvider().analyze(sanitizedInput)
        }

        val validEventIds = sanitizedInput.events.map { it.id }.toSet()
        val filteredRelevantIds = result.relevantEventIds.filter { validEventIds.contains(it) }

        return result.copy(
            relevantEventIds = if (filteredRelevantIds.isNotEmpty()) filteredRelevantIds else validEventIds.toList()
        )
    }
}
