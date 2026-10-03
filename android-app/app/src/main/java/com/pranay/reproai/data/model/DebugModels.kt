package com.pranay.reproai.data.model

import com.pranay.reproai.ai.AnalysisResult

enum class DebugEventType {
    SESSION_STARTED,
    SESSION_STOPPED,
    SCREEN_OPENED,
    USER_ACTION,
    NETWORK_CONNECTED,
    NETWORK_DISCONNECTED,
    NETWORK_CHANGED,
    APP_FOREGROUND,
    APP_BACKGROUND,
    ORIENTATION_CHANGED,
    API_REQUEST,
    API_RESPONSE,
    ERROR,
    BUG_CAPTURED,
    CUSTOM,
    // Backward compatibility aliases
    UI_ACTION,
    RETRY,
    SYSTEM_EVENT
}

data class DeviceInfo(
    val manufacturer: String = "Unknown",
    val model: String = "Unknown Device",
    val androidVersion: String = "14",
    val sdkVersion: Int = 34,
    val appVersion: String = "1.0.0"
)

data class DebugSession(
    val id: String,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val isActive: Boolean = true,
    val issueTitle: String? = "Debug Session",
    val userDescription: String? = null,
    val bugCapturedAt: Long? = null,
    val deviceInfo: DeviceInfo = DeviceInfo(),
    val events: List<DebugEvent> = emptyList(),
    val analysisResult: AnalysisResult? = null,
    // UI presentation compatibility fields
    val title: String = issueTitle ?: "Debug Session",
    val status: String = if (bugCapturedAt != null) "Bug Captured" else if (isActive) "Recording" else "Completed",
    val category: String = "System",
    val timestamp: String = "Just now",
    val durationSeconds: Int = 0,
    val eventsCaptured: Int = events.size,
    val currentScreen: String = "Checkout",
    val networkType: String = "Wi-Fi",
    val appState: String = "Foreground",
    val memoryStatus: String = "Normal"
)

data class DebugEvent(
    val id: String,
    val sessionId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val elapsedMs: Long = 0L,
    val type: DebugEventType,
    val title: String,
    val description: String = "",
    val metadata: Map<String, String> = emptyMap()
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
