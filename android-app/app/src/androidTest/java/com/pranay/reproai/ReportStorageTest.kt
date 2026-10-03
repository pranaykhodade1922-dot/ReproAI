package com.pranay.reproai

import androidx.room.Room
import androidx.core.content.FileProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.pranay.reproai.data.local.ReproDatabase
import com.pranay.reproai.data.local.entity.*
import com.pranay.reproai.data.model.DebugSession
import com.pranay.reproai.data.repository.DebugSessionRepository
import com.pranay.reproai.report.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.UUID

class ReportStorageTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    @Test fun reportMetadataAndExecutionHistorySurviveDatabaseReopen() = runBlocking {
        val name="report-test-${UUID.randomUUID()}.db"
        var db=Room.databaseBuilder(context,ReproDatabase::class.java,name).build()
        try {
            var sessions=DebugSessionRepository(db.debugSessionDao(),db.debugEventDao())
            sessions.insertSession(DebugSession("fixture",startedAt=1,issueTitle="Fixture"))
            val first=IncidentReportRepository(db,sessions).load("fixture")!!
            db.reportDao().saveExecution(ExecutionHistoryEntity("exec1","fixture","REPRODUCE","{}","scenario","2026-01-01"))
            db.reportDao().saveExecution(ExecutionHistoryEntity("exec2","fixture","VERIFY_FIX","{}","scenario","2026-01-02"))
            db.close()
            db=Room.databaseBuilder(context,ReproDatabase::class.java,name).build()
            assertEquals(first.reportId,db.reportDao().metadata("fixture")!!.reportId)
            assertEquals(first.createdAt,db.reportDao().metadata("fixture")!!.createdAt)
            assertEquals(listOf("exec1","exec2"),db.reportDao().executions("fixture").map { it.executionId })
            assertNotNull(db.debugSessionDao().getSessionById("fixture"))
        } finally { db.close();context.deleteDatabase(name) }
    }
    @Test fun exportedFilesAreSanitizedAndProviderRejectsUnrelatedPaths() {
        val report=IncidentReportBuilder.build(DebugSession("file-fixture",issueTitle="Bearer test-secret"),null,"r",1)
        ReportFormat.entries.filter { it != ReportFormat.SCENARIO_JSON }.forEach {
            val file=ReportSharing.exportFile(context,report,it)
            try {
                assertFalse(file.readText().contains("test-secret"))
                val uri=FileProvider.getUriForFile(context,"${context.packageName}.reports",file)
                assertEquals("content",uri.scheme)
                assertEquals("${context.packageName}.reports",uri.authority)
                assertFalse(uri.toString().contains(context.filesDir.path))
            } finally {file.delete()}
        }
        try {
            FileProvider.getUriForFile(context,"${context.packageName}.reports",File(context.filesDir,"unrelated.txt"))
            fail("Provider must reject files outside reports directory")
        } catch(expected: IllegalArgumentException) { }
    }
}
