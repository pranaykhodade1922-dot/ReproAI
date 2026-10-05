package com.pranay.demoshop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** Real checkout controls in constrained viewports; no coordinates or radio changes. */
@RunWith(Parameterized::class)
class CheckoutLayoutTest(private val width: Int, private val height: Int, private val fontScale: Float) {
    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}x{1}dp font={2}")
        fun viewports() = listOf(360, 393, 430).flatMap { width ->
            listOf(arrayOf<Any>(width, 640, 1f), arrayOf<Any>(width, 480, 1.3f))
        }
    }

    @get:Rule val rule = createComposeRule()

    private fun insideViewport(tag: String) {
        val bounds = rule.onNodeWithTag(tag).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val viewport = rule.onNodeWithTag("viewport").fetchSemanticsNode().boundsInRoot
        assertTrue("$tag extends outside $width x $height viewport", viewport.left <= bounds.left + 1f &&
            viewport.top <= bounds.top + 1f && viewport.right + 1f >= bounds.right &&
            viewport.bottom + 1f >= bounds.bottom)
    }

    @Test fun payStaysVisibleWhilePaymentAndDeveloperControlsScroll() {
        rule.runOnUiThread {
            ShopController.initialize(InstrumentationRegistry.getInstrumentation().targetContext)
            ShopController.resetPresentation()
            ShopController.addToCart()
            ShopController.checkout()
        }
        rule.setContent {
            val pixelWidth = LocalConfiguration.current.screenWidthDp * LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(pixelWidth / width, fontScale)) {
                MaterialTheme {
                    Box(Modifier.requiredSize(width.dp, height.dp).testTag("viewport")) { CheckoutScreen() }
                }
            }
        }
        insideViewport("checkout_pay")
        for (tag in listOf("checkout_rotation_fixed_toggle", "checkout_fixed_auth_toggle",
            "checkout_payment_upi", "checkout_demo_network_toggle")) {
            rule.onNodeWithTag(tag).performScrollTo()
            insideViewport(tag)
            insideViewport("checkout_pay")
        }
        rule.onNodeWithTag("checkout_payment_upi").performScrollTo().performClick()
        rule.onNodeWithTag("checkout_demo_network_toggle").performScrollTo().performClick()
        rule.runOnIdle {
            assertEquals("UPI", ShopController.state.value.selectedPaymentMethod)
            assertTrue(ShopController.state.value.simulateNetworkTransition)
        }
        rule.onNodeWithTag("checkout_pay").assertIsEnabled().performClick()
        rule.waitUntil(10_000) { ShopController.state.value.paymentStatus == "FAILED" }
        rule.runOnIdle {
            assertEquals(401, ShopController.state.value.lastApiStatus)
            assertTrue(ShopController.state.value.events.containsAll(listOf(
                "PAY_BUTTON_CLICKED", "PAYMENT_RETRY", "TOKEN_EXPIRED", "PAYMENT_FAILED")))
            ShopController.resetPresentation()
        }
    }
}
