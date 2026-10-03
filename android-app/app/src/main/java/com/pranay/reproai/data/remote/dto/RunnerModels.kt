package com.pranay.reproai.data.remote.dto

import com.google.gson.JsonObject

data class RunnerConfig(val host: String = "", val port: Int = 8000) {
    fun baseUrl(): String {
        require(host.matches(Regex("[a-zA-Z0-9.-]+")) && port in 1..65535) { "Enter a host name or IPv4 address and port 1–65535." }
        return "http://$host:$port/"
    }
}
data class RunnerHealth(val status: String, val service: String, val runnerMode: String)
data class RunnerDevice(val serial: String, val state: String, val model: String, val manufacturer: String, val android_version: String)
data class RunnerReadiness(val ready: Boolean, val executionMode: ExecutionMode, val deviceSerial: String?,
    val checks: List<String>, val issues: List<String>, val warnings: List<String>) {
    fun validated(): RunnerReadiness {
        requireNotNull(executionMode);requireNotNull(checks);requireNotNull(issues);requireNotNull(warnings)
        require(!ready || issues.isEmpty())
        return this
    }
}
enum class ExecutionPurpose { REPRODUCE, VERIFY_FIX }
enum class ExecutionStatus { PENDING, RUNNING, PASSED, FAILED, ERROR, CANCELLED }
enum class StepStatus { PENDING, RUNNING, PASSED, FAILED, SKIPPED, UNSUPPORTED }
enum class ExecutionMode { MOCK, ADB }
data class ExecutionStepResult(
    val index: Int, val action: String, val target: String?, val status: StepStatus,
    val started_at: String, val finished_at: String, val duration_ms: Double,
    val message: String, val evidence: JsonObject
)
data class ExecutionResult(
    val execution_id: String, val scenario_id: String, val scenario_name: String,
    val device_serial: String, val status: ExecutionStatus, val execution_mode: ExecutionMode,
    val started_at: String, val finished_at: String, val duration_ms: Double,
    val steps: List<ExecutionStepResult>, val assertions_passed: Int,
    val assertions_failed: Int, val failure_reason: String?,
    val network_strategy: String? = null, val observed_state: JsonObject? = null,
    val setup_evidence: JsonObject? = null
) {
    val terminal: Boolean get() = status !in listOf(ExecutionStatus.PENDING, ExecutionStatus.RUNNING)
    fun validated(expectedExecutionId: String? = null, expectedScenarioId: String? = null): ExecutionResult {
        require(execution_id.isNotBlank() && scenario_id.isNotBlank() && scenario_name.isNotBlank())
        require(expectedExecutionId == null || execution_id == expectedExecutionId)
        require(expectedScenarioId == null || scenario_id == expectedScenarioId)
        requireNotNull(status)
        requireNotNull(execution_mode)
        requireNotNull(device_serial)
        requireNotNull(started_at)
        requireNotNull(finished_at)
        requireNotNull(steps)
        require(duration_ms.isFinite() && duration_ms >= 0 && assertions_passed >= 0 && assertions_failed >= 0)
        steps.forEach { step ->
            requireNotNull(step.status)
            requireNotNull(step.action)
            requireNotNull(step.evidence)
            requireNotNull(step.message)
            require(step.index >= 0 && step.duration_ms.isFinite() && step.duration_ms >= 0)
        }
        return this
    }
}
enum class ConnectionStatus { Disconnected, Connecting, Connected, Error }
enum class ExecutionPhase { Idle, Connecting, Submitting, Running, Completed, Error }
data class RunnerState(
    val config: RunnerConfig = RunnerConfig(), val connection: ConnectionStatus = ConnectionStatus.Disconnected,
    val health: RunnerHealth? = null, val phase: ExecutionPhase = ExecutionPhase.Idle,
    val purpose: ExecutionPurpose = ExecutionPurpose.REPRODUCE,
    val result: ExecutionResult? = null, val message: String? = null
    ,val readiness: RunnerReadiness? = null
)
