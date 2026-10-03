package com.pranay.reproai.data.mock

import com.pranay.reproai.data.model.AIAnalysis
import com.pranay.reproai.data.model.DebugEvent
import com.pranay.reproai.data.model.DebugEventType
import com.pranay.reproai.data.model.DebugSession
import com.pranay.reproai.data.model.ReproductionStep
import com.pranay.reproai.data.model.TestResultData
import com.pranay.reproai.data.model.TestScenario
import com.pranay.reproai.data.model.TestStepProgress
import com.pranay.reproai.data.model.TestStepStatus

object MockData {

    val recentSessions = listOf(
        DebugSession(
            id = "sess_01",
            title = "Payment Failure",
            status = "Bug Reproduced",
            category = "Network transition",
            timestamp = "10 mins ago"
        ),
        DebugSession(
            id = "sess_02",
            title = "Location Permission Error",
            status = "Fix Verified",
            category = "Permission",
            timestamp = "1 hour ago"
        ),
        DebugSession(
            id = "sess_03",
            title = "Checkout Screen Freeze",
            status = "Bug Reproduced",
            category = "UI Thread Lock",
            timestamp = "Yesterday"
        )
    )


    val sampleAnalysis = AIAnalysis(
        issueName = "Payment Failure",
        confidencePercentage = 91,
        probableTrigger = "Network transition during payment",
        likelyCause = "Authentication token was not refreshed after the network changed, causing the payment retry to reuse an expired token.",
        relevantComponents = listOf(
            "AuthenticationManager",
            "NetworkRetryInterceptor",
            "PaymentRepository"
        )
    )

    private val baseTime = System.currentTimeMillis() - 60000L

    val sampleTimelineEvents = listOf(
        DebugEvent(
            id = "evt_1",
            sessionId = "sess_01",
            timestamp = baseTime,
            elapsedMs = 0L,
            type = DebugEventType.USER_ACTION,
            title = "PAY button tapped",
            description = "User clicked on primary checkout payment action"
        ),
        DebugEvent(
            id = "evt_2",
            sessionId = "sess_01",
            timestamp = baseTime + 1000L,
            elapsedMs = 1000L,
            type = DebugEventType.NETWORK_CHANGED,
            title = "Wi-Fi disconnected",
            description = "Signal dropped below threshold, interface down"
        ),
        DebugEvent(
            id = "evt_3",
            sessionId = "sess_01",
            timestamp = baseTime + 2000L,
            elapsedMs = 2000L,
            type = DebugEventType.NETWORK_CHANGED,
            title = "Mobile data connected",
            description = "Network interface switched to 5G LTE"
        ),
        DebugEvent(
            id = "evt_4",
            sessionId = "sess_01",
            timestamp = baseTime + 2000L,
            elapsedMs = 2000L,
            type = DebugEventType.RETRY,
            title = "Payment retry triggered",
            description = "Automatic network failure retry policy initiated"
        ),
        DebugEvent(
            id = "evt_5",
            sessionId = "sess_01",
            timestamp = baseTime + 3000L,
            elapsedMs = 3000L,
            type = DebugEventType.ERROR,
            title = "HTTP 401 received",
            description = "Backend response: Unauthorized bearer token"
        ),
        DebugEvent(
            id = "evt_6",
            sessionId = "sess_01",
            timestamp = baseTime + 3000L,
            elapsedMs = 3000L,
            type = DebugEventType.ERROR,
            title = "TOKEN_EXPIRED",
            description = "Session auth token validation failure"
        ),
        DebugEvent(
            id = "evt_7",
            sessionId = "sess_01",
            timestamp = baseTime + 3000L,
            elapsedMs = 3000L,
            type = DebugEventType.ERROR,
            title = "Payment failed",
            description = "Checkout flow halted and error dialog displayed"
        )
    )

    val sampleReproductionSteps = listOf(
        ReproductionStep(1, "Open DemoShop", "Launch application in foreground"),
        ReproductionStep(2, "Add a product to cart", "Select item #1042 and proceed"),
        ReproductionStep(3, "Open Checkout", "Navigate to final payment screen"),
        ReproductionStep(4, "Connect to Wi-Fi", "Ensure active Wi-Fi connection"),
        ReproductionStep(5, "Tap PAY", "Initiate payment submit event"),
        ReproductionStep(6, "Disconnect Wi-Fi immediately", "Simulate rapid network change"),
        ReproductionStep(7, "Allow mobile data connection", "Fallback network connection active"),
        ReproductionStep(8, "Observe payment failure", "Verify unexpected HTTP 401 error")
    )

    val sampleScenario = TestScenario(
        id = "scen_01",
        title = "Payment Network Transition",
        connectedDevice = "iQOO Device",
        totalSteps = 8,
        expectedBehavior = "Payment should retry successfully.",
        actualBehavior = "Payment fails with HTTP 401."
    )

    val initialTestProgress = listOf(
        TestStepProgress("step_1", "Open Checkout", TestStepStatus.COMPLETED),
        TestStepProgress("step_2", "Tap PAY", TestStepStatus.COMPLETED),
        TestStepProgress("step_3", "Disconnect Wi-Fi", TestStepStatus.COMPLETED),
        TestStepProgress("step_4", "Switch Network", TestStepStatus.COMPLETED),
        TestStepProgress("step_5", "Retry Payment", TestStepStatus.RUNNING),
        TestStepProgress("step_6", "Verify Result", TestStepStatus.PENDING)
    )

    val bugReproducedResult = TestResultData(
        isBugReproduced = true,
        statusTitle = "BUG REPRODUCED",
        expectedText = "Payment Success",
        actualText = "HTTP 401 Unauthorized",
        durationText = "8.4 sec",
        detailsMessage = "The automated scenario reliably reproduced the authorization token expiration during network switch."
    )

    val fixVerifiedResult = TestResultData(
        isBugReproduced = false,
        statusTitle = "FIX VERIFIED",
        expectedText = "Payment Success",
        actualText = "Payment Success (HTTP 200 OK)",
        durationText = "5.2 sec",
        detailsMessage = "Previously failing scenario now passes successfully. Authentication token was refreshed automatically on network switch."
    )
}
