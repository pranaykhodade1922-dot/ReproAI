package com.pranay.demoshop

import android.content.Context
import com.pranay.reproai.sdk.ReproAI
import com.pranay.reproai.sdk.ReproEventType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ShopState(
    val screen: String = "product", val visibleScreen: String = "", val cartCount: Int = 0,
    val paymentStatus: String = "IDLE", val message: String = "",
    val simulateNetworkTransition: Boolean = false, val useFixedAuth: Boolean = false,
    val executionId: String = "manual-${UUID.randomUUID()}",
    val lastApiStatus: Int? = null, val events: List<String> = emptyList(),
    val selectedPaymentMethod: String? = null, val checkoutValid: Boolean = false,
    val checkoutStateLost: Boolean = false, val lastStateEvent: String? = null,
    val preserveCheckoutRotation: Boolean = false, val orientationBefore: Int? = null,
    val orientationAfter: Int? = null
)

/** Shared UI/payment state; the debug bridge invokes these same operations. */
object ShopController {
    private val mutable = MutableStateFlow(ShopState())
    val state: StateFlow<ShopState> = mutable.asStateFlow()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var payment: Job? = null
    private var context: Context? = null
    fun initialize(appContext: Context) {
        if (context != null) return
        context = appContext.applicationContext
        ReproAI.initialize(appContext)
        val preferences=appContext.getSharedPreferences("demo", Context.MODE_PRIVATE)
        mutable.update { it.copy(useFixedAuth = BuildConfig.DEBUG && preferences.getBoolean("fixed_auth", false),
            preserveCheckoutRotation=BuildConfig.DEBUG && preferences.getBoolean("fixed_rotation",false)) }
    }
    fun reset(executionId: String) {
        require(executionId.matches(Regex("[a-zA-Z0-9_-]{1,100}")))
        payment?.cancel()
        val previous = mutable.value
        mutable.value = ShopState(executionId = executionId, useFixedAuth = previous.useFixedAuth,
            preserveCheckoutRotation = previous.preserveCheckoutRotation)
    }
    fun resetPresentation() {
        check(BuildConfig.DEBUG) { "Debug build required" }
        reset("demo-${UUID.randomUUID()}")
        fixedAuth(false)
        fixedRotation(false)
    }
    fun open(screen: String) {
        require(screen in listOf("product", "cart", "checkout"))
        check(mutable.value.paymentStatus != "PROCESSING") { "Payment is in progress" }
        mutable.update { it.copy(screen = screen, cartCount = if (screen != "product") 1 else it.cartCount) }
        if (screen == "checkout") checkoutEvent("CHECKOUT_OPENED")
    }
    fun addToCart() {
        check(mutable.value.screen == "product") { "Product screen required" }
        ReproAI.trackAction("ADD_TO_CART")
        mutable.update { it.copy(cartCount = 1, screen = "cart") }
    }
    fun checkout() {
        check(mutable.value.screen == "cart" && mutable.value.cartCount > 0) { "Cart required" }
        ReproAI.trackAction("PROCEED_TO_CHECKOUT")
        open("checkout")
    }
    fun transition(enabled: Boolean) { mutable.update { it.copy(simulateNetworkTransition = enabled) } }
    fun visible(screen: String) { mutable.update { it.copy(visibleScreen = screen) } }
    fun fixedAuth(enabled: Boolean) {
        check(BuildConfig.DEBUG) { "Debug build required" }
        check(mutable.value.paymentStatus != "PROCESSING") { "Cannot change auth during payment" }
        context?.getSharedPreferences("demo", Context.MODE_PRIVATE)?.edit()?.putBoolean("fixed_auth", enabled)?.apply()
        mutable.update { it.copy(useFixedAuth = enabled) }
    }
    fun fixedRotation(enabled: Boolean) {
        check(BuildConfig.DEBUG)
        context?.getSharedPreferences("demo",Context.MODE_PRIVATE)?.edit()?.putBoolean("fixed_rotation",enabled)?.apply()
        mutable.update { it.copy(preserveCheckoutRotation = enabled) }
    }
    private fun checkoutEvent(name: String, metadata: Map<String,String> = emptyMap()) {
        mutable.update { it.copy(events=it.events+name) }
        ReproAI.trackEvent(ReproEventType.CUSTOM,name,name,metadata + mapOf(
            "executionId" to mutable.value.executionId,"simulated" to "false","mechanism" to "ANDROID_CONFIGURATION"))
    }
    fun selectPaymentMethod(method: String) {
        require(method in listOf("UPI","CARD"))
        check(mutable.value.screen == "checkout")
        mutable.update { it.copy(selectedPaymentMethod=method,checkoutValid=true,checkoutStateLost=false,lastStateEvent="PAYMENT_METHOD_SELECTED") }
        checkoutEvent("PAYMENT_METHOD_SELECTED",mapOf("method" to method))
        checkoutEvent("CHECKOUT_VALID");checkoutEvent("PAYMENT_AVAILABLE")
    }
    /** Called exclusively by the Activity after Android recreates it, never by the debug provider. */
    fun checkoutRecreated(previousMethod: String?, before: Int, after: Int) {
        if (mutable.value.screen != "checkout" || previousMethod == null || before == after) return
        mutable.update { it.copy(orientationBefore=before,orientationAfter=after) }
        checkoutEvent("ORIENTATION_CHANGED",mapOf("before" to before.toString(),"after" to after.toString()))
        checkoutEvent("CHECKOUT_RECREATED")
        val restored=mutable.value.preserveCheckoutRotation || !BuildConfig.DEBUG
        mutable.update { it.copy(selectedPaymentMethod=if(restored) previousMethod else null,
            checkoutValid=restored,checkoutStateLost=!restored,
            lastStateEvent=if(restored) "CHECKOUT_STATE_RESTORED" else "CHECKOUT_STATE_LOST") }
        checkoutEvent(if(restored) "CHECKOUT_STATE_RESTORED" else "CHECKOUT_STATE_LOST",
            mapOf("field" to "paymentMethod","previousValue" to previousMethod))
        checkoutEvent(if(restored) "CHECKOUT_VALID" else "CHECKOUT_INVALID")
        checkoutEvent(if(restored) "PAYMENT_AVAILABLE" else "PAYMENT_BLOCKED")
    }
    fun pay() {
        if(mutable.value.checkoutStateLost) { checkoutEvent("PAYMENT_BLOCKED");return }
        check(mutable.value.screen == "checkout" && mutable.value.paymentStatus != "PROCESSING") { "Idle Checkout required" }
        val run = mutable.value.executionId
        mutable.update { it.copy(paymentStatus = "PROCESSING", lastApiStatus = null, message = "", events = emptyList()) }
        payment = scope.launch {
            FakePaymentService.processPayment("WIFI", { "WIFI" }, false,
                onResult = { success, message ->
                    if (mutable.value.executionId == run) mutable.update {
                        it.copy(screen = "result", paymentStatus = if (success) "SUCCESS" else "FAILED", message = message)
                    }
                },
                transitionProvider = { mutable.value.simulateNetworkTransition },
                fixedAuthProvider = { mutable.value.useFixedAuth },
                executionId = run,
                onEvidence = { name, status ->
                    if (mutable.value.executionId == run) mutable.update {
                        it.copy(events = it.events + name, lastApiStatus = status ?: it.lastApiStatus)
                    }
                })
        }
    }
    fun snapshot(): JSONObject {
        val s = mutable.value
        return JSONObject().apply {
            put("executionId", s.executionId); put("screen", s.screen)
            put("visibleScreen", s.visibleScreen)
            put("cartCount", s.cartCount); put("paymentStatus", s.paymentStatus)
            put("lastApiStatus", s.lastApiStatus ?: JSONObject.NULL)
            put("events", JSONArray(s.events)); put("endpoint", if(s.lastApiStatus != null) "/payment" else JSONObject.NULL)
            put("simulateNetworkTransition", s.simulateNetworkTransition)
            put("useFixedAuth", s.useFixedAuth); put("source", "DEMOSHOP")
            put("simulated", true); put("network_strategy", if(s.orientationAfter != null) "ANDROID_CONFIGURATION" else "DEMOSHOP_DEMO_HOOK")
            put("checkout",JSONObject().apply {
                put("selectedPaymentMethod",s.selectedPaymentMethod ?: JSONObject.NULL)
                put("isValid",s.checkoutValid);put("lastStateEvent",s.lastStateEvent ?: JSONObject.NULL)
                put("preserveAfterRotation",s.preserveCheckoutRotation)
                put("orientationBefore",s.orientationBefore ?: JSONObject.NULL)
                put("orientationAfter",s.orientationAfter ?: JSONObject.NULL)
            })
        }
    }
}
