package com.pranay.reproai.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pranay.reproai.ui.screens.analysis.AiAnalysisScreen
import com.pranay.reproai.ui.screens.describe.DescribeBugScreen
import com.pranay.reproai.ui.screens.home.HomeScreen
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

    NavHost(
        navController = navController,
        startDestination = AppRoutes.HOME,
        modifier = modifier
    ) {
        composable(AppRoutes.HOME) {
            HomeScreen(
                recentSessions = uiState.recentSessions,
                onStartSession = {
                    viewModel.startNewSession()
                    navController.navigate(AppRoutes.ACTIVE_SESSION)
                },
                onSessionSelected = { _ ->
                    navController.navigate(AppRoutes.ACTIVE_SESSION)
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
                onCaptureBug = {
                    navController.navigate(AppRoutes.DESCRIBE_BUG)
                },
                onStopSession = {
                    viewModel.stopSession()
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.DESCRIBE_BUG) {
            DescribeBugScreen(
                descriptionText = uiState.bugDescriptionText,
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
                onGenerateTest = {
                    navController.navigate(AppRoutes.RUNNING_TEST)
                    viewModel.startTestRun(isVerifyingFix = false) {
                        navController.navigate(AppRoutes.TEST_RESULT) {
                            popUpTo(AppRoutes.REPRODUCTION_STEPS) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(AppRoutes.RUNNING_TEST) {
            RunningTestScreen(
                testSteps = uiState.testSteps,
                connectedDevice = uiState.testScenario.connectedDevice,
                testName = uiState.testScenario.title
            )
        }

        composable(AppRoutes.TEST_RESULT) {
            TestResultScreen(
                result = uiState.testResult,
                onVerifyFix = {
                    navController.navigate(AppRoutes.RUNNING_TEST)
                    viewModel.startTestRun(isVerifyingFix = true) {
                        navController.navigate(AppRoutes.TEST_RESULT) {
                            popUpTo(AppRoutes.RUNNING_TEST) { inclusive = true }
                        }
                    }
                },
                onViewTimeline = {
                    navController.navigate(AppRoutes.FAILURE_TIMELINE)
                },
                onReturnHome = {
                    viewModel.resetToHome()
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}
