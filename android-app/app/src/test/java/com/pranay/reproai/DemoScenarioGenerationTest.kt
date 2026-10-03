package com.pranay.reproai

import com.pranay.reproai.ai.*
import com.pranay.reproai.data.model.DebugEvent
import com.pranay.reproai.data.model.DebugEventType
import com.pranay.reproai.data.model.DeviceInfo
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class DemoScenarioGenerationTest {
    @Test fun capturedDemoFailureGeneratesReliableOrderingAndHonestEvidence() = runBlocking {
        val events = listOf("PAY_BUTTON_CLICKED", "TOKEN_EXPIRED", "API Response 401", "PAYMENT_FAILED").mapIndexed { i, title ->
            DebugEvent("event-$i", "session", type=DebugEventType.ERROR, title=title,
                metadata=mapOf("source" to "DEMOSHOP", "simulated" to "true"))
        }
        val result = RuleBasedFallbackProvider().analyze(AnalysisInput("session", 1, "payment failure", DeviceInfo(), events))
        val steps = result.testScenario.steps
        assertEquals(listOf(TestAction.OPEN_SCREEN, TestAction.CHANGE_NETWORK, TestAction.TAP, TestAction.WAIT), steps.map { it.action })
        assertEquals("PAY", steps[2].target)
        assertTrue(result.testScenario.assertions.any { it.target == "PAYMENT_FAILED" })
        assertTrue(result.probableTrigger.contains("simulated"))
        assertEquals(events.map { "${it.type}: ${it.title}" }, result.observedEvidence)
        assertTrue(result.reproductionSteps[1].description!!.contains("physical radios remain unchanged"))
    }
}
