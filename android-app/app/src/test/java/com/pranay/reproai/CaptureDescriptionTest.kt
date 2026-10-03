package com.pranay.reproai

import com.pranay.reproai.ai.*
import com.pranay.reproai.data.model.*
import com.pranay.reproai.report.IncidentReportBuilder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CaptureDescriptionTest {
    private fun events(vararg titles: String, sessionId: String = "session") =
        titles.mapIndexed { index, title -> DebugEvent("e$index", sessionId,
            type = if (title == "ORIENTATION_CHANGED") DebugEventType.ORIENTATION_CHANGED else DebugEventType.CUSTOM,
            title = title, metadata = mapOf("source" to "DEMOSHOP")) }

    private val payment = events("PAYMENT_RETRY", "TOKEN_EXPIRED", "API Response 401", "PAYMENT_FAILED")

    @Test fun paymentSummaryUsesObservedEvidenceAndStillAnalyzes() = runBlocking {
        val text = CaptureDescription.summarize("session", payment)
        assertEquals("Payment failed during authentication retry with HTTP 401 after token expiry.", text)
        assertFalse(text.contains("Wi-Fi"))
        assertFalse(text.contains("mobile data"))
        val analysis = RuleBasedFallbackProvider().analyze(AnalysisInput("session", 0, text, DeviceInfo(), payment))
        assertEquals("Payment Failure During Network Transition", analysis.issueTitle)
        assertEquals(3, analysis.testScenario.assertions.size)
    }

    @Test fun rotationSummaryNeedsRotationRecreationAndInvalidLostState() {
        val rotation = events("ORIENTATION_CHANGED", "CHECKOUT_RECREATED", "CHECKOUT_STATE_LOST", "CHECKOUT_INVALID")
        assertEquals("Checkout state was lost after device rotation, leaving the payment flow invalid.",
            CaptureDescription.summarize("session", rotation))
        assertEquals("", CaptureDescription.summarize("session", rotation.dropLast(1)))
    }

    @Test fun unknownIncompleteAndOtherSessionEvidenceRemainEmpty() {
        assertEquals("", CaptureDescription.summarize("session", events("Screen: Checkout")))
        assertEquals("", CaptureDescription.summarize("session", payment.drop(1)))
        assertEquals("", CaptureDescription.summarize("other", payment))
        assertEquals("Describe what went wrong", CaptureDescription.placeholder)
    }

    @Test fun userEditsAndDeliberatelyEmptyTextSurviveRepeatedCaptureAndEventChanges() {
        val generated = CaptureDescriptionDraft().initialize("session", payment)
        val edited = generated.edit("Retry failed on checkout")
        assertEquals(edited, edited.initialize("session", emptyList()))
        assertEquals("", edited.edit("").initialize("session", payment).text)
        assertEquals("", edited.initialize("new-session", payment).text)
    }

    @Test fun unknownDraftIsNotRegeneratedByLaterEvents() {
        val draft = CaptureDescriptionDraft().initialize("session", emptyList())
        assertEquals("", draft.initialize("session", payment).text)
    }

    @Test fun savedUserDescriptionIsRestoredAndRetainedInReports() {
        val restored = CaptureDescriptionDraft.restore("session", "My observed checkout failure")
            .initialize("session", payment)
        val report = IncidentReportBuilder.build(DebugSession("session", userDescription = restored.text),
            null, "report", 0, emptyList())
        assertEquals("My observed checkout failure", report.description)
        assertEquals("", CaptureDescriptionDraft.restore("session", "").initialize("session", payment).text)
    }
}
