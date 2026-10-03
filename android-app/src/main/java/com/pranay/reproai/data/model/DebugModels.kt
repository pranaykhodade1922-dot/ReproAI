package com.pranay.reproai.data.model

enum class DebugEventType {
    UI_ACTION,
    NETWORK_CHANGE,
    RETRY,
    ERROR,
    SYSTEM_EVENT
}

data class DebugSession(
    val id: String,
    val title: String,
    val status: String,
    val category: String,
    val timestamp: String,
    val durationSeconds: Int = 92,
    val eventsCaptured: Int = 47,
    val currentScreen: String = "Checkout",
    val networkType: String = "5G",
    val appState: String = "Foreground",
    val memoryStatus: String = "Normal"
)

data class DebugEvent(
    val id: String,
    val timestamp: String,
    val type: DebugEventType,
    val title: String,
    val description: String = ""
)

data class AIAnalysis(
    val issueName: String,
    val confidencePercentage: Int,
    val probableTrigger: String,
    val likelyCause: String,
    val relevantComponents: List<String>
)

data class ReproductionStep(
    val stepNumber: Int,
    val title: String,
    val description: String = ""
)

enum class TestStepStatus {
    COMPLETED,
    RUNNING,
    PENDING,
    FAILED
}

data class TestStepProgress(
    val id: String,
    val title: String,
    val status: TestStepStatus
)

data class TestScenario(
    val id: String,
    val title: String,
    val connectedDevice: String,
    val totalSteps: Int,
    val expectedBehavior: String,
    val actualBehavior: String
)

data class TestResultData(
    val isBugReproduced: Boolean,
    val statusTitle: String,
    val expectedText: String,
    val actualText: String,
    val durationText: String,
    val detailsMessage: String
)
