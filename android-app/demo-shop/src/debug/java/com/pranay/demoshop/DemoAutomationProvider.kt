package com.pranay.demoshop

import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.*
import android.util.Base64
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Debug APK only. Accessible to authorized adb shell/root and our own process. */
class DemoAutomationProvider : ContentProvider() {
    override fun onCreate(): Boolean { ShopController.initialize(requireNotNull(context)); return true }
    override fun call(method: String, arg: String?, extras: Bundle?): Bundle {
        val uid = Binder.getCallingUid()
        check(uid == 2000 || uid == 0 || uid == android.os.Process.myUid()) { "ADB shell access required" }
        require(method in setOf("STATE", "RESET_DEMO", "RESET_PRESENTATION", "PRODUCT", "CART", "CHECKOUT", "ADD_TO_CART", "PROCEED_TO_CHECKOUT", "PAY", "ENABLE_DEMO_NETWORK_TRANSITION", "FIXED_AUTH", "BUGGY_AUTH", "SELECT_UPI", "SELECT_CARD", "FIXED_ROTATION", "BUGGY_ROTATION")) { "Unknown action" }
        var error: Throwable? = null
        val done = CountDownLatch(1)
        Handler(Looper.getMainLooper()).post {
            try {
                when (method) {
                    "RESET_DEMO" -> ShopController.reset(requireNotNull(arg))
                    "RESET_PRESENTATION" -> ShopController.resetPresentation()
                    "PRODUCT", "CART", "CHECKOUT" -> ShopController.open(method.lowercase())
                    "ADD_TO_CART" -> ShopController.addToCart()
                    "PROCEED_TO_CHECKOUT" -> ShopController.checkout()
                    "PAY" -> ShopController.pay()
                    "ENABLE_DEMO_NETWORK_TRANSITION" -> ShopController.transition(true)
                    "FIXED_AUTH" -> ShopController.fixedAuth(true)
                    "BUGGY_AUTH" -> ShopController.fixedAuth(false)
                    "SELECT_UPI" -> ShopController.selectPaymentMethod("UPI")
                    "SELECT_CARD" -> ShopController.selectPaymentMethod("CARD")
                    "FIXED_ROTATION" -> ShopController.fixedRotation(true)
                    "BUGGY_ROTATION" -> ShopController.fixedRotation(false)
                }
            } catch (t: Throwable) { error = t } finally { done.countDown() }
        }
        check(done.await(5, TimeUnit.SECONDS)) { "Demo action timed out" }
        error?.let { throw IllegalStateException(it.message ?: "Demo action failed") }
        return Bundle().apply {
            putString("data", Base64.encodeToString(ShopController.snapshot().toString().toByteArray(Charsets.UTF_8), Base64.NO_WRAP))
        }
    }
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = throw UnsupportedOperationException()
    override fun getType(uri: Uri): String = "application/json"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = throw UnsupportedOperationException()
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = throw UnsupportedOperationException()
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = throw UnsupportedOperationException()
}
