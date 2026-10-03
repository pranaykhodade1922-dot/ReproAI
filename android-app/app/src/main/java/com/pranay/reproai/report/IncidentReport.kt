package com.pranay.reproai.report

import com.pranay.reproai.ai.AnalysisResult
import com.pranay.reproai.ai.TestScenario
import com.pranay.reproai.data.model.DebugSession
import com.pranay.reproai.data.remote.dto.*
import com.pranay.reproai.ui.screens.running.executionTitle

enum class ReportStatus { DRAFT, READY, REPRODUCED, FIX_VERIFIED }
enum class OwnerCategory { APPLICATION, BACKEND, NETWORK, ANDROID_OS, DEVICE, UNKNOWN }
data class ReportEnvironment(val manufacturer: String, val model: String, val androidVersion: String,
    val application: String?, val packageName: String?, val runnerMode: String?, val networkStrategy: String?,
    val capturedAt: Long?)
data class ReportEvidence(val timestamp: Long, val source: String, val type: String, val title: String,
    val description: String)
data class ReportReproduction(val scenario: TestScenario, val expectedBehavior: String, val actualBehavior: String)
data class ReportExecution(val result: ExecutionResult, val outcome: String, val observedSignature: List<String>)
data class ReportVerification(val execution: ReportExecution, val originalFailure: List<String>, val sameScenario: Boolean?)
data class IncidentReport(val reportId: String, val sessionId: String, val incidentId: String, val createdAt: Long,
    val issueTitle: String, val description: String, val status: ReportStatus, val likelyOwnerCategory: OwnerCategory,
    val environment: ReportEnvironment, val evidence: List<ReportEvidence>, val diagnosis: AnalysisResult?,
    val reproduction: ReportReproduction?, val execution: ReportExecution?, val verification: ReportVerification?,
    val artifacts: List<String> = emptyList())

object IncidentReportBuilder {
    fun build(session: DebugSession, analysis: AnalysisResult?, reportId: String, createdAt: Long,
        runs: List<Pair<ExecutionPurpose, ExecutionResult>> = emptyList()): IncidentReport {
        val scenario = analysis?.testScenario
        val scoped = runs.filter { scenario != null && it.second.scenario_id == scenario.id }
        fun present(pair: Pair<ExecutionPurpose, ExecutionResult>) = ReportExecution(pair.second,
            executionTitle(RunnerState(phase=ExecutionPhase.Completed, purpose=pair.first, result=pair.second), scenario),
            signature(pair.second))
        val original = scoped.lastOrNull { it.first == ExecutionPurpose.REPRODUCE }?.let(::present)
        val fixed = scoped.lastOrNull { it.first == ExecutionPurpose.VERIFY_FIX }?.let(::present)
        val originalIndex = scoped.indexOfLast { it.first == ExecutionPurpose.REPRODUCE }
        val fixedIndex = scoped.indexOfLast { it.first == ExecutionPurpose.VERIFY_FIX }
        val beforeFix = scoped.take(fixedIndex.coerceAtLeast(0)).lastOrNull { it.first == ExecutionPurpose.REPRODUCE }?.let(::present)
        val latest = scoped.lastOrNull()?.second
        val pkg = session.events.mapNotNull { it.metadata["sourcePackage"] }.lastOrNull()
        val app = if (pkg == "com.pranay.demoshop") "DemoShop" else pkg
        val status = when {
            fixed?.outcome == "FIX VERIFIED" && fixedIndex > originalIndex -> ReportStatus.FIX_VERIFIED
            original?.outcome == "BUG REPRODUCED" -> ReportStatus.REPRODUCED
            analysis != null -> ReportStatus.READY
            else -> ReportStatus.DRAFT
        }
        val relevant = analysis?.relevantEventIds.orEmpty().toSet()
        val evidence = session.events.filter { it.id in relevant || it.type.name in setOf("API_RESPONSE", "ERROR", "NETWORK_CHANGED", "RETRY") ||
            it.title.contains(Regex("PAY|TOKEN_|NETWORK_|RETRY")) }.map {
            ReportEvidence(it.timestamp, it.metadata["source"] ?: "DEVICE", it.type.name, it.title, it.description)
        }
        val owner = if (analysis != null && pkg == "com.pranay.demoshop" &&
            (analysis.probableCause + analysis.relevantComponents.joinToString()).contains(Regex("auth|token|checkout|saved state", RegexOption.IGNORE_CASE)))
            OwnerCategory.APPLICATION else OwnerCategory.UNKNOWN
        return IncidentReport(reportId, session.id, "RPA-${session.id.take(8).uppercase()}", createdAt,
            analysis?.issueTitle ?: session.issueTitle ?: "Untitled incident", session.userDescription.orEmpty(), status, owner,
            ReportEnvironment(session.deviceInfo.manufacturer, session.deviceInfo.model, session.deviceInfo.androidVersion,
                app, pkg, latest?.execution_mode?.name, latest?.network_strategy, session.bugCapturedAt), evidence, analysis,
            analysis?.let { ReportReproduction(it.testScenario, it.expectedBehavior, it.actualBehavior) }, original,
            fixed?.let { ReportVerification(it, beforeFix?.observedSignature.orEmpty(), beforeFix?.let { r -> r.result.scenario_id == it.result.scenario_id }) })
    }
    private fun signature(result: ExecutionResult): List<String> = runCatching {
        if (result.execution_mode == ExecutionMode.MOCK) return@runCatching emptyList<String>()
        val observed = result.observed_state ?: return@runCatching emptyList<String>()
        if (observed.get("verified")?.asBoolean != true || observed.get("executionId")?.asString != result.execution_id)
            return@runCatching emptyList<String>()
        observed.getAsJsonArray("events")?.map { it.asString }.orEmpty().distinct() +
            listOfNotNull(observed.get("lastApiStatus")?.takeUnless { it.isJsonNull }?.let { "HTTP ${it.asInt}" })
    }.getOrDefault(emptyList())
}
