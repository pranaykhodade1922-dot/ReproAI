package com.pranay.reproai.tracking

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppLifecycleMonitor : DefaultLifecycleObserver {

    private val _appState = MutableStateFlow("Foreground")
    val appState: StateFlow<String> = _appState.asStateFlow()

    private var onLifecycleChangedListener: ((state: String) -> Unit)? = null
    private var isListening = false

    fun startMonitoring(onLifecycleChanged: (state: String) -> Unit) {
        this.onLifecycleChangedListener = onLifecycleChanged
        if (!isListening) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
            isListening = true
        }
    }

    fun stopMonitoring() {
        if (isListening) {
            ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
            isListening = false
        }
        onLifecycleChangedListener = null
    }

    override fun onStart(owner: LifecycleOwner) {
        _appState.value = "Foreground"
        onLifecycleChangedListener?.invoke("Foreground")
    }

    override fun onStop(owner: LifecycleOwner) {
        _appState.value = "Background"
        onLifecycleChangedListener?.invoke("Background")
    }
}
