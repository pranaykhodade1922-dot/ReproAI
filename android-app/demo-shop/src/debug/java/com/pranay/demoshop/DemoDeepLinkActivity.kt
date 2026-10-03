package com.pranay.demoshop

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class DemoDeepLinkActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent.data
        if (uri?.scheme == "reproai-demo" && uri.host in listOf("product", "cart", "checkout")) {
            ShopController.initialize(this)
            if (ShopController.state.value.paymentStatus != "PROCESSING") {
                ShopController.open(requireNotNull(uri.host))
                if (uri.getQueryParameter("simulateNetworkTransition") == "true") ShopController.transition(true)
                startActivity(Intent(this, MainActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }
        }
        finish()
    }
}
