package com.pranay.reproai

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.*
import com.pranay.reproai.ai.TestAction
import com.pranay.reproai.ai.TestActionItem
import com.pranay.reproai.ai.TestScenario
import com.pranay.reproai.ai.AnalysisResult
import com.pranay.reproai.data.model.DebugSession
import com.pranay.reproai.data.remote.dto.*
import com.pranay.reproai.ui.screens.home.HomeScreen
import com.pranay.reproai.ui.screens.reproduction.ReproductionScreen
import com.pranay.reproai.ui.screens.running.RunnerExecutionScreen
import com.pranay.reproai.ui.theme.ReproAITheme
import com.google.gson.JsonObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** Isolated presentation fixtures; never written to Room or used by the production workflow. */
@RunWith(Parameterized::class)
class PhoneLayoutTest(private val width: Int, private val fontScale: Float) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name="{0}dp font={1}")
        fun widths() = listOf(arrayOf<Any>(360,1f),arrayOf<Any>(393,1f),arrayOf<Any>(430,1f),
            arrayOf<Any>(360,1.3f),arrayOf<Any>(393,1.3f),arrayOf<Any>(430,1.3f))
    }
    @get:Rule val rule = createComposeRule()
    private val longTitle = "Payment authentication failure while retrying checkout after a network transition with a long incident description"
    private fun render(content: @Composable () -> Unit) {
        rule.setContent {
            // Fit each dp viewport into the real phone width without changing device settings.
            val pixelWidth = LocalConfiguration.current.screenWidthDp * LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(pixelWidth/width,fontScale)) {
                ReproAITheme { Box(Modifier.requiredSize(width.dp,720.dp).testTag("viewport")) {content()} }
            }
        }
    }
    private fun insideViewport(text: String) {
        val bounds = rule.onNodeWithText(text).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val viewport = rule.onNodeWithTag("viewport").fetchSemanticsNode().boundsInRoot
        assertTrue("$text overflows right edge at $width",bounds.right <= viewport.right + 1f)
        assertTrue("$text overflows left edge",bounds.left >= viewport.left - 1f)
        assertTrue("$text overflows bottom",bounds.bottom <= viewport.bottom + 1f)
    }
    @Test fun longIncidentAndStartActionRemainReadable() {
        var started = false
        render {HomeScreen(recentSessions=listOf(DebugSession("fixture",issueTitle=longTitle)),
            onStartSession={started=true},onSessionSelected={})}
        insideViewport(longTitle)
        insideViewport("Start session")
        rule.onNodeWithText("Start session").performClick()
        rule.runOnIdle {assertTrue(started)}
    }
    @Test fun reproductionFooterStaysVisibleWhileLongStepsScroll() {
        var ran = false
        val scenario=TestScenario("fixture",longTitle,"",steps=List(25) {TestActionItem(TestAction.TAP,"A_VERY_LONG_TECHNICAL_TARGET_THAT_MUST_WRAP_WITHOUT_CLIPPING","",longTitle)})
        val analysis=AnalysisResult(longTitle,"","","",80,emptyList(),emptyList(),emptyList(),emptyList(),emptyList(),"Expected","Actual",scenario)
        render {ReproductionScreen(emptyList(),com.pranay.reproai.data.model.TestScenario("fixture",longTitle,"device",25,"Expected","Actual"),analysis,{ran=true})}
        insideViewport("Run reproduction")
        rule.onNode(hasScrollToIndexAction()).performScrollToIndex(20)
        insideViewport("Run reproduction")
        rule.onNodeWithText("Run reproduction").performClick()
        rule.runOnIdle {assertTrue(ran)}
    }
    @Test fun longExecutionTargetAndCancelRemainWithinPhone() {
        var cancelled=false
        val target="checkout_payment_authentication_retry_network_transition_target_very_long"
        val scenario=TestScenario("fixture",longTitle,"",steps=listOf(TestActionItem(TestAction.TAP,target)))
        val result=ExecutionResult("exec-layout-fixture","fixture",longTitle,"device-with-a-long-serial-address",ExecutionStatus.RUNNING,ExecutionMode.ADB,
            "2026-10-02T00:00:00Z","",0.0,listOf(ExecutionStepResult(0,"TAP",target,StepStatus.RUNNING,"","",0.0,"",JsonObject())),0,0,null)
        render {RunnerExecutionScreen(RunnerState(phase=ExecutionPhase.Running,result=result),scenario,{cancelled=true},{},{})}
        rule.onNodeWithText(target).performScrollTo()
        insideViewport(target)
        insideViewport("Cancel execution")
        rule.onNodeWithText("Cancel execution").performClick()
        rule.runOnIdle {assertTrue(cancelled)}
    }
    @Test fun analysisHighlightsFailureEvidenceAndKeepsActionReachable() {
        var opened=false
        val scenario=TestScenario("fixture",longTitle,"")
        val sequence=listOf("Screen: Checkout","PAY_BUTTON_CLICKED","PAYMENT_NETWORK_CHANGED","TOKEN_EXPIRED","API Response 401","PAYMENT_FAILED")
            .mapIndexed { i,title -> com.pranay.reproai.ai.FailureSequenceItem("event-$i",title,"DEMOSHOP",1L) }
        val analysis=AnalysisResult(longTitle,"","Transition",longTitle,90,emptyList(),sequence.map {it.eventId},emptyList(),sequence,emptyList(),"Expected","Actual",scenario)
        render {com.pranay.reproai.ui.screens.analysis.AiAnalysisScreen(
            com.pranay.reproai.data.model.AIAnalysis("",0,"","",emptyList()),analysis,{}, {opened=true})}
        rule.onNodeWithText("TOKEN_EXPIRED").performScrollTo()
        insideViewport("TOKEN_EXPIRED")
        insideViewport("Open reproduction")
        rule.onNodeWithText("Open reproduction").performClick()
        rule.runOnIdle {assertTrue(opened)}
    }

    @Test fun reportFooterAndExportMenuStayReachable() {
        val report = com.pranay.reproai.report.IncidentReportBuilder.build(
            DebugSession("layout-fixture",issueTitle=longTitle),null,"report-fixture",1)
        render {com.pranay.reproai.ui.screens.report.IncidentReportScreen(report,{})}
        insideViewport("Share report")
        insideViewport("Copy summary")
        insideViewport("Export")
        rule.onNodeWithText("Export").performClick()
        rule.onNodeWithText("Export JSON").assertIsDisplayed()
        rule.onNodeWithText("Export Markdown").assertIsDisplayed()
        rule.onNodeWithText("Export Text").assertIsDisplayed()
        rule.onNodeWithText("Cancel").performClick()
        rule.onNodeWithText("Copy summary").performClick()
        rule.onNodeWithText("Report summary copied").assertIsDisplayed()
    }
}
