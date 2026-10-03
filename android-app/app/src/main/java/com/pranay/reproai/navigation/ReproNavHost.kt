package com.pranay.reproai.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.pranay.reproai.ui.screens.analysis.AiAnalysisScreen
import com.pranay.reproai.ui.screens.describe.DescribeBugScreen
import com.pranay.reproai.ui.screens.home.HomeScreen
import com.pranay.reproai.ui.screens.report.BugReportScreen
import com.pranay.reproai.ui.screens.reproduction.ReproductionScreen
import com.pranay.reproai.ui.screens.result.TestResultScreen
import com.pranay.reproai.ui.screens.running.RunningTestScreen
import com.pranay.reproai.ui.screens.session.ActiveSessionScreen
import com.pranay.reproai.ui.screens.timeline.TimelineScreen
import com.pranay.reproai.viewmodel.ReproViewModel

@Composable
fun ReproNavHost(
    viewModel: ReproViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val uiState by viewModel.uiState.collectAsState()
    val runnerState by viewModel.runnerState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(uiState.isTestRunning, runnerState.phase, runnerState.result?.execution_id, lifecycleOwner) {
        if(!uiState.isTestRunning && runnerState.phase in listOf(
                com.pranay.reproai.data.remote.dto.ExecutionPhase.Completed,
                com.pranay.reproai.data.remote.dto.ExecutionPhase.Error)) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if(navController.currentDestination?.route == AppRoutes.RUNNING_TEST) {
                    navController.navigate(AppRoutes.TEST_RESULT) {
                        popUpTo(AppRoutes.RUNNING_TEST) {inclusive=true}
                    }
                }
            }
        }
    }
    var pendingMockRun by remember {mutableStateOf<(() -> Unit)?>(null)}
    fun requestExecution(verify: Boolean) {
        viewModel.prepareExecution { mock ->
            val start: () -> Unit = {
                navController.navigate(AppRoutes.RUNNING_TEST)
                // Completion is observed by the current resumed composition:
                // a retained ViewModel must not call a destroyed NavController.
                viewModel.startTestRun(isVerifyingFix=verify,allowMock=mock) {}
            }
            if(mock) pendingMockRun=start else start()
        }
    }
    if(pendingMockRun != null) androidx.compose.material3.AlertDialog(onDismissRequest={pendingMockRun=null},
        title={androidx.compose.material3.Text("Mock runner enabled")},
        text={androidx.compose.material3.Text("This run is simulated and cannot prove reproduction or fix verification. Use ADB mode for the live demo.")},
        confirmButton={androidx.compose.material3.TextButton(onClick={val run=pendingMockRun;pendingMockRun=null;run?.invoke()}) {androidx.compose.material3.Text("Run mock explicitly")}},
        dismissButton={androidx.compose.material3.TextButton(onClick={pendingMockRun=null}) {androidx.compose.material3.Text("Cancel")}})

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            destination.route?.let { route ->
                val screenName = when (route) {
                    AppRoutes.HOME -> "Home"
                    AppRoutes.ACTIVE_SESSION -> "Active Session"
                    AppRoutes.DESCRIBE_BUG -> "Capture Issue"
                    AppRoutes.AI_ANALYSIS -> "Incident Analysis"
                    AppRoutes.FAILURE_TIMELINE -> "Failure Timeline"
                    AppRoutes.REPRODUCTION_STEPS -> "Reproduction Steps"
                    AppRoutes.RUNNING_TEST -> "Reproduction Run"
                    AppRoutes.TEST_RESULT -> "Test Result"
                    AppRoutes.BUG_REPORT -> "Bug Report"
                    else -> route
                }
                viewModel.trackScreenOpened(screenName)
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.HOME,
        modifier = modifier
    ) {
        composable(AppRoutes.HOME) {
            HomeScreen(
                runnerSettings = { com.pranay.reproai.ui.screens.running.RunnerSettings(runnerState, viewModel::configureRunner, viewModel::testRunnerConnection) },
                runnerStatus = runnerState.result?.let { "${it.execution_mode} ${it.status}" } ?: "None",
                recentSessions = uiState.recentSessions,
                onViewReport = { session -> navController.navigate(AppRoutes.report(session.id)) },
                demoSettings = {
                    if(com.pranay.reproai.BuildConfig.DEBUG) com.pranay.reproai.ui.screens.running.DemoSettings(
                        runnerState,uiState.isTestRunning,viewModel::checkDemoReadiness,{viewModel.resetDemo()})
                },
                summaries = com.pranay.reproai.ui.screens.home.rememberIncidentSummaries(
                    uiState.recentSessions.map { it.id }, runnerState.result?.execution_id),
                activeSession = uiState.currentSession?.takeIf { uiState.isRecording },
                connectionLabel = if (runnerState.connection == com.pranay.reproai.data.remote.dto.ConnectionStatus.Connected) "Ready" else runnerState.connection.name,
                onOpenActive = { navController.navigate(AppRoutes.ACTIVE_SESSION) },
                onStartSession = {
                    viewModel.startNewSession()
                    navController.navigate(AppRoutes.ACTIVE_SESSION)
                },
                onSessionSelected = { session ->
                    viewModel.openSavedIncident(session) { hasResult ->
                        navController.navigate(if (hasResult) AppRoutes.TEST_RESULT else AppRoutes.ACTIVE_SESSION)
                    }
                }
            )
        }

        composable(AppRoutes.ACTIVE_SESSION) {
            ActiveSessionScreen(
                timerSeconds = uiState.sessionTimerSeconds,
                eventsCount = uiState.eventsCapturedCount,
                currentScreen = uiState.currentScreenName,
                networkType = uiState.networkType,
                appState = uiState.appState,
                memoryState = uiState.memoryState,
                events = uiState.timelineEvents,
                sessionId = uiState.currentSession?.id,
                orientation = uiState.orientationState,
                recording = uiState.isRecording,
                onBack = { navController.popBackStack() },
                onCaptureBug = {
                    viewModel.captureBug()
                    navController.navigate(AppRoutes.DESCRIBE_BUG)
                },
                onStopSession = {
                    viewModel.stopSession()
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.HOME) { inclusive = true }
                    }
                },
                onSimulatePaymentBug = {
                    viewModel.simulatePaymentBug()
                    Toast.makeText(context, "Simulated Payment Bug Events Injected!", Toast.LENGTH_SHORT).show()
                },
                onExportJson = {
                    val json = viewModel.exportCurrentSessionJson()
                    if (json != null) {
                        Toast.makeText(context, "Session exported as JSON (${json.length} chars)", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "No active session to export", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        composable(AppRoutes.DESCRIBE_BUG) {
            DescribeBugScreen(
                descriptionText = uiState.bugDescriptionText,
                sessionId = uiState.currentSession?.id,
                events = uiState.timelineEvents,
                isAnalyzing = uiState.isAnalyzing,
                isListeningVoice = uiState.isListeningVoice,
                onDescriptionChanged = viewModel::updateBugDescription,
                onVoiceToggle = viewModel::toggleVoiceListening,
                onAnalyzeBug = {
                    viewModel.analyzeBug {
                        navController.navigate(AppRoutes.AI_ANALYSIS)
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoutes.AI_ANALYSIS) {
            AiAnalysisScreen(
                analysis = uiState.aiAnalysis,
                result = uiState.analysisResult,
                sessionId = uiState.currentSession?.id,
                onBack = { navController.popBackStack() },
                onCreateReport = { uiState.currentSession?.id?.let { navController.navigate(AppRoutes.report(it)) } },
                onViewTimeline = {
                    navController.navigate(AppRoutes.FAILURE_TIMELINE)
                },
                onGenerateReproduction = {
                    navController.navigate(AppRoutes.REPRODUCTION_STEPS)
                }
            )
        }

        composable(AppRoutes.FAILURE_TIMELINE) {
            TimelineScreen(
                events = uiState.timelineEvents,
                onBack = {
                    navController.popBackStack()
                },
                onGenerateReproduction = {
                    navController.navigate(AppRoutes.REPRODUCTION_STEPS)
                }
            )
        }

        composable(AppRoutes.REPRODUCTION_STEPS) {
            ReproductionScreen(
                steps = uiState.reproductionSteps,
                scenario = uiState.testScenario,
                analysisResult = uiState.analysisResult,
                busy = uiState.isTestRunning,
                runnerMessage = runnerState.message,
                onReconnect = viewModel::testRunnerConnection,
                onReset = if(com.pranay.reproai.BuildConfig.DEBUG) ({viewModel.resetDemo {navController.navigate(AppRoutes.HOME)}}) else null,
                onBack = { navController.popBackStack() },
                onGenerateTest = { requestExecution(false) }
            )
        }

        composable(AppRoutes.RUNNING_TEST) {
            com.pranay.reproai.ui.screens.running.RunnerExecutionScreen(
                runnerState, uiState.analysisResult?.testScenario,
                viewModel::cancelExecution, {}, { navController.navigate(AppRoutes.HOME) })
        }
        composable(AppRoutes.TEST_RESULT) {
            com.pranay.reproai.ui.screens.running.RunnerExecutionScreen(
                runnerState, uiState.analysisResult?.testScenario,
                viewModel::cancelExecution,
                { requestExecution(true) },
                { navController.navigate(AppRoutes.HOME) },
                onViewTimeline = { navController.navigate(AppRoutes.FAILURE_TIMELINE) },
                onCreateReport = { uiState.currentSession?.id?.let { navController.navigate(AppRoutes.report(it)) } },
                onRetry = {requestExecution(runnerState.purpose == com.pranay.reproai.data.remote.dto.ExecutionPurpose.VERIFY_FIX)},
                onReconnect = viewModel::testRunnerConnection,
                onReset = if(com.pranay.reproai.BuildConfig.DEBUG) ({viewModel.resetDemo {navController.navigate(AppRoutes.HOME)}}) else null)
        }

        composable(AppRoutes.BUG_REPORT) { entry ->
            BugReportScreen(
                sessionId = requireNotNull(entry.arguments?.getString("sessionId")),
                currentAnalysis = uiState.analysisResult.takeIf { uiState.currentSession?.id == entry.arguments?.getString("sessionId") },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
