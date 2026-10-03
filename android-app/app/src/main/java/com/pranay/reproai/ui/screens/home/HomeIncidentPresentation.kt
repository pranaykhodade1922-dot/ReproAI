package com.pranay.reproai.ui.screens.home

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.pranay.reproai.ai.AnalysisResult
import com.pranay.reproai.data.local.ReproDatabase
import com.pranay.reproai.data.remote.RunnerApiClient
import com.pranay.reproai.data.remote.dto.*
import com.pranay.reproai.ui.screens.running.executionTitle
import kotlinx.coroutines.CancellationException

data class HomeIncidentSummary(val title: String, val status: String)

/** Read-only presentation join. Session records and execution results remain unchanged. */
@Composable
fun rememberIncidentSummaries(ids: List<String>, refreshKey: String?): Map<String, HomeIncidentSummary> {
    val context = LocalContext.current.applicationContext
    var summaries by remember { mutableStateOf<Map<String, HomeIncidentSummary>>(emptyMap()) }
    LaunchedEffect(ids, refreshKey) {
        val next = mutableMapOf<String, HomeIncidentSummary>()
        val dao = ReproDatabase.getInstance(context).incidentExecutionDao()
        for (id in ids) {
            try {
                val saved = dao.get(id) ?: continue
                val analysis = RunnerApiClient.gson.fromJson(saved.analysisJson, AnalysisResult::class.java)
                val result = RunnerApiClient.gson.fromJson(saved.resultJson, ExecutionResult::class.java).validated()
                val state = RunnerState(phase = ExecutionPhase.Completed, result = result,
                    purpose = ExecutionPurpose.valueOf(saved.purpose))
                val status = when (executionTitle(state, analysis.testScenario)) {
                    "BUG REPRODUCED" -> "REPRODUCED"
                    "FIX VERIFIED" -> "VERIFIED"
                    else -> if (result.execution_mode == ExecutionMode.MOCK) "MOCK" else result.status.name
                }
                next[id] = HomeIncidentSummary(analysis.issueTitle, status)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                // Keep the actual session row if its optional saved execution cannot be read.
            }
        }
        summaries = next
    }
    return summaries
}
