package com.pranay.reproai

import com.google.gson.JsonObject
import com.google.gson.JsonArray
import com.google.gson.Gson
import com.pranay.reproai.ai.*
import com.pranay.reproai.data.remote.dto.*
import com.pranay.reproai.ui.screens.running.executionTitle
import org.junit.Assert.*
import org.junit.Test

class ExecutionInterpretationTest {
    private val scenario = TestScenario("payment", "Payment failure", "", steps = listOf(TestActionItem(TestAction.TAP, "PAY")), assertions = listOf(TestAssertion("ASSERT_API_STATUS", "/payment", "401")))
    private fun state(purpose: ExecutionPurpose, fixed: Boolean = false, verified: Boolean = true, mode: ExecutionMode = ExecutionMode.ADB): RunnerState {
        val evidence = JsonObject().apply { addProperty("verified", verified) }
        val observed = JsonObject().apply {
            addProperty("verified", verified); addProperty("executionId", "exec"); addProperty("source", "DEMOSHOP")
            addProperty("lastApiStatus", if(fixed) 200 else 401); addProperty("paymentStatus", if(fixed) "SUCCESS" else "FAILED")
            add("events", JsonArray().apply { add(if(fixed) "TOKEN_REFRESHED" else "TOKEN_EXPIRED"); add(if(fixed) "PAYMENT_SUCCESS" else "PAYMENT_FAILED") })
        }
        return RunnerState(purpose = purpose, phase = ExecutionPhase.Completed, result = ExecutionResult(
            "exec", "payment", "Payment failure", "device", if(fixed) ExecutionStatus.FAILED else ExecutionStatus.PASSED, mode,
            "2026-10-02T00:00:00Z", "2026-10-02T00:00:01Z", 1000.0,
            listOf(ExecutionStepResult(0, "TAP", "PAY", StepStatus.PASSED, "", "", 1.0, "", evidence)),
            if(fixed) 0 else 1, if(fixed) 1 else 0, null, "DEMOSHOP_DEMO_HOOK", observed))
    }
    @Test fun mockPassCannotClaimProductOutcome() {
        for(purpose in ExecutionPurpose.entries) assertTrue(executionTitle(state(purpose, mode=ExecutionMode.MOCK), scenario).startsWith("MOCK EXECUTION"))
    }
    @Test fun measuredFullFailureSignatureProvesReproduction() {
        assertEquals("BUG REPRODUCED", executionTitle(state(ExecutionPurpose.REPRODUCE), scenario))
        assertFalse(executionTitle(state(ExecutionPurpose.VERIFY_FIX), scenario).contains("FIX VERIFIED"))
    }
    @Test fun actualFixedPathVerifiesSameFailureScenarioWithoutFakingAssertions() {
        val fixed = state(ExecutionPurpose.VERIFY_FIX, fixed=true)
        assertEquals(ExecutionStatus.FAILED, fixed.result!!.status)
        assertEquals("FIX VERIFIED", executionTitle(fixed, scenario))
    }
    @Test fun missingEventStaleScopeOrUnmeasuredStateCannotProveOutcome() {
        val missing = state(ExecutionPurpose.REPRODUCE)
        missing.result!!.observed_state!!.getAsJsonArray("events").remove(1)
        assertFalse(executionTitle(missing, scenario).contains("BUG REPRODUCED"))
        val stale = state(ExecutionPurpose.REPRODUCE)
        stale.result!!.observed_state!!.addProperty("executionId", "old-execution")
        assertFalse(executionTitle(stale, scenario).contains("BUG REPRODUCED"))
        assertFalse(executionTitle(state(ExecutionPurpose.REPRODUCE, verified=false), scenario).contains("BUG REPRODUCED"))
    }
    @Test fun singleVerifiedAssertionWithoutSnapshotCannotProveReproduction() {
        val current = state(ExecutionPurpose.REPRODUCE)
        assertFalse(executionTitle(current.copy(result=current.result!!.copy(observed_state=null)), scenario).contains("BUG REPRODUCED"))
    }
    @Test fun successWithoutRefreshCannotProveFixedRetry() {
        val current = state(ExecutionPurpose.VERIFY_FIX, fixed=true)
        current.result!!.observed_state!!.getAsJsonArray("events").remove(0)
        assertFalse(executionTitle(current, scenario).contains("FIX VERIFIED"))
    }
    @Test(expected = IllegalArgumentException::class) fun unknownExecutionModeIsRejectedBeforeUiRendering() {
        val gson = Gson()
        val json = gson.toJsonTree(state(ExecutionPurpose.REPRODUCE).result).asJsonObject
        json.addProperty("execution_mode", "UNKNOWN")
        gson.fromJson(json, ExecutionResult::class.java).validated()
    }
}
