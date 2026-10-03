package com.pranay.reproai.data.repository

import android.content.Context
import com.pranay.reproai.ai.TestScenario
import com.pranay.reproai.data.remote.RunnerApiClient
import com.pranay.reproai.data.remote.dto.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import okhttp3.*
import retrofit2.HttpException
import java.io.IOException

class ReproRunnerRepository(context: Context) {
    private val preferences = context.getSharedPreferences("runner", Context.MODE_PRIVATE)
    private val mutable = MutableStateFlow(RunnerState(config = RunnerConfig(
        preferences.getString("host", "") ?: "", preferences.getInt("port", 8000))))
    val state: StateFlow<RunnerState> = mutable.asStateFlow()
    fun reportError(message: String) {
        mutable.update { it.copy(phase = ExecutionPhase.Error, message = message, result = null) }
    }
    fun reportStorageError() {
        mutable.update { it.copy(message = "Execution finished, but saving the incident failed. The result remains available here.") }
    }
    fun restore(result: ExecutionResult, purpose: ExecutionPurpose) {
        if(!result.terminal) {
            reportError("Saved execution was interrupted. Reconnect the runner and retry; no active run was restored.")
            return
        }
        if (!active) mutable.update { it.copy(result = result, purpose = purpose, phase = ExecutionPhase.Completed, message = null) }
    }
    private var active = false
    fun configure(host: String, port: String): Boolean {
        if (active || state.value.connection == ConnectionStatus.Connecting) return false
        val config = RunnerConfig(host.trim(), port.toIntOrNull() ?: 0)
        try {
            config.baseUrl()
            preferences.edit().putString("host", config.host).putInt("port", config.port).apply()
            mutable.value = RunnerState(config = config)
            return true
        } catch (e: IllegalArgumentException) {
            mutable.update { it.copy(connection = ConnectionStatus.Error, health = null, message = e.message) }
            return false
        }
    }
    suspend fun testConnection() {
        if (active) return
        connect()
    }
    private suspend fun connect() {
        mutable.update { it.copy(connection = ConnectionStatus.Connecting, health = null, message = null) }
        try {
            val health = RunnerApiClient.api(state.value.config).health()
            require(health.status == "ok" && health.service == "repro-runner" && health.runnerMode in listOf("mock", "adb")) { "Unexpected runner health response." }
            mutable.update { it.copy(connection = ConnectionStatus.Connected, health = health) }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            mutable.update { it.copy(connection = ConnectionStatus.Error, message = friendly(e)) }
        }
    }
    suspend fun checkReadiness(scenario: TestScenario? = null): RunnerReadiness? {
        if(active) return null
        return requestReadiness(scenario)
    }
    private suspend fun requestReadiness(scenario: TestScenario?): RunnerReadiness? {
        connect()
        if(state.value.connection != ConnectionStatus.Connected) return null
        return try {
            val api=RunnerApiClient.api(state.value.config)
            val readiness=(if(scenario == null) api.preflight() else api.preflight(scenario)).validated()
            mutable.update {it.copy(readiness=readiness,message=(readiness.issues+readiness.warnings).joinToString("\n").ifBlank {null})}
            readiness
        } catch(e: Exception) {
            if(e is CancellationException) throw e
            mutable.update {it.copy(readiness=null,message=friendly(e))}
            null
        }
    }
    suspend fun resetDemo(): Boolean {
        if(active) return false
        return try {
            RunnerApiClient.api(state.value.config).resetDemo()
            mutable.update {RunnerState(config=it.config,message="Demo reset to BUGGY mode. Incident history preserved.")}
            true
        } catch(e: Exception) {
            if(e is CancellationException) throw e
            mutable.update {it.copy(message=if(e is HttpException && e.code()==403)
                "Enable ENABLE_DEMO_CONTROLS on the trusted demo runner before resetting." else friendly(e))}
            false
        }
    }
    suspend fun execute(scenario: TestScenario, purpose: ExecutionPurpose, allowMock: Boolean = false): ExecutionResult? {
        if (active) return null
        active = true
        val config = state.value.config
        val api = try { RunnerApiClient.api(config) } catch (e: Exception) {
            active = false
            mutable.update { it.copy(phase = ExecutionPhase.Error, result = null, message = friendly(e)) }
            return null
        }
        mutable.update { it.copy(purpose = purpose, result = null, phase = ExecutionPhase.Connecting, message = null) }
        try {
            val readiness=requireNotNull(requestReadiness(scenario)) {state.value.message ?: "Repro Runner unavailable. Reconnect and retry."}
            check(readiness.ready) {readiness.issues.joinToString("\n")}
            check(readiness.executionMode != ExecutionMode.MOCK || allowMock) {"Mock runner enabled. Confirm a mock run explicitly, or restart the runner in ADB mode."}
            mutable.update { it.copy(phase = ExecutionPhase.Submitting) }
            val submitted = api.execute(scenario).validated(expectedScenarioId = scenario.id)
            mutable.update { it.copy(result = submitted, phase = ExecutionPhase.Running) }
            return withTimeout(190_000) {
                var result = submitted
                if (!result.terminal) {
                    val updates = Channel<ExecutionResult>(Channel.CONFLATED)
                    val listener = object : WebSocketListener() {
                        override fun onMessage(webSocket: WebSocket, text: String) {
                            try {
                                val update = RunnerApiClient.gson.fromJson(text, ExecutionResult::class.java)
                                    .validated(submitted.execution_id, scenario.id)
                                if (update.execution_id == submitted.execution_id) updates.trySend(update)
                            } catch (_: Exception) { updates.close() }
                        }
                        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) { updates.close() }
                        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) { updates.close() }
                    }
                    val socket = RunnerApiClient.http.newWebSocket(Request.Builder()
                        .url(config.baseUrl().replaceFirst("http", "ws") + "ws/executions/${submitted.execution_id}").build(), listener)
                    try {
                        var connectionFailures = 0
                        while (!result.terminal) {
                            val update = withTimeoutOrNull(750) { updates.receiveCatching().getOrNull() }
                            try {
                                result = update ?: api.execution(submitted.execution_id).validated(submitted.execution_id, scenario.id)
                                connectionFailures = 0
                            } catch (e: IOException) {
                                if (++connectionFailures >= 3) throw e
                                mutable.update { it.copy(message = "Runner connection interrupted. Reconnecting to the current execution…") }
                                delay(1_000)
                                continue
                            }
                            mutable.update { it.copy(result = result, message = null) }
                            if (update == null && !result.terminal) delay(750)
                        }
                    } finally { socket.cancel(); updates.close() }
                }
                mutable.update { it.copy(result = result, phase = ExecutionPhase.Completed) }
                result
            }
        } catch (e: Exception) {
            if (e is CancellationException && e !is TimeoutCancellationException) throw e
            state.value.result?.takeUnless { it.terminal }?.let {
                try { api.cancel(it.execution_id) } catch (_: Exception) { }
            }
            mutable.update { it.copy(phase = ExecutionPhase.Error, message = friendly(e)) }
            return null
        } finally { active = false }
    }
    suspend fun cancel() {
        val result = state.value.result ?: return
        if (result.terminal) return
        try {
            val cancelled = RunnerApiClient.api(state.value.config).cancel(result.execution_id).validated(result.execution_id, result.scenario_id)
            mutable.update { it.copy(result = cancelled) }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            mutable.update { it.copy(message = friendly(e)) }
        }
    }
    private fun friendly(e: Exception): String = when (e) {
        is HttpException -> when (e.code()) {
            422 -> "Scenario validation failed. Check the API contract and generated scenario."
            404 -> "Execution no longer exists. The runner may have restarted."
            else -> "Runner HTTP error ${e.code()}. Check the backend."
        }
        is java.net.SocketTimeoutException, is TimeoutCancellationException -> "Runner timed out. Check the backend and device."
        is IOException -> "Cannot reach runner. Check host, port, Wi-Fi and Windows firewall."
        is IllegalArgumentException, is IllegalStateException -> e.message ?: "Invalid runner configuration."
        else -> "Invalid response or runner error. Check the backend API contract."
    }
}
