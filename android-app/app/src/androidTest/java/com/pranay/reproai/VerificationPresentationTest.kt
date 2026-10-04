package com.pranay.reproai

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.pranay.reproai.ai.*
import com.pranay.reproai.data.remote.dto.*
import com.pranay.reproai.ui.screens.running.RunnerExecutionScreen
import com.pranay.reproai.ui.theme.ReproAITheme
import org.junit.Rule
import org.junit.Test

/** Presentation fixtures do not create incidents or execute the runner. */
class VerificationPresentationTest {
    @get:Rule val rule = createComposeRule()
    private val scenario = TestScenario("original", "Payment verification", "", steps=listOf(TestActionItem(TestAction.TAP,"PAY")),
        assertions=listOf(TestAssertion("ASSERT_API_STATUS","/payment","401")), verificationAssertions=VerificationProfile.payment)
    private fun render(passed: Boolean) {
        val observed=JsonObject().apply {
            addProperty("verified",true);addProperty("executionId","verify");addProperty("source","DEMOSHOP")
            addProperty("lastApiStatus",200);addProperty("paymentStatus","SUCCESS")
            add("events",JsonArray().apply {add("TOKEN_REFRESHED");add("PAYMENT_SUCCESS")})
        }
        val result=ExecutionResult("verify","original",scenario.name,"device",if(passed) ExecutionStatus.PASSED else ExecutionStatus.FAILED,
            ExecutionMode.ADB,"2026-10-04T00:00:00Z","2026-10-04T00:00:01Z",1000.0,
            listOf(ExecutionStepResult(0,"TAP","PAY",StepStatus.PASSED,"","",1.0,"",JsonObject())),
            if(passed) 3 else 2,if(passed) 0 else 1,null,"DEMOSHOP_DEMO_HOOK",observed,
            execution_purpose=ExecutionPurpose.VERIFY_FIX,product_outcome=if(passed) ProductOutcome.FIX_VERIFIED else ProductOutcome.VERIFICATION_FAILED)
        rule.setContent {ReproAITheme {RunnerExecutionScreen(RunnerState(phase=ExecutionPhase.Completed,
            purpose=ExecutionPurpose.VERIFY_FIX,result=result),scenario,{},{},{})}}
    }
    @Test fun healthyProfileShowsVerifiedAndSameScenario() {
        render(true)
        rule.onNodeWithText("Fix verified").assertIsDisplayed()
        rule.onNodeWithText("Same scenario passed with healthy-state evidence.").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Healthy-state assertions").assertIsDisplayed()
        rule.onNodeWithText("VERIFICATION FAILED").assertDoesNotExist()
    }
    @Test fun failedAssertionCannotBecomeVerifiedFromHealthySnapshot() {
        render(false)
        rule.onNodeWithText("VERIFICATION FAILED").assertIsDisplayed()
        rule.onNodeWithText("Fix verified").assertDoesNotExist()
    }
}
