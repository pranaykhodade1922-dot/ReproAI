package com.pranay.reproai

import com.google.gson.Gson
import com.pranay.reproai.ai.*
import com.pranay.reproai.data.remote.dto.ExecutionPurpose
import org.junit.Assert.*
import org.junit.Test

class VerificationProfileTest {
    private val original = TestScenario("original", "Payment", "", steps=listOf(
        TestActionItem(TestAction.OPEN_SCREEN,"Checkout"), TestActionItem(TestAction.CHANGE_NETWORK,value="CELLULAR"),
        TestActionItem(TestAction.TAP,"PAY")), assertions=listOf(
        TestAssertion("ASSERT_API_STATUS","/payment","401"),TestAssertion("ASSERT_EVENT","TOKEN_EXPIRED","True"),
        TestAssertion("ASSERT_EVENT","PAYMENT_FAILED","True")))
    @Test fun sameScenarioActionsAndFailureProfileRemainUnchanged() {
        val reproduce=VerificationProfile.request(original,ExecutionPurpose.REPRODUCE)
        val verify=VerificationProfile.request(original,ExecutionPurpose.VERIFY_FIX)
        assertEquals(original.id,verify.id);assertSame(original.steps,verify.steps)
        assertSame(original.preconditions,verify.preconditions);assertSame(original.assertions,verify.assertions)
        assertEquals(reproduce.copy(executionPurpose=ExecutionPurpose.VERIFY_FIX),verify)
        assertEquals(VerificationProfile.payment,verify.verificationAssertions)
        assertEquals(ExecutionPurpose.REPRODUCE,original.executionPurpose)
    }
    @Test fun legacySavedScenarioMigratesWithoutRegeneration() {
        val gson=Gson()
        val json=gson.toJsonTree(original).asJsonObject.apply {remove("verification_assertions");remove("execution_purpose")}
        val saved=gson.fromJson(json,TestScenario::class.java)
        val request=VerificationProfile.request(saved,ExecutionPurpose.VERIFY_FIX)
        assertEquals(original.id,request.id);assertEquals(original.steps,request.steps)
        assertEquals("VERIFY_FIX",gson.toJsonTree(request).asJsonObject.get("execution_purpose").asString)
        assertEquals(3,gson.toJsonTree(request).asJsonObject.getAsJsonArray("verification_assertions").size())
    }
    @Test(expected=IllegalArgumentException::class) fun unknownScenarioCannotVerifyFromErrorAbsence() {
        VerificationProfile.request(original.copy(assertions=emptyList()),ExecutionPurpose.VERIFY_FIX)
    }
}
