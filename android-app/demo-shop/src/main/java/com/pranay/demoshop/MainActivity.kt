package com.pranay.demoshop

import android.os.Bundle
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pranay.reproai.sdk.ReproAI
import kotlinx.coroutines.launch

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ShopController.initialize(this)
        savedInstanceState?.let { ShopController.checkoutRecreated(it.getString("checkout_method"),
            it.getInt("checkout_orientation"),resources.configuration.orientation) }
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().safeDrawingPadding().semantics { testTagsAsResourceId = true },
                    color = Color(0xFF121212)
                ) {
                    Column {
                        if(BuildConfig.DEBUG) {
                            val mode by ShopController.state.collectAsState()
                            Text(if(mode.useFixedAuth) "FIXED MODE / DEMO_HOOK" else "BUGGY MODE / DEMO_HOOK",
                                color=Color.White,modifier=Modifier.padding(horizontal=16.dp,vertical=8.dp))
                        }
                        Box(Modifier.weight(1f)) {DemoShopNavHost()}
                    }
                }
            }
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("checkout_method",ShopController.state.value.selectedPaymentMethod)
        outState.putInt("checkout_orientation",resources.configuration.orientation)
        super.onSaveInstanceState(outState)
    }
}

object DemoShopRoutes {
    const val PRODUCT = "product"
    const val CART = "cart"
    const val CHECKOUT = "checkout"
    const val RESULT = "result"
}

@Composable
fun DemoShopNavHost() {
    val navController = rememberNavController()
    val shop by ShopController.state.collectAsState()
    LaunchedEffect(shop.screen,shop.executionId) {
        if (navController.currentDestination?.route != shop.screen) {
            navController.navigate(shop.screen) { popUpTo(DemoShopRoutes.PRODUCT); launchSingleTop = true }
        } else {
            // Reset clears the marker even when this destination is unchanged.
            // Confirm it from the actual composed navigation destination.
            ShopController.visible(shop.screen)
        }
    }

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            destination.route?.let { route ->
                ShopController.visible(route)
                val screenName = when (route) {
                    DemoShopRoutes.PRODUCT -> "Product"
                    DemoShopRoutes.CART -> "Cart"
                    DemoShopRoutes.CHECKOUT -> "Checkout"
                    DemoShopRoutes.RESULT -> "Payment Result"
                    else -> route
                }
                ReproAI.trackScreen(screenName)
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    NavHost(navController = navController, startDestination = DemoShopRoutes.PRODUCT) {
        composable(DemoShopRoutes.PRODUCT) {
            ProductScreen(
                onAddToCart = {
                    ShopController.addToCart()
                }
            )
        }

        composable(DemoShopRoutes.CART) {
            CartScreen(
                onProceedToCheckout = {
                    ShopController.checkout()
                }
            )
        }

        composable(DemoShopRoutes.CHECKOUT) {
            CheckoutScreen()
        }

        composable(DemoShopRoutes.RESULT) {
            PaymentResultScreen(
                isSuccess = shop.paymentStatus == "SUCCESS",
                message = shop.message,
                onReturnToShop = {
                    ShopController.open("product")
                }
            )
        }
    }
}

@Composable
fun ProductScreen(onAddToCart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "DEMOSHOP",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8)
        )
        Text(
            text = "Featured Product",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFF6366F1).copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Headset,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(56.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Pro Wireless Earbuds",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Active Noise Cancellation & 30hr Battery",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "₹2,499",
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onAddToCart,
            modifier = Modifier
                .testTag("product_add_to_cart").semantics { contentDescription = "product_add_to_cart" }
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "ADD TO CART", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun CartScreen(onProceedToCheckout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "YOUR CART",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8)
        )
        Text(
            text = "Cart Summary",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Pro Wireless Earbuds", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "Qty: 1", fontSize = 13.sp, color = Color.Gray)
                }
                Text(text = "₹2,499", fontSize = 18.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onProceedToCheckout,
            modifier = Modifier
                .testTag("cart_checkout").semantics { contentDescription = "cart_checkout" }
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
        ) {
            Text(text = "PROCEED TO CHECKOUT", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun CheckoutScreen() {
    val shop by ShopController.state.collectAsState()
    val isProcessing = shop.paymentStatus == "PROCESSING"
    val isSimulatedTransitionEnabled = shop.simulateNetworkTransition

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Keep payment reachable while the checkout/developer controls scroll.
        // MainActivity already applies safeDrawingPadding to the whole viewport.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "FINAL STEP",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )
            Text(
                text = "Checkout & Payment",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "ORDER TOTAL", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)
                    Text(text = "₹2,499", fontSize = 28.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Secured via ReproAI Event Tracked Payment Engine", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (BuildConfig.DEBUG) {
                Text("Checkout rotation state: ${if(shop.preserveCheckoutRotation) "FIXED" else "BUGGY"}",color=Color.White)
                Switch(checked=shop.preserveCheckoutRotation,onCheckedChange=ShopController::fixedRotation,
                    modifier=Modifier.testTag("checkout_rotation_fixed_toggle"))
                Text(if(shop.useFixedAuth) "FIXED MODE" else "BUGGY MODE", color = Color(0xFFEF4444))
                Text("DEMO_HOOK / fake payment service", color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Authentication retry behavior: ${if(shop.useFixedAuth) "FIXED" else "BUGGY"}", color = Color.White, modifier = Modifier.weight(1f))
                    Switch(checked = shop.useFixedAuth, onCheckedChange = { ShopController.fixedAuth(it) },
                        enabled = !isProcessing, modifier = Modifier.testTag("checkout_fixed_auth_toggle"))
                }
                OutlinedButton(onClick={ShopController.resetPresentation()},enabled=!isProcessing) {Text("RESET DEMO")}
            }
            Text("Payment method: ${shop.selectedPaymentMethod ?: "Not selected"}",color=Color.White,
                modifier=Modifier.testTag("checkout_payment_selected"))
            Row {
                OutlinedButton(onClick={ShopController.selectPaymentMethod("UPI")},modifier=Modifier.testTag("checkout_payment_upi")) {Text("UPI")}
                OutlinedButton(onClick={ShopController.selectPaymentMethod("CARD")},modifier=Modifier.testTag("checkout_payment_card")) {Text("Card")}
            }
            if(shop.checkoutStateLost) Text("Checkout invalid: payment method lost after rotation. Select a method to continue.",
                color=Color(0xFFEF4444),modifier=Modifier.testTag("checkout_invalid_state"))
            // Demo Controls Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1B2A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DEMO CONTROLS",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Simulate network transition during payment",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "Simulates Wi-Fi → Cellular token expiry bug",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            modifier = Modifier.testTag("checkout_demo_network_toggle").semantics { contentDescription = "checkout_demo_network_toggle" },
                            checked = isSimulatedTransitionEnabled,
                            onCheckedChange = { ShopController.transition(it) }
                        )
                    }
                }
            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { ShopController.pay() },
            enabled = !isProcessing && !shop.checkoutStateLost,
            modifier = Modifier
                .testTag("checkout_pay").semantics { contentDescription = "checkout_pay" }
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
        ) {
            if (isProcessing) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(text = "PAY ₹2,499", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun PaymentResultScreen(isSuccess: Boolean, message: String, onReturnToShop: () -> Unit) {
    val icon = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error
    val color = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .background(color.copy(alpha = 0.15f), androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(44.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (isSuccess) "Payment Successful" else "Payment Failed",
            modifier = Modifier.testTag(if (isSuccess) "payment_success" else "payment_failure")
                .semantics { contentDescription = if (isSuccess) "payment_success" else "payment_failure" },
            fontSize = 24.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = color
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = message,
            modifier = Modifier.testTag("payment_status").padding(horizontal = 16.dp),
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = onReturnToShop,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "RETURN TO DEMOSHOP", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color.White)
        }
    }
}
