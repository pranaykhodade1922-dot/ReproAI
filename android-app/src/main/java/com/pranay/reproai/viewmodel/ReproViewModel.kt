package com.pranay.reproai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pranay.reproai.data.mock.MockData
import com.pranay.reproai.data.model.AIAnalysis
import com.pranay.reproai.data.model.DebugEvent
import com.pranay.reproai.data.model.DebugSession
import com.pranay.reproai.data.model.ReproductionStep
import com.pranay.reproai.data.model.TestResultData
import com.pranay.reproai.data.model.TestScenario
import com.pranay.reproai.data.model.TestStepProgress
import com.pranay.reproai.data.model.TestStepStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReproUiState(
    val recentSessions: List<DebugSession> = MockData.recentSessions,
    // Active Session
    val isRecording: Boolean = true,
    val sessionTimerSeconds: Int = 92,
    val eventsCapturedCount: Int = 47,
    val currentScreenName: String = "Checkout",
    val networkType: String = "5G",
    val appState: String = "Foreground",
    val memoryState: String = "Normal",
    // Describe Bug
    val bugDescriptionText: String = MockData.defaultBugDescription,
    val isAnalyzing: Boolean = false,
    val isListeningVoice: Boolean = false,
    // AI Analysis
    val aiAnalysis: AIAnalysis = MockData.sampleAnalysis,
    // Timeline & Reproduction
    val timelineEvents: List<DebugEvent> = MockData.sampleTimelineEvents,
    val reproductionSteps: List<ReproductionStep> = MockData.sampleReproductionSteps,
    val testScenario: TestScenario = MockData.sampleScenario,
    // Running Test
    val testSteps: List<TestStepProgress> = MockData.initialTestProgress,
    val isTestRunning: Boolean = false,
    // Test Result
    val testResult: TestResultData = MockData.bugReproducedResult,
    val isFixVerifiedMode: Boolean = false
)

class ReproViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReproUiState())
    val uiState: StateFlow<ReproUiState> = _uiState.asStateFlow()

    init {
        startTimerLoop()
    }

    private fun startTimerLoop() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_uiState.value.isRecording) {
                    _uiState.update { state ->
                        state.copy(sessionTimerSeconds = state.sessionTimerSeconds + 1)
                    }
                }
            }
        }
    }

    fun startNewSession() {
        _uiState.update { state ->
            state.copy(
                isRecording = true,
                sessionTimerSeconds = 1,
                eventsCapturedCount = 12
            )
        }
    }

    fun stopSession() {
        _uiState.update { state ->
            state.copy(isRecording = false)
        }
    }

    fun updateBugDescription(text: String) {
        _uiState.update { state ->
            state.copy(bugDescriptionText = text)
        }
    }

    fun toggleVoiceListening() {
        _uiState.update { state ->
            val nextVoice = !state.isListeningVoice
            state.copy(
                isListeningVoice = nextVoice,
                bugDescriptionText = if (nextVoice) {
                    "Payment failed right when Wi-Fi disconnected during payment authorization."
                } else {
                    state.bugDescriptionText
                }
            )
        }
    }

    fun analyzeBug(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true) }
            delay(1500) // Mock AI Processing Delay
            _uiState.update { it.copy(isAnalyzing = false) }
            onComplete()
        }
    }

    fun startTestRun(isVerifyingFix: Boolean = false, onFinished: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isTestRunning = true,
                    isFixVerifiedMode = isVerifyingFix,
                    testSteps = listOf(
                        TestStepProgress("step_1", "Open Checkout", TestStepStatus.COMPLETED),
                        TestStepProgress("step_2", "Tap PAY", TestStepStatus.COMPLETED),
                        TestStepProgress("step_3", "Disconnect Wi-Fi", TestStepStatus.COMPLETED),
                        TestStepProgress("step_4", "Switch Network", TestStepStatus.COMPLETED),
                        TestStepProgress("step_5", "Retry Payment", TestStepStatus.RUNNING),
                        TestStepProgress("step_6", "Verify Result", TestStepStatus.PENDING)
                    )
                )
            }

            delay(1200)
            _uiState.update { state ->
                state.copy(
                    testSteps = state.testSteps.map {
                        if (it.id == "step_5") it.copy(status = TestStepStatus.COMPLETED)
                        else if (it.id == "step_6") it.copy(status = TestStepStatus.RUNNING)
                        else it
                    }
                )
            }

            delay(1200)
            _uiState.update { state ->
                state.copy(
                    isTestRunning = false,
                    testSteps = state.testSteps.map { it.copy(status = TestStepStatus.COMPLETED) },
                    testResult = if (isVerifyingFix) MockData.fixVerifiedResult else MockData.bugReproducedResult
                )
            }

            onFinished()
        }
    }

    fun resetToHome() {
        _uiState.update { state ->
            state.copy(
                isRecording = true,
                sessionTimerSeconds = 92,
                eventsCapturedCount = 47,
                bugDescriptionText = MockData.defaultBugDescription,
                testResult = MockData.bugReproducedResult,
                isFixVerifiedMode = false
            )
        }
    }
}
