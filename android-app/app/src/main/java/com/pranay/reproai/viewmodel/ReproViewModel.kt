package com.pranay.reproai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.pranay.reproai.ai.AnalysisInput
import com.pranay.reproai.ai.CaptureDescriptionDraft
import com.pranay.reproai.ai.AnalysisResult
import com.pranay.reproai.ai.BugAnalyzer
import com.pranay.reproai.ai.RuleBasedFallbackProvider
import com.pranay.reproai.data.local.ReproDatabase
import com.pranay.reproai.data.mock.MockData
import com.pranay.reproai.data.model.AIAnalysis
import com.pranay.reproai.data.model.DebugEvent
import com.pranay.reproai.data.model.DebugSession
import com.pranay.reproai.data.model.DeviceInfo
import com.pranay.reproai.data.model.ReproductionStep
import com.pranay.reproai.data.model.TestResultData
import com.pranay.reproai.data.model.TestScenario
import com.pranay.reproai.data.model.TestStepProgress
import com.pranay.reproai.data.model.TestStepStatus
import com.pranay.reproai.data.repository.DebugSessionRepository
import com.pranay.reproai.tracking.DebugSessionManager
import com.pranay.reproai.tracking.ReproTracker
import com.pranay.reproai.tracking.SessionExporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReproUiState(
    val recentSessions: List<DebugSession> = emptyList(),
    // Active Session
    val currentSession: DebugSession? = null,
    val isRecording: Boolean = false,
    val sessionTimerSeconds: Int = 0,
    val eventsCapturedCount: Int = 0,
    val currentScreenName: String = "Home",
    val networkType: String = "Wi-Fi",
    val appState: String = "Foreground",
    val orientationState: String = "Portrait",
    val memoryState: String = "Normal",
    // Describe Bug
    val captureDescription: CaptureDescriptionDraft = CaptureDescriptionDraft(),
    val isAnalyzing: Boolean = false,
    val isListeningVoice: Boolean = false,
    // AI Analysis
    val aiAnalysis: AIAnalysis = MockData.sampleAnalysis,
    val analysisResult: AnalysisResult? = null,
    // Timeline & Reproduction
    val timelineEvents: List<DebugEvent> = emptyList(),
    val reproductionSteps: List<ReproductionStep> = MockData.sampleReproductionSteps,
    val testScenario: TestScenario = MockData.sampleScenario,
    // Running Test
    val testSteps: List<TestStepProgress> = MockData.initialTestProgress,
    val isTestRunning: Boolean = false,
    // Test Result
    val testResult: TestResultData = MockData.bugReproducedResult,
    val isFixVerifiedMode: Boolean = false,
    val exportedJson: String? = null
) {
    val bugDescriptionText: String get() = captureDescription.text
}

class ReproViewModel(application: Application) : AndroidViewModel(application) {

    val runnerRepository = com.pranay.reproai.data.repository.ReproRunnerRepository(application)
    val runnerState = runnerRepository.state
    fun configureRunner(host: String, port: String) = runnerRepository.configure(host, port)
    fun testRunnerConnection() { viewModelScope.launch { runnerRepository.testConnection() } }
    fun cancelExecution() { viewModelScope.launch { runnerRepository.cancel() } }
    fun openSavedIncident(session: DebugSession, onLoaded: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
            val saved = db.incidentExecutionDao().get(session.id)
            val gson = com.pranay.reproai.data.remote.RunnerApiClient.gson
            val analysis = db.reportDao().analysis(session.id)?.let { gson.fromJson(it.analysisJson, AnalysisResult::class.java) }
                ?: saved?.let { gson.fromJson(it.analysisJson, AnalysisResult::class.java) }
            _uiState.update { it.copy(currentSession = session, analysisResult = analysis, timelineEvents = session.events,
                captureDescription = CaptureDescriptionDraft.restore(session.id, session.userDescription)) }
            if (saved != null) runnerRepository.restore(
                gson.fromJson(saved.resultJson, com.pranay.reproai.data.remote.dto.ExecutionResult::class.java).validated(),
                com.pranay.reproai.data.remote.dto.ExecutionPurpose.valueOf(saved.purpose))
            onLoaded(saved != null)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                runnerRepository.reportError("The saved incident result could not be loaded. Analyze and run this incident again.")
                onLoaded(true)
            }
        }
    }
    private val db = ReproDatabase.getInstance(application)
    val repository = DebugSessionRepository(db.debugSessionDao(), db.debugEventDao())
    val sessionManager = DebugSessionManager(application, repository)
    val bugAnalyzer = BugAnalyzer(repository)

    private val _uiState = MutableStateFlow(ReproUiState())
    val uiState: StateFlow<ReproUiState> = _uiState.asStateFlow()

    init {
        observeRecentSessions()
        observeActiveSession()
    }

    private fun observeRecentSessions() {
        viewModelScope.launch {
            repository.getRecentSessionsFlow().collectLatest { sessions ->
                _uiState.update { state ->
                    state.copy(recentSessions = sessions)
                }
            }
        }
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            sessionManager.currentSession.collectLatest { session ->
                _uiState.update { state ->
                    state.copy(
                        currentSession = session,
                        isRecording = session?.isActive ?: false,
                        networkType = session?.networkType ?: sessionManager.networkMonitor.networkState.value,
                        appState = session?.appState ?: sessionManager.lifecycleMonitor.appState.value
                    )
                }
            }
        }

        viewModelScope.launch {
            sessionManager.events.collectLatest { events ->
                _uiState.update { state ->
                    state.copy(
                        eventsCapturedCount = events.size,
                        timelineEvents = events
                    )
                }
            }
        }

        viewModelScope.launch {
            sessionManager.elapsedSeconds.collectLatest { elapsed ->
                _uiState.update { state ->
                    state.copy(sessionTimerSeconds = elapsed)
                }
            }
        }

        viewModelScope.launch {
            sessionManager.orientationMonitor.orientationState.collectLatest { orientation ->
                _uiState.update { state ->
                    state.copy(orientationState = orientation)
                }
            }
        }
    }

    fun startNewSession() {
        ReproTracker.trackAction("START DEBUG SESSION")
        val session = sessionManager.startSession()
        _uiState.update { state ->
            state.copy(
                currentSession = session,
                isRecording = true,
                sessionTimerSeconds = 0,
                eventsCapturedCount = 1,
                exportedJson = null,
                captureDescription = CaptureDescriptionDraft(sessionId = session.id),
                analysisResult = null
            )
        }
    }

    fun stopSession() {
        ReproTracker.trackAction("STOP DEBUG SESSION")
        sessionManager.stopSession()
        _uiState.update { state ->
            state.copy(isRecording = false)
        }
    }

    fun captureBug() {
        val session = sessionManager.currentSession.value ?: return
        if (!session.isActive || _uiState.value.currentSession?.id != session.id) return
        _uiState.update { state -> state.copy(captureDescription =
            state.captureDescription.initialize(session.id, sessionManager.events.value)) }
        ReproTracker.trackAction("CAPTURE BUG")
        sessionManager.captureBug(_uiState.value.bugDescriptionText)
    }

    fun trackScreenOpened(screenName: String) {
        _uiState.update { it.copy(currentScreenName = screenName) }
        if (_uiState.value.isRecording) {
            ReproTracker.trackScreen(screenName)
        }
    }

    fun simulatePaymentBug() {
        if (!_uiState.value.isRecording) {
            startNewSession()
        }
        sessionManager.simulatePaymentBug()
    }

    fun exportCurrentSessionJson(): String? {
        val session = _uiState.value.currentSession ?: return null
        val json = SessionExporter.exportToJson(session, _uiState.value.analysisResult)
        _uiState.update { it.copy(exportedJson = json) }
        return json
    }

    fun updateBugDescription(text: String) {
        _uiState.update { state ->
            state.copy(captureDescription = state.captureDescription.edit(text))
        }
    }

    fun toggleVoiceListening() {
        _uiState.update { state ->
            val nextVoice = !state.isListeningVoice
            state.copy(
                isListeningVoice = nextVoice
            )
        }
    }

    fun analyzeBug(onComplete: () -> Unit) {
        ReproTracker.trackAction("ANALYZE BUG")
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true) }

            val session = _uiState.value.currentSession
            val description = _uiState.value.bugDescriptionText

            val result = if (session != null) {
                bugAnalyzer.analyzeSessionBug(session, description)
            } else {
                val dummyInput = AnalysisInput(
                    sessionId = "mock_session",
                    bugCapturedAt = System.currentTimeMillis(),
                    userDescription = description,
                    deviceInfo = DeviceInfo(),
                    events = _uiState.value.timelineEvents
                )
                RuleBasedFallbackProvider().analyze(dummyInput)
            }

            if (session != null) {
                db.reportDao().saveAnalysis(com.pranay.reproai.data.local.entity.IncidentAnalysisEntity(session.id,
                    com.pranay.reproai.data.remote.RunnerApiClient.gson.toJson(result)))
                if (sessionManager.currentSession.value?.id == session.id)
                    sessionManager.updateIncidentDetails(session.id, result.issueTitle, description)
                else repository.updateSession(session.copy(userDescription = description, issueTitle = result.issueTitle))
            }
            _uiState.update { state ->
                state.copy(
                    analysisResult = result,
                    isAnalyzing = false
                )
            }

            onComplete()
        }
    }

    fun checkDemoReadiness() {
        if(_uiState.value.isTestRunning) return
        _uiState.update {it.copy(isTestRunning=true)}
        viewModelScope.launch {
            try {runnerRepository.checkReadiness(_uiState.value.analysisResult?.testScenario)}
            finally {_uiState.update {it.copy(isTestRunning=false)}}
        }
    }
    fun resetDemo(onReset: () -> Unit = {}) {
        if(!com.pranay.reproai.BuildConfig.DEBUG || _uiState.value.isTestRunning) return
        _uiState.update {it.copy(isTestRunning=true)}
        viewModelScope.launch {
            try {
            if(runnerRepository.resetDemo()) {
                if(_uiState.value.isRecording) sessionManager.stopSession()
                _uiState.update {it.copy(isRecording=false,analysisResult=null,isFixVerifiedMode=false)}
                onReset()
            }
            } finally {_uiState.update {it.copy(isTestRunning=false)}}
        }
    }
    fun prepareExecution(onReady: (Boolean) -> Unit) {
        if(_uiState.value.isTestRunning) return
        val scenario=_uiState.value.analysisResult?.testScenario
        if(scenario == null) {runnerRepository.reportError("Analyze an incident before running its scenario.");return}
        _uiState.update {it.copy(isTestRunning=true)}
        viewModelScope.launch {
            val readiness = try {runnerRepository.checkReadiness(scenario)} finally {_uiState.update {it.copy(isTestRunning=false)}}
            if(readiness?.ready == true) onReady(readiness.executionMode == com.pranay.reproai.data.remote.dto.ExecutionMode.MOCK)
        }
    }
    fun startTestRun(isVerifyingFix: Boolean = false, allowMock: Boolean = false, onFinished: () -> Unit) {
        if (_uiState.value.isTestRunning) return
        val analysis = _uiState.value.analysisResult
        val sessionId = _uiState.value.currentSession?.id
        _uiState.update { it.copy(isTestRunning = true, isFixVerifiedMode = isVerifyingFix) }
        viewModelScope.launch {
            try {
                if (analysis == null) {
                    runnerRepository.reportError("Analyze an incident before running its scenario.")
                } else {
                    val purpose = if (isVerifyingFix) com.pranay.reproai.data.remote.dto.ExecutionPurpose.VERIFY_FIX
                        else com.pranay.reproai.data.remote.dto.ExecutionPurpose.REPRODUCE
                    val result = runnerRepository.execute(analysis.testScenario, purpose, allowMock)
                    if (result != null && sessionId != null) {
                        val gson = com.pranay.reproai.data.remote.RunnerApiClient.gson
                        try {
                            db.withTransaction {
                                // Retain the previous real run before replacing the compatibility latest-result row.
                                db.incidentExecutionDao().get(sessionId)?.let { previous ->
                                    val old = gson.fromJson(previous.resultJson, com.pranay.reproai.data.remote.dto.ExecutionResult::class.java).validated()
                                    db.reportDao().saveExecution(com.pranay.reproai.data.local.entity.ExecutionHistoryEntity(
                                        old.execution_id,sessionId,previous.purpose,previous.resultJson,old.scenario_id,old.finished_at))
                                }
                                db.reportDao().saveExecution(com.pranay.reproai.data.local.entity.ExecutionHistoryEntity(
                                    result.execution_id,sessionId,purpose.name,gson.toJson(result),result.scenario_id,result.finished_at))
                                db.incidentExecutionDao().save(com.pranay.reproai.data.local.entity.IncidentExecutionEntity(
                                    sessionId, gson.toJson(analysis), gson.toJson(analysis.testScenario), gson.toJson(result), purpose.name))
                            }
                        } catch (e: Exception) {
                            if (e is kotlinx.coroutines.CancellationException) throw e
                            runnerRepository.reportStorageError()
                        }
                    }
                }
            } finally {
                _uiState.update { it.copy(isTestRunning = false) }
            }
            onFinished()
        }
    }

    fun resetToHome() {
        _uiState.update { state ->
            state.copy(
                testResult = MockData.bugReproducedResult,
                isFixVerifiedMode = false
            )
        }
    }
}
