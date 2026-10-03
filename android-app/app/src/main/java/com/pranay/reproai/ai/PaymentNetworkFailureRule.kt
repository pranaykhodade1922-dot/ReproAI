package com.pranay.reproai.ai

import com.pranay.reproai.data.model.DebugEventType
import java.util.UUID

class PaymentNetworkFailureRule : IncidentAnalysisRule {

    override fun detect(input: AnalysisInput): Boolean = input.events.any { it.title == "TOKEN_EXPIRED" } && input.events.any { it.title.contains("401") } && input.events.any { it.title == "PAYMENT_FAILED" }
    override suspend fun analyze(input: AnalysisInput): AnalysisResult {
        val events = input.events

        // Check if DemoShop Payment Network Transition bug occurred
        val hasPayClicked = events.any { it.title.contains("PAY", ignoreCase = true) || it.title.contains("Payment", ignoreCase = true) }
        val hasNetworkChange = events.any { it.type == DebugEventType.NETWORK_CHANGED || it.title.contains("Wi-Fi disconnected", ignoreCase = true) || it.title.contains("Network changed", ignoreCase = true) }
        val hasTokenExpired = events.any { it.title.contains("TOKEN_EXPIRED", ignoreCase = true) || it.description.contains("token", ignoreCase = true) }
        val hasHttp401 = events.any { it.title.contains("401", ignoreCase = true) || it.description.contains("401", ignoreCase = true) }

        val demoTransition = events.any { it.metadata["source"] == "DEMOSHOP" && it.metadata["simulated"] == "true" }
        val relevantEventIds = events.map { it.id }

        if (detect(input)) {
            val failureSequence = events.map { event ->
                val source = when {
                    event.metadata["source"] == "DEMOSHOP" -> "DEMOSHOP"
                    event.type in listOf(DebugEventType.NETWORK_CHANGED, DebugEventType.ORIENTATION_CHANGED, DebugEventType.APP_FOREGROUND, DebugEventType.APP_BACKGROUND) -> "DEVICE"
                    else -> "REPROAI"
                }
                FailureSequenceItem(
                    eventId = event.id,
                    title = event.title,
                    source = source,
                    timestamp = event.timestamp
                )
            }

            val testScenario = TestScenario(
                id = "scen_${UUID.randomUUID().toString().take(8)}",
                name = "Payment Network Transition Failure",
                description = "On-device DemoShop test reproducing HTTP 401 with a deterministic simulated network transition",
                preconditions = listOf(
                    TestPrecondition("NETWORK", "WIFI"),
                    TestPrecondition("APP_STATE", "Checkout")
                ),
                steps = listOf(
                    TestActionItem(TestAction.OPEN_SCREEN, target = "Checkout", description = "Navigate to DemoShop Checkout"),
                    TestActionItem(TestAction.CHANGE_NETWORK, value = "CELLULAR", description = "Enable deterministic DemoShop network transition"),
                    TestActionItem(TestAction.TAP, target = "PAY", description = "Invoke the Checkout payment action"),
                    TestActionItem(TestAction.WAIT, value = "2500", description = "Wait for payment retry execution")
                ),
                assertions = listOf(
                    TestAssertion(type = "ASSERT_API_STATUS", target = "/payment", expected = "401"),
                    TestAssertion(type = "ASSERT_EVENT", target = "TOKEN_EXPIRED", expected = "True"),
                    TestAssertion(type = "ASSERT_EVENT", target = "PAYMENT_FAILED", expected = "True")
                )
            )

            return AnalysisResult(
                issueTitle = "Payment Failure During Network Transition",
                summary = if (demoTransition) "DemoShop simulated a transition and its fake payment service returned HTTP 401." else "The payment request failed with HTTP 401 after a recorded transition.",
                probableTrigger = if (demoTransition) "DemoShop simulated network transition during payment retry" else "Recorded network transition during payment request",
                probableCause = "Authentication token was not refreshed prior to the payment retry following the network transition, resulting in re-use of an expired token.",
                confidence = 91,
                relevantComponents = listOf(
                    "AuthenticationManager",
                    "NetworkRetryInterceptor",
                    "PaymentRepository"
                ),
                relevantEventIds = relevantEventIds,
                observedEvidence = events.map { "${it.type}: ${it.title}" },
                failureSequence = failureSequence,
                reproductionSteps = listOf(
                    ReproductionStep(1, "OPEN_SCREEN", "Checkout", null, "Open DemoShop Checkout"),
                    ReproductionStep(2, "CHANGE_NETWORK", "Demo transition", "CELLULAR", "Enable the deterministic DemoShop transition; physical radios remain unchanged"),
                    ReproductionStep(3, "TAP", "PAY", null, "Invoke the payment action"),
                    ReproductionStep(4, "WAIT", null, "2500", "Wait for the demo retry"),
                    ReproductionStep(5, "OBSERVE_FAILURE", "Payment Result", "HTTP_401", "Verify HTTP 401, TOKEN_EXPIRED and PAYMENT_FAILED")
                ),
                expectedBehavior = "Payment should automatically refresh the authentication token on network switch and complete successfully.",
                actualBehavior = "Payment fails with HTTP 401 Unauthorized due to re-use of expired bearer token.",
                testScenario = testScenario,
                analysisSource = "RuleBasedFallback"
            )
        } else {
            // General Fallback Analysis
            val failureSequence = events.map { event ->
                FailureSequenceItem(
                    eventId = event.id,
                    title = event.title,
                    source = event.metadata["source"] ?: "SYSTEM",
                    timestamp = event.timestamp
                )
            }

            val testScenario = TestScenario(
                id = "scen_gen_${UUID.randomUUID().toString().take(8)}",
                name = "General Debug Sequence",
                description = "General test scenario generated from observed debug events",
                preconditions = listOf(TestPrecondition("APP_STATE", "Foreground")),
                steps = listOf(
                    TestActionItem(TestAction.OPEN_SCREEN, target = "Home"),
                    TestActionItem(TestAction.WAIT, value = "1000")
                ),
                assertions = listOf(
                    TestAssertion(type = "ASSERT_VISIBLE", target = "MainScreen", expected = "True")
                )
            )

            return AnalysisResult(
                issueTitle = "Observed System Event Sequence",
                summary = "Captured ${events.size} trace events around bug capture time.",
                probableTrigger = if (events.isNotEmpty()) events.last().title else "User bug report",
                probableCause = "Event sequence captured during active monitoring window.",
                confidence = 25,
                relevantComponents = listOf("AppManager", "NetworkMonitor"),
                relevantEventIds = relevantEventIds,
                observedEvidence = events.map { "${it.type}: ${it.title}" },
                failureSequence = failureSequence,
                reproductionSteps = listOf(
                    ReproductionStep(1, "OPEN_APP", "ReproAI", null, "Open monitored application"),
                    ReproductionStep(2, "REPEAT_ACTIONS", "UI", null, "Perform recorded user actions in order"),
                    ReproductionStep(3, "VERIFY_STATE", "Screen", null, "Verify bug state occurs")
                ),
                expectedBehavior = "Application should operate without errors.",
                actualBehavior = "Bug reported by user during session.",
                testScenario = testScenario,
                analysisSource = "RuleBasedFallback"
            )
        }
    }
}

