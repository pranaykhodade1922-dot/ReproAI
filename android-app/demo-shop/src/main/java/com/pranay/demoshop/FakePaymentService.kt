package com.pranay.demoshop

import com.pranay.reproai.sdk.ReproAI
import com.pranay.reproai.sdk.ReproEventType
import kotlinx.coroutines.delay

/** A fake payment service executed on the phone, with buggy and fixed retry paths. */
object FakePaymentService {
    suspend fun processPayment(
        initialNetwork: String,
        currentNetworkProvider: () -> String,
        isSimulatedTransitionEnabled: Boolean,
        onResult: (Boolean, String) -> Unit,
        transitionProvider: () -> Boolean = { isSimulatedTransitionEnabled },
        fixedAuthProvider: () -> Boolean = { false },
        executionId: String = "manual",
        onEvidence: (String, Int?) -> Unit = { _, _ -> }
    ) {
        fun emit(name: String, type: ReproEventType, status: Int? = null) {
            val metadata = mutableMapOf("source" to "DEMOSHOP", "simulated" to "true",
                "execution_id" to executionId, "endpoint" to "/payment",
                "network_strategy" to "DEMOSHOP_DEMO_HOOK")
            if (status != null) metadata["statusCode"] = status.toString()
            if (name == "TOKEN_EXPIRED") metadata["errorCode"] = name
            ReproAI.trackEvent(type, name, "Demo payment: $name", metadata)
            onEvidence(name, status)
        }
        emit("PAY_BUTTON_CLICKED", ReproEventType.USER_ACTION)
        emit("API Request POST /payment", ReproEventType.API_REQUEST)
        delay(1500)
        val changed = transitionProvider() || initialNetwork != currentNetworkProvider()
        if (changed) {
            emit("PAYMENT_NETWORK_CHANGED", ReproEventType.STATE_CHANGE)
            delay(150)
            emit("PAYMENT_RETRY", ReproEventType.CUSTOM)
            delay(150)
            if (fixedAuthProvider()) {
                // The fixed implementation refreshes the demo token before retry.
                emit("TOKEN_REFRESHED", ReproEventType.CUSTOM)
                emit("API Response 200", ReproEventType.API_RESPONSE, 200)
                emit("PAYMENT_SUCCESS", ReproEventType.STATE_CHANGE)
                onResult(true, "HTTP 200: token refreshed before payment retry.")
            } else {
                emit("TOKEN_EXPIRED", ReproEventType.ERROR)
                emit("API Response 401", ReproEventType.API_RESPONSE, 401)
                emit("PAYMENT_FAILED", ReproEventType.STATE_CHANGE)
                onResult(false, "HTTP 401 Unauthorized: expired token reused during demo network transition.")
            }
        } else {
            emit("API Response 200", ReproEventType.API_RESPONSE, 200)
            emit("PAYMENT_SUCCESS", ReproEventType.STATE_CHANGE)
            onResult(true, "Payment successful (demo service).")
        }
    }
}
