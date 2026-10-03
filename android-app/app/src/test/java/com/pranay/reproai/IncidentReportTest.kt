package com.pranay.reproai

import com.google.gson.*
import com.pranay.reproai.ai.*
import com.pranay.reproai.data.model.*
import com.pranay.reproai.data.remote.dto.*
import com.pranay.reproai.report.*
import com.pranay.reproai.tracking.EventSanitizer
import org.junit.Assert.*
import org.junit.Test

class IncidentReportTest {
    private val scenario = com.pranay.reproai.ai.TestScenario("payment","Payment failure","",steps=listOf(TestActionItem(TestAction.TAP,"PAY")),
        assertions=listOf(TestAssertion("ASSERT_API_STATUS","/payment","401")))
    private val analysis = AnalysisResult("Payment failure","Failure after network transition","Network transition",
        "Authentication token not refreshed",90,listOf("Auth"),listOf("e"),listOf("HTTP 401"),emptyList(),emptyList(),
        "Payment succeeds","Payment fails",scenario)
    private val session = DebugSession("session-1",startedAt=100,bugCapturedAt=120,userDescription="Payment failed",
        deviceInfo=DeviceInfo("OPPO","CPH2477","12",31),events=listOf(DebugEvent("e","session-1",timestamp=120,
            type=DebugEventType.API_RESPONSE,title="API Response 401",metadata=mapOf("sourcePackage" to "com.pranay.demoshop","source" to "DEMOSHOP"))))
    private fun run(fixed: Boolean=false,mode: ExecutionMode=ExecutionMode.ADB,scoped: Boolean=true): ExecutionResult {
        val id=if(fixed) "verify" else "reproduce"
        val observed=JsonObject().apply {
            addProperty("verified",true);addProperty("executionId",if(scoped) id else "stale");addProperty("source","DEMOSHOP")
            addProperty("lastApiStatus",if(fixed) 200 else 401);addProperty("paymentStatus",if(fixed) "SUCCESS" else "FAILED")
            add("events",JsonArray().apply {add(if(fixed) "TOKEN_REFRESHED" else "TOKEN_EXPIRED");add(if(fixed) "PAYMENT_SUCCESS" else "PAYMENT_FAILED")})
        }
        return ExecutionResult(id,"payment","Payment failure","device",if(fixed) ExecutionStatus.FAILED else ExecutionStatus.PASSED,
            mode,"2026-10-02T00:00:00Z","2026-10-02T00:00:01Z",1000.0,listOf(ExecutionStepResult(0,"TAP","PAY",
                StepStatus.PASSED,"","",1.0,"",JsonObject())),if(fixed) 0 else 1,if(fixed) 1 else 0,null,"DEMOSHOP_DEMO_HOOK",observed)
    }
    private fun report(a: AnalysisResult?=analysis,runs: List<Pair<ExecutionPurpose,ExecutionResult>> = emptyList()) =
        IncidentReportBuilder.build(session,a,"report-1",150,runs)
    @Test fun partialReportIsDraftAndHasNoInventedExecution() {
        val r=report(null);assertEquals(ReportStatus.DRAFT,r.status);assertNull(r.diagnosis);assertNull(r.execution);assertNull(r.verification)
        assertEquals("OPPO",r.environment.manufacturer);assertEquals("12",r.environment.androidVersion)
    }
    @Test fun technicalExecutionIdentitySurvivesPrivacyRedaction() {
        val id="exec-7685566e1694476d8cf1039272407a86"
        val original=run(true)
        val observed=original.observed_state!!.deepCopy().apply {
            addProperty("executionId",id);addProperty("customerPhone","9876543210")
            addProperty("authToken",id)
        }
        val safe=IncidentReportExporter.sanitized(report(runs=listOf(ExecutionPurpose.VERIFY_FIX to
            original.copy(execution_id=id,observed_state=observed))))
        assertEquals(id,safe.verification!!.execution.result.execution_id)
        assertEquals(id,safe.verification.execution.result.observed_state!!.get("executionId").asString)
        assertFalse(safe.verification.execution.result.observed_state!!.get("customerPhone").asString.contains("9876543210"))
        assertEquals("[REDACTED_SECRET]",safe.verification.execution.result.observed_state!!.get("authToken").asString)
    }
    @Test fun analyzedReportIsReadyAndReusesGeneratedScenario() {
        val r=report();assertEquals(ReportStatus.READY,r.status);assertSame(scenario,r.reproduction!!.scenario)
        assertEquals("DemoShop",r.environment.application);assertEquals("com.pranay.demoshop",r.environment.packageName)
    }
    @Test fun reproducedReportRequiresMeasuredSignature() {
        val r=report(runs=listOf(ExecutionPurpose.REPRODUCE to run()));assertEquals(ReportStatus.REPRODUCED,r.status)
        assertTrue(r.execution!!.observedSignature.contains("HTTP 401"))
    }
    @Test fun verifiedReportPreservesOriginalAndFailedFailureAssertions() {
        val r=report(runs=listOf(ExecutionPurpose.REPRODUCE to run(),ExecutionPurpose.VERIFY_FIX to run(true)))
        assertEquals(ReportStatus.FIX_VERIFIED,r.status);assertEquals(true,r.verification!!.sameScenario)
        assertEquals(ExecutionStatus.FAILED,r.verification.execution.result.status)
        assertTrue(r.verification.originalFailure.contains("TOKEN_EXPIRED"));assertTrue(r.verification.execution.observedSignature.contains("HTTP 200"))
    }
    @Test fun staleSignatureDoesNotUpgradeReport() {
        val r=report(runs=listOf(ExecutionPurpose.VERIFY_FIX to run(true,scoped=false)))
        assertEquals(ReportStatus.READY,r.status);assertTrue(r.verification!!.execution.observedSignature.isEmpty())
    }
    @Test fun mismatchedScenarioExcluded() {
        assertNull(report(runs=listOf(ExecutionPurpose.VERIFY_FIX to run(true).copy(scenario_id="other"))).verification)
    }
    @Test fun laterReproductionSupersedesAnOlderFixStatus() {
        val r=report(runs=listOf(ExecutionPurpose.REPRODUCE to run(),ExecutionPurpose.VERIFY_FIX to run(true),ExecutionPurpose.REPRODUCE to run()))
        assertEquals(ReportStatus.REPRODUCED,r.status)
        assertTrue(r.verification!!.originalFailure.contains("HTTP 401"))
    }
    @Test fun mockExecutionIsExplicitAndNeverReproduced() {
        val r=report(runs=listOf(ExecutionPurpose.REPRODUCE to run(mode=ExecutionMode.MOCK)))
        assertEquals(ReportStatus.READY,r.status);assertTrue(IncidentReportExporter.summary(r).contains("MOCK EXECUTION"))
        assertTrue(IncidentReportExporter.markdown(r).contains("Execution mode: MOCK"))
    }
    @Test fun ownerCategoryIsInferenceFromActualApplicationAndDiagnosis() {
        assertEquals(OwnerCategory.APPLICATION,report().likelyOwnerCategory)
        assertEquals(OwnerCategory.UNKNOWN,report(null).likelyOwnerCategory)
        assertEquals(OwnerCategory.UNKNOWN,IncidentReportBuilder.build(session.copy(events=emptyList()),analysis,"r",150).likelyOwnerCategory)
    }
    @Test fun jsonHasMachineReadableSectionsAndNoRawSession() {
        val json=JsonParser.parseString(IncidentReportExporter.json(report())).asJsonObject
        listOf("report","session","environment","evidence","analysis","reproduction","execution","verification").forEach {assertTrue(json.has(it))}
        assertEquals("READY",json.getAsJsonObject("report").get("status").asString)
        assertFalse(json.getAsJsonObject("session").has("events"))
    }
    @Test fun markdownAndTextIncludePartialAndMeasuredSections() {
        val md=IncidentReportExporter.markdown(report(null))
        assertTrue(md.startsWith("# ReproAI Incident Report"));assertTrue(md.contains("## Verification\nNot available yet"))
        assertFalse(IncidentReportExporter.text(report()).startsWith("#"))
    }
    @Test fun summaryHasActualStepsAndInferredCause() {
        val s=IncidentReportExporter.summary(report());assertTrue(s.contains("1. TAP PAY"));assertTrue(s.contains("Probable cause (inferred)"))
        assertFalse(s.contains("FIX VERIFIED"));assertTrue(s.endsWith("Generated by ReproAI"))
    }
    @Test fun everyFormatRedactsNestedSecretsAndPersonalInformation() {
        val result=run().apply { steps[0].evidence.addProperty("Authorization","Basic secret-basic")
            steps[0].evidence.add("nested",JsonObject().apply {addProperty("refresh_token","raw-refresh");addProperty("api_key","raw-api")}) }
        val dirty=report(runs=listOf(ExecutionPurpose.REPRODUCE to result)).copy(description="Bearer raw-bearer user@example.com +919876543210 password=raw-password Authorization: Basic raw-basic {\"api_key\":\"raw-json\"}",
            diagnosis=analysis.copy(probableCause="authorization: secret-header access_token=raw-access"))
        listOf(IncidentReportExporter.json(dirty),IncidentReportExporter.markdown(dirty),IncidentReportExporter.text(dirty),IncidentReportExporter.summary(dirty)).forEach {
            listOf("raw-bearer","user@example.com","9876543210","raw-password","secret-header","raw-access","raw-refresh","raw-api","secret-basic","raw-basic","raw-json").forEach { secret -> assertFalse(secret,it.contains(secret)) }
        }
        assertEquals("[REDACTED_SECRET]",EventSanitizer.sanitizeMetadata(mapOf("clientSecret" to "opaque")).getValue("clientSecret"))
    }
}
