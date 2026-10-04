package com.pranay.reproai.ai

import com.pranay.reproai.data.remote.dto.ExecutionPurpose

/** Positive expectations for existing deterministic rules, including saved pre-profile incidents. */
object VerificationProfile {
    val payment = listOf(
        TestAssertion("ASSERT_EVENT", "TOKEN_REFRESHED", "True"),
        TestAssertion("ASSERT_API_STATUS", "/payment", "200"),
        TestAssertion("ASSERT_EVENT", "PAYMENT_SUCCESS", "True")
    )
    val rotation = listOf("ORIENTATION_CHANGED", "CHECKOUT_RECREATED", "CHECKOUT_STATE_RESTORED",
        "CHECKOUT_VALID", "PAYMENT_AVAILABLE").map { TestAssertion("ASSERT_EVENT", it, "True") }

    fun resolve(scenario: TestScenario): List<TestAssertion> {
        scenario.verificationAssertions?.takeIf { it.isNotEmpty() }?.let { return it }
        // Migrate only known rule signatures; never regenerate the scenario or change actions.
        val assertions = scenario.assertions.toSet()
        return when {
            assertions.containsAll(setOf(TestAssertion("ASSERT_API_STATUS", "/payment", "401"),
                TestAssertion("ASSERT_EVENT", "TOKEN_EXPIRED", "True"),
                TestAssertion("ASSERT_EVENT", "PAYMENT_FAILED", "True"))) &&
                scenario.steps.any { it.action == TestAction.CHANGE_NETWORK } &&
                scenario.steps.any { it.action == TestAction.TAP && it.target == "PAY" } -> payment
            scenario.steps.any { it.action == TestAction.ROTATE_DEVICE } &&
                listOf("CHECKOUT_STATE_LOST", "CHECKOUT_INVALID", "PAYMENT_BLOCKED").all {
                    TestAssertion("ASSERT_EVENT", it, "True") in assertions } -> rotation
            else -> emptyList()
        }
    }

    fun request(scenario: TestScenario, purpose: ExecutionPurpose): TestScenario {
        val profile = resolve(scenario)
        require(purpose != ExecutionPurpose.VERIFY_FIX || profile.isNotEmpty()) {
            "This scenario has no healthy verification profile. Fix verification cannot be proven."
        }
        return scenario.copy(verificationAssertions = profile, executionPurpose = purpose)
    }
}
