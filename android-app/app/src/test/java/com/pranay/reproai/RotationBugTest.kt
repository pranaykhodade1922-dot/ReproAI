package com.pranay.reproai

import com.google.gson.*
import com.pranay.reproai.ai.*
import com.pranay.reproai.data.model.*
import com.pranay.reproai.data.remote.dto.*
import com.pranay.reproai.ui.screens.running.executionTitle
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class RotationBugTest {
    private fun input(fixed: Boolean=false, onlyRotation: Boolean=false): AnalysisInput {
        val titles=if(onlyRotation) listOf("Orientation changed") else listOf("PAYMENT_METHOD_SELECTED","Orientation changed","CHECKOUT_RECREATED",
            if(fixed) "CHECKOUT_STATE_RESTORED" else "CHECKOUT_STATE_LOST",if(fixed) "CHECKOUT_VALID" else "CHECKOUT_INVALID")
        val events=titles.mapIndexed {i,t->DebugEvent("e$i","rotation",timestamp=i.toLong(),
            type=if(t=="Orientation changed") DebugEventType.ORIENTATION_CHANGED else DebugEventType.CUSTOM,title=t,
            metadata=if(t=="Orientation changed") mapOf("from" to "PORTRAIT","to" to "LANDSCAPE") else mapOf("source" to "DEMOSHOP"))}
        return AnalysisInput("rotation",5,"Checkout lost selection",DeviceInfo(),events)
    }
    @Test fun buggyEvidenceGeneratesRotationScenarioWithoutNetworkActions()=runBlocking {
        val a=RuleBasedFallbackProvider().analyze(input())
        assertEquals("Checkout State Lost After Device Rotation",a.issueTitle)
        assertTrue(a.testScenario.steps.any {it.action==TestAction.ROTATE_DEVICE})
        assertFalse(a.testScenario.steps.any {it.action==TestAction.CHANGE_NETWORK})
        assertTrue(a.testScenario.assertions.any {it.target=="CHECKOUT_STATE_LOST"})
    }
    @Test fun fixedEvidenceDoesNotDiagnoseStateLoss()=runBlocking {
        assertFalse(RuleBasedFallbackProvider().analyze(input(true)).issueTitle.contains("State Lost"))
    }
    @Test fun orientationAloneIsInsufficient()=runBlocking {
        val a=RuleBasedFallbackProvider().analyze(input(onlyRotation=true));assertTrue(a.confidence<50)
        assertFalse(a.issueTitle.contains("State Lost"))
    }
    @Test fun normalProviderUsesTheSamePattern()=runBlocking {
        assertEquals("Checkout State Lost After Device Rotation",LocalAiProvider().analyze(input()).issueTitle)
    }
    @Test fun measuredRotationFailureAndHealthyStateUseTheSameScenario()=runBlocking {
        val analysis=RuleBasedFallbackProvider().analyze(input())
        val scenario=analysis.testScenario
        fun result(fixed:Boolean):RunnerState {
            val observed=JsonObject().apply {
                addProperty("verified",true);addProperty("executionId","run");addProperty("source","DEMOSHOP")
                add("lastApiStatus",JsonNull.INSTANCE)
                add("events",JsonArray().apply {listOf("PAYMENT_METHOD_SELECTED","ORIENTATION_CHANGED","CHECKOUT_RECREATED",
                    if(fixed) "CHECKOUT_STATE_RESTORED" else "CHECKOUT_STATE_LOST",if(fixed) "CHECKOUT_VALID" else "CHECKOUT_INVALID",
                    if(fixed) "PAYMENT_AVAILABLE" else "PAYMENT_BLOCKED").forEach {add(it)}})
                add("checkout",JsonObject().apply {addProperty("orientationBefore",1);addProperty("orientationAfter",2)
                    addProperty("isValid",fixed);if(fixed)addProperty("selectedPaymentMethod","UPI") else add("selectedPaymentMethod",JsonNull.INSTANCE)})
            }
            val execution=ExecutionResult("run",scenario.id,scenario.name,"device",if(fixed) ExecutionStatus.FAILED else ExecutionStatus.PASSED,
                ExecutionMode.ADB,"2026-10-03T00:00:00Z","2026-10-03T00:00:01Z",1000.0,
                scenario.steps.mapIndexed {i,s->ExecutionStepResult(i,s.action.name,s.target,StepStatus.PASSED,"","",1.0,"",JsonObject())},
                0,0,null,"ANDROID_CONFIGURATION",observed)
            return RunnerState(phase=ExecutionPhase.Completed,purpose=if(fixed) ExecutionPurpose.VERIFY_FIX else ExecutionPurpose.REPRODUCE,result=execution)
        }
        assertEquals("BUG REPRODUCED",executionTitle(result(false),scenario))
        val legacy=result(false)
        legacy.result!!.observed_state!!.getAsJsonObject("checkout").remove("selectedPaymentMethod")
        assertEquals("BUG REPRODUCED",executionTitle(legacy,scenario))
        val saved=com.pranay.reproai.data.remote.RunnerApiClient.gson.toJson(result(false).result)
        assertTrue(saved.contains("\"selectedPaymentMethod\":null"))
        assertEquals("FIX VERIFIED",executionTitle(result(true),scenario))
        val missing=result(false);missing.result!!.observed_state!!.getAsJsonObject("checkout").addProperty("orientationAfter",1)
        assertFalse(executionTitle(missing,scenario).contains("BUG REPRODUCED"))
        val report=com.pranay.reproai.report.IncidentReportBuilder.build(DebugSession("rotation",events=input().events),analysis,"report",1,
            listOf(ExecutionPurpose.REPRODUCE to result(false).result!!,ExecutionPurpose.VERIFY_FIX to result(true).result!!))
        assertEquals(com.pranay.reproai.report.ReportStatus.FIX_VERIFIED,report.status)
        val markdown=com.pranay.reproai.report.IncidentReportExporter.markdown(report)
        assertTrue(markdown.contains("CHECKOUT_STATE_LOST"));assertTrue(markdown.contains("CHECKOUT_STATE_RESTORED"))
        listOf("HTTP 401","TOKEN_EXPIRED","/payment").forEach {assertFalse(markdown.contains(it))}
    }
}
