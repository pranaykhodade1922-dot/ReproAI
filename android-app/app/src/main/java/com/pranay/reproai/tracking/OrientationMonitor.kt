package com.pranay.reproai.tracking

import android.content.Context
import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.Surface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrientationMonitor(private val context: Context) {

    private val _orientationState = MutableStateFlow("PORTRAIT")
    val orientationState: StateFlow<String> = _orientationState.asStateFlow()

    private var onOrientationChangedListener: ((from: String, to: String) -> Unit)? = null
    private val displays=context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    private val displayListener=object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {}
        override fun onDisplayRemoved(displayId: Int) {}
        override fun onDisplayChanged(displayId: Int) {
            if(displayId==Display.DEFAULT_DISPLAY) updateType(getCurrentOrientation())
        }
    }
    private val callbacks=object : ComponentCallbacks {
        override fun onConfigurationChanged(newConfig: Configuration) {updateOrientation(newConfig)}
        override fun onLowMemory() {}
    }

    fun startMonitoring(onOrientationChanged: (from: String, to: String) -> Unit) {
        if(onOrientationChangedListener != null) context.applicationContext.unregisterComponentCallbacks(callbacks)
        _orientationState.value=getCurrentOrientation()
        this.onOrientationChangedListener = onOrientationChanged
        context.applicationContext.registerComponentCallbacks(callbacks)
        displays.unregisterDisplayListener(displayListener)
        displays.registerDisplayListener(displayListener,Handler(Looper.getMainLooper()))
    }

    fun stopMonitoring() {
        if(onOrientationChangedListener != null) context.applicationContext.unregisterComponentCallbacks(callbacks)
        onOrientationChangedListener = null
        displays.unregisterDisplayListener(displayListener)
    }

    fun updateOrientation(newConfig: Configuration) {
        val newType = if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) "LANDSCAPE" else "PORTRAIT"
        updateType(newType)
    }

    private fun updateType(newType: String) {
        val oldType = _orientationState.value
        if (oldType != newType) {
            _orientationState.value = newType
            onOrientationChangedListener?.invoke(oldType, newType)
        }
    }

    fun getCurrentOrientation(): String {
        val rotation=displays.getDisplay(Display.DEFAULT_DISPLAY)?.rotation
        return if(rotation==Surface.ROTATION_90 || rotation==Surface.ROTATION_270) "LANDSCAPE" else "PORTRAIT"
    }
}
