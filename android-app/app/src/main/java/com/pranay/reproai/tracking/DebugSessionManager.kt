package com.pranay.reproai.tracking

import android.content.Context
import android.os.Build
import android.util.Log
import com.pranay.reproai.data.model.DebugEvent
import com.pranay.reproai.data.model.DebugEventType
import com.pranay.reproai.data.model.DebugSession
import com.pranay.reproai.data.model.DeviceInfo
import com.pranay.reproai.data.repository.DebugSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class DebugSessionManager(
    private val context: Context,
    private val repository: DebugSessionRepository
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val eventMutex = Mutex()

    val networkMonitor = NetworkMonitor(context)
    val lifecycleMonitor = AppLifecycleMonitor()
    val orientationMonitor = OrientationMonitor(context)

    private val _currentSession = MutableStateFlow<DebugSession?>(null)
    val currentSession: StateFlow<DebugSession?> = _currentSession.asStateFlow()

    private val _events = MutableStateFlow<List<DebugEvent>>(emptyList())
    val events: StateFlow<List<DebugEvent>> = _events.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    @Volatile
    private var isSessionPersisted = false

    init {
        ReproTracker.initialize(this)
        com.pranay.reproai.receiver.SdkEventReceiver.activeSessionManager = this
    }

    fun hasActiveSession(): Boolean {
        return _currentSession.value?.isActive == true && isSessionPersisted
    }

    fun startSession(): DebugSession {
        stopSessionInternalSync()

        val sessionId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val deviceInfo = DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            appVersion = "1.0.0"
        )

        val session = DebugSession(
            id = sessionId,
            startedAt = now,
            isActive = true,
            deviceInfo = deviceInfo,
            currentScreen = "Home",
            networkType = networkMonitor.getCurrentNetworkType(),
            appState = "Foreground",
            memoryStatus = "Normal"
        )

        isSessionPersisted = false
        _currentSession.value = session
        _events.value = emptyList()
        _elapsedSeconds.value = 0

        scope.launch {
            try {
                Log.d("ReproDB", "Creating session: ${session.id}")
                repository.insertSession(session)
                isSessionPersisted = true
                Log.d("ReproDB", "Session persisted: ${session.id}")

                addEventInternal(
                    type = DebugEventType.SESSION_STARTED,
                    title = "Debug session started",
                    description = "Started monitoring on ${deviceInfo.manufacturer} ${deviceInfo.model}"
                )

                withContext(Dispatchers.Main) { startMonitors() }
                startTimer()
            } catch (e: Exception) {
                Log.e("ReproDB", "Error starting debug session: ${e.message}", e)
            }
        }

        return session
    }

    fun stopSession() {
        scope.launch {
            stopSessionInternal()
        }
    }

    private fun stopSessionInternalSync() {
        stopMonitors()
    }

    private suspend fun stopSessionInternal() {
        val session = _currentSession.value ?: return
        if (!session.isActive) return

        withContext(Dispatchers.Main) { stopMonitors() }

        val now = System.currentTimeMillis()
        val elapsedMs = now - session.startedAt

        val stopEvent = DebugEvent(
            id = UUID.randomUUID().toString(),
            sessionId = session.id,
            timestamp = now,
            elapsedMs = elapsedMs,
            type = DebugEventType.SESSION_STOPPED,
            title = "Debug session stopped",
            description = "Total session duration: ${_elapsedSeconds.value}s"
        )

        if (isSessionPersisted) {
            repository.insertEvent(stopEvent)
        }

        val updatedEvents = _events.value + stopEvent
        _events.value = updatedEvents

        val finalSession = session.copy(
            endedAt = now,
            isActive = false,
            eventsCaptured = updatedEvents.size,
            events = updatedEvents
        )

        _currentSession.value = finalSession

        if (isSessionPersisted) {
            repository.updateSession(finalSession)
        }

        isSessionPersisted = false
        Log.d("ReproDB", "Session stopped cleanly: ${session.id}")
    }

    fun addEvent(
        type: DebugEventType,
        title: String,
        description: String = "",
        metadata: Map<String, String> = emptyMap(),
        timestamp: Long = System.currentTimeMillis()
    ) {
        if (!hasActiveSession()) {
            Log.w("ReproDB", "Ignored event $type because no persisted active session exists.")
            return
        }

        scope.launch {
            addEventInternal(type, title, description, metadata, timestamp)
        }
    }

    private suspend fun addEventInternal(
        type: DebugEventType,
        title: String,
        description: String = "",
        metadata: Map<String, String> = emptyMap(),
        timestamp: Long = System.currentTimeMillis()
    ) = eventMutex.withLock {
        val session = _currentSession.value ?: return@withLock
        val now = timestamp.coerceIn(session.startedAt, System.currentTimeMillis())
        val elapsedMs = now - session.startedAt

        val event = DebugEvent(
            id = UUID.randomUUID().toString(),
            sessionId = session.id,
            timestamp = now,
            elapsedMs = elapsedMs,
            type = type,
            title = title,
            description = description,
            metadata = metadata
        )

        val updatedEvents = (_events.value + event).sortedBy {it.timestamp}
        _events.value = updatedEvents

        val updatedSession = session.copy(
            eventsCaptured = updatedEvents.size,
            events = updatedEvents
        )
        _currentSession.value = updatedSession

        if (isSessionPersisted) {
            repository.insertEvent(event)
            repository.updateSession(updatedSession)
        }
    }

    suspend fun updateIncidentDetails(sessionId: String, title: String, description: String) {
        val session = _currentSession.value?.takeIf { it.id == sessionId } ?: return
        val updated = session.copy(issueTitle = title, userDescription = description)
        _currentSession.value = updated
        if (isSessionPersisted) repository.updateSession(updated)
    }

    fun captureBug(userDescription: String? = null) {
        if (!hasActiveSession()) return
        val session = _currentSession.value ?: return
        val now = System.currentTimeMillis()

        val updatedSession = session.copy(
            bugCapturedAt = now,
            userDescription = userDescription ?: session.userDescription
        )
        _currentSession.value = updatedSession

        val metadata = mapOf(
            "network" to networkMonitor.networkState.value,
            "appState" to lifecycleMonitor.appState.value,
            "orientation" to orientationMonitor.orientationState.value
        )

        scope.launch {
            if (isSessionPersisted) {
                repository.updateSession(updatedSession)
            }
            addEventInternal(
                type = DebugEventType.BUG_CAPTURED,
                title = "Bug Captured",
                description = "Captured bug snapshot during active session",
                metadata = metadata
            )
        }
    }

    fun simulatePaymentBug() {
        if (!hasActiveSession()) {
            startSession()
        }
        scope.launch {
            var retry = 0
            while (!isSessionPersisted && retry < 20) {
                delay(100)
                retry++
            }
            if (!isSessionPersisted) return@launch

            addEventInternal(
                type = DebugEventType.USER_ACTION,
                title = "PAY button tapped",
                description = "User clicked on primary checkout payment action",
                metadata = mapOf("source" to "simulation", "component" to "CheckoutScreen")
            )
            delay(500)
            addEventInternal(
                type = DebugEventType.NETWORK_CHANGED,
                title = "Wi-Fi disconnected",
                description = "Network interface dropped signal",
                metadata = mapOf("source" to "simulation", "from" to "WIFI", "to" to "CELLULAR")
            )
            delay(500)
            addEventInternal(
                type = DebugEventType.RETRY,
                title = "Payment retry triggered",
                description = "Automatic network failure retry policy initiated",
                metadata = mapOf("source" to "simulation")
            )
            delay(600)
            addEventInternal(
                type = DebugEventType.ERROR,
                title = "HTTP 401 received",
                description = "Backend response: Unauthorized bearer token",
                metadata = mapOf("source" to "simulation", "status" to "401")
            )
            delay(400)
            addEventInternal(
                type = DebugEventType.ERROR,
                title = "TOKEN_EXPIRED",
                description = "Session auth token validation failure",
                metadata = mapOf("source" to "simulation")
            )
        }
    }

    private fun startTimer() {
        scope.launch {
            while (_currentSession.value?.isActive == true && isSessionPersisted) {
                val session = _currentSession.value ?: break
                val seconds = ((System.currentTimeMillis() - session.startedAt) / 1000).toInt()
                _elapsedSeconds.value = seconds
                delay(1000)
            }
        }
    }

    private fun startMonitors() {
        networkMonitor.startMonitoring { from, to ->
            if (hasActiveSession()) {
                addEvent(
                    type = DebugEventType.NETWORK_CHANGED,
                    title = "Network changed",
                    description = "$from → $to",
                    metadata = mapOf("from" to from, "to" to to)
                )
                _currentSession.value = _currentSession.value?.copy(networkType = to)
            }
        }

        lifecycleMonitor.startMonitoring { state ->
            if (hasActiveSession()) {
                val eventType = if (state == "Foreground") DebugEventType.APP_FOREGROUND else DebugEventType.APP_BACKGROUND
                addEvent(
                    type = eventType,
                    title = "Application $state",
                    description = "App transitioned to $state"
                )
                _currentSession.value = _currentSession.value?.copy(appState = state)
            }
        }

        orientationMonitor.startMonitoring { from, to ->
            if (hasActiveSession()) {
                addEvent(
                    type = DebugEventType.ORIENTATION_CHANGED,
                    title = "Orientation changed",
                    description = "$from → $to",
                    metadata = mapOf("from" to from, "to" to to)
                )
            }
        }
    }

    private fun stopMonitors() {
        networkMonitor.stopMonitoring()
        lifecycleMonitor.stopMonitoring()
        orientationMonitor.stopMonitoring()
    }
}

object ReproTracker {
    private var sessionManager: DebugSessionManager? = null

    fun initialize(manager: DebugSessionManager) {
        sessionManager = manager
    }

    fun trackAction(title: String, description: String = "", metadata: Map<String, String> = emptyMap()) {
        sessionManager?.addEvent(
            type = DebugEventType.USER_ACTION,
            title = title,
            description = description,
            metadata = metadata
        )
    }

    fun trackScreen(screenName: String) {
        sessionManager?.addEvent(
            type = DebugEventType.SCREEN_OPENED,
            title = "Screen opened: $screenName",
            description = "Navigated to $screenName screen",
            metadata = mapOf("screen" to screenName)
        )
    }

    fun trackEvent(
        type: DebugEventType,
        title: String,
        description: String = "",
        metadata: Map<String, String> = emptyMap()
    ) {
        sessionManager?.addEvent(type, title, description, metadata)
    }
}
