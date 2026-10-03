package com.pranay.reproai.tracking

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkMonitor(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkState = MutableStateFlow(getCurrentNetworkType())
    val networkState: StateFlow<String> = _networkState.asStateFlow()

    private var onNetworkChangedListener: ((from: String, to: String) -> Unit)? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun startMonitoring(onNetworkChanged: (from: String, to: String) -> Unit) {
        this.onNetworkChangedListener = onNetworkChanged
        if (networkCallback != null) return

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val newType = getNetworkTypeFromCapabilities(connectivityManager.getNetworkCapabilities(network))
                updateState(newType)
            }

            override fun onLost(network: Network) {
                updateState("OFFLINE")
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                val newType = getNetworkTypeFromCapabilities(capabilities)
                updateState(newType)
            }
        }

        try {
            connectivityManager.registerNetworkCallback(request, networkCallback!!)
        } catch (e: Exception) {
            // Fallback gracefully
        }
    }

    fun stopMonitoring() {
        networkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                // Ignore unregister errors
            }
        }
        networkCallback = null
        onNetworkChangedListener = null
    }

    fun getCurrentNetworkType(): String {
        val activeNetwork = connectivityManager.activeNetwork ?: return "OFFLINE"
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return "OFFLINE"
        return getNetworkTypeFromCapabilities(caps)
    }

    private fun updateState(newType: String) {
        val oldType = _networkState.value
        if (oldType != newType) {
            _networkState.value = newType
            onNetworkChangedListener?.invoke(oldType, newType)
        }
    }

    private fun getNetworkTypeFromCapabilities(caps: NetworkCapabilities?): String {
        if (caps == null) return "OFFLINE"
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
            else -> "OTHER"
        }
    }
}
