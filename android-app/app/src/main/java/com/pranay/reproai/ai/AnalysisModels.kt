package com.pranay.reproai.ai

import com.pranay.reproai.data.model.DebugEvent
import com.pranay.reproai.data.model.DeviceInfo

data class AnalysisInput(
    val sessionId: String,
    val bugCapturedAt: Long,
    val userDescription: String,
    val deviceInfo: DeviceInfo,
    val events: List<DebugEvent>,
    val currentNetwork: String = "Unknown",
    val orientation: String = "Portrait",
    val appState: String = "Foreground"
)

enum class TestAction {
    OPEN_SCREEN,
    TAP,
    WAIT,
    CHANGE_NETWORK,
    BACKGROUND_APP,
    FOREGROUND_APP,
    ROTATE_DEVICE,
    ASSERT_VISIBLE,
    ASSERT_TEXT,
    ASSERT_API_STATUS,
    ASSERT_EVENT,
    CUSTOM
}

data class TestPrecondition(
    val type: String,
    val value: String
)

data class TestActionItem(
    val action: TestAction,
    val target: String? = null,
    val value: String? = null,
    val description: String? = null
)

data class TestAssertion(
    val type: String,
    val target: String? = null,
    val expected: String
)

data class TestScenario(
    val id: String,
    val name: String,
    val description: String,
    val preconditions: List<TestPrecondition> = emptyList(),
    val steps: List<TestActionItem> = emptyList(),
    val assertions: List<TestAssertion> = emptyList(),
    @com.google.gson.annotations.SerializedName("verification_assertions")
    val verificationAssertions: List<TestAssertion>? = emptyList(),
    @com.google.gson.annotations.SerializedName("execution_purpose")
    val executionPurpose: com.pranay.reproai.data.remote.dto.ExecutionPurpose = com.pranay.reproai.data.remote.dto.ExecutionPurpose.REPRODUCE
)

data class ReproductionStep(
    val order: Int,
    val action: String,
    val target: String? = null,
    val value: String? = null,
    val description: String? = null
)

data class FailureSequenceItem(
    val eventId: String,
    val title: String,
    val source: String,
    val timestamp: Long
)

data class AnalysisResult(
    val issueTitle: String,
    val summary: String,
    val probableTrigger: String,
    val probableCause: String,
    val confidence: Int,
    val relevantComponents: List<String>,
    val relevantEventIds: List<String>,
    val observedEvidence: List<String>,
    val failureSequence: List<FailureSequenceItem>,
    val reproductionSteps: List<ReproductionStep>,
    val expectedBehavior: String,
    val actualBehavior: String,
    val testScenario: TestScenario,
    val analysisSource: String = "AI"
)
