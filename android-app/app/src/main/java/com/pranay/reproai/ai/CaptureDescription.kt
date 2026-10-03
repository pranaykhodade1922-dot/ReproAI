package com.pranay.reproai.ai

import com.pranay.reproai.data.model.DebugEvent
import com.pranay.reproai.data.model.DebugEventType

object CapturedFailureEvidence {
    fun paymentFailure(events: List<DebugEvent>): Boolean =
        events.any { it.title == "TOKEN_EXPIRED" } &&
            events.any { it.title.contains("401") } &&
            events.any { it.title == "PAYMENT_FAILED" }
}

object CaptureDescription {
    const val placeholder = "Describe what went wrong"

    fun summarize(sessionId: String, events: List<DebugEvent>): String {
        val captured = events.filter { it.sessionId == sessionId }
        val titles = captured.map { it.title }.toSet()
        return when {
            captured.any { it.type == DebugEventType.ORIENTATION_CHANGED || it.title == "ORIENTATION_CHANGED" } &&
                titles.containsAll(listOf("CHECKOUT_RECREATED", "CHECKOUT_STATE_LOST", "CHECKOUT_INVALID")) ->
                "Checkout state was lost after device rotation, leaving the payment flow invalid."
            CapturedFailureEvidence.paymentFailure(captured) && "PAYMENT_RETRY" in titles ->
                "Payment failed during authentication retry with HTTP 401 after token expiry."
            else -> ""
        }
    }
}

/** Initialized at capture, never during composition or live event updates. */
data class CaptureDescriptionDraft(
    val sessionId: String? = null,
    val text: String = "",
    val initialized: Boolean = false
) {
    fun initialize(id: String, events: List<DebugEvent>): CaptureDescriptionDraft =
        if (sessionId == id && initialized) this
        else CaptureDescriptionDraft(id, CaptureDescription.summarize(id, events), true)

    fun edit(value: String): CaptureDescriptionDraft = copy(text = value, initialized = true)

    companion object {
        fun restore(id: String, description: String?) =
            CaptureDescriptionDraft(id, description.orEmpty(), description != null)
    }
}
