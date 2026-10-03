package com.pranay.reproai.report

import androidx.room.withTransaction
import com.google.gson.Gson
import com.pranay.reproai.ai.AnalysisResult
import com.pranay.reproai.data.local.ReproDatabase
import com.pranay.reproai.data.local.entity.*
import com.pranay.reproai.data.repository.DebugSessionRepository
import com.pranay.reproai.data.remote.dto.*
import java.util.UUID

class IncidentReportRepository(private val db: ReproDatabase, private val sessions: DebugSessionRepository) {
    private val gson = Gson()
    suspend fun load(sessionId: String, currentAnalysis: AnalysisResult? = null): IncidentReport? = db.withTransaction {
        val session = sessions.getSession(sessionId) ?: return@withTransaction null
        val latest = db.incidentExecutionDao().get(sessionId)
        // Import only the real latest legacy run; older overwritten results cannot be reconstructed.
        latest?.let {
            val r = gson.fromJson(it.resultJson, ExecutionResult::class.java).validated()
            db.reportDao().saveExecution(ExecutionHistoryEntity(r.execution_id,sessionId,it.purpose,it.resultJson,r.scenario_id,r.finished_at))
        }
        val analysis = currentAnalysis ?: db.reportDao().analysis(sessionId)?.let { gson.fromJson(it.analysisJson, AnalysisResult::class.java) }
            ?: latest?.let { gson.fromJson(it.analysisJson, AnalysisResult::class.java) }
        currentAnalysis?.let { db.reportDao().saveAnalysis(IncidentAnalysisEntity(sessionId,gson.toJson(it))) }
        val metadata = db.reportDao().metadata(sessionId)
        val runs = db.reportDao().executions(sessionId).map { ExecutionPurpose.valueOf(it.purpose) to gson.fromJson(it.resultJson,ExecutionResult::class.java).validated() }
        val report = IncidentReportBuilder.build(session,analysis,metadata?.reportId ?: UUID.randomUUID().toString(),
            metadata?.createdAt ?: System.currentTimeMillis(),runs)
        db.reportDao().saveMetadata(ReportMetadataEntity(sessionId,report.reportId,report.createdAt,report.status.name,report.likelyOwnerCategory.name))
        report
    }
}
