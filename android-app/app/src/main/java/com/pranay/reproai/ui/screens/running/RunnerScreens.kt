package com.pranay.reproai.ui.screens.running

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pranay.reproai.ai.TestScenario
import com.pranay.reproai.data.remote.dto.*
import com.pranay.reproai.ui.components.*
import com.pranay.reproai.ui.theme.*

@Composable
fun RunnerSettings(state: RunnerState, onSave: (String, String) -> Boolean, onTest: () -> Unit) {
    var host by remember(state.config) { mutableStateOf(state.config.host) }
    var port by remember(state.config) { mutableStateOf(state.config.port.toString()) }
    val editing = state.phase !in listOf(ExecutionPhase.Connecting,ExecutionPhase.Submitting,ExecutionPhase.Running)
    Column(Modifier.fillMaxWidth().padding(bottom=8.dp)) {
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(host,{host=it},label={Text("Host")},singleLine=true,enabled=editing,
                modifier=Modifier.weight(.72f),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Uri))
            OutlinedTextField(port,{port=it},label={Text("Port")},singleLine=true,enabled=editing,
                modifier=Modifier.weight(.28f),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))
        }
        TextButton(onClick={if(onSave(host,port)) onTest()},enabled=editing && state.connection != ConnectionStatus.Connecting,
            modifier=Modifier.heightIn(min=48.dp)) {Text("Test connection")}
        MetadataRow("Connection",state.connection.name)
        state.health?.let {MetadataRow("Service","${it.service} / ${it.runnerMode.uppercase()}")}
        state.message?.let {Text(it,color=StatusError,style=MaterialTheme.typography.bodySmall)}
    }
}

fun executionTitle(state: RunnerState, scenario: TestScenario?): String {
    if (state.phase == ExecutionPhase.Error) return "EXECUTION ERROR"
    val result = state.result ?: return if (state.phase == ExecutionPhase.Error) "EXECUTION ERROR" else state.phase.name.uppercase()
    if (!result.terminal) return result.status.name
    if (result.execution_mode == ExecutionMode.MOCK) return "MOCK EXECUTION — ${result.status}"
    val measuredOutcome = runCatching {
        val observed = result.observed_state ?: return@runCatching null
        val verified = observed.get("verified")?.asBoolean == true &&
            observed.get("executionId")?.asString == result.execution_id &&
            observed.get("source")?.asString == "DEMOSHOP" &&
            result.network_strategy in listOf("DEMOSHOP_DEMO_HOOK","ANDROID_CONFIGURATION") && result.scenario_id == scenario?.id
        val controlCount = scenario?.steps?.size ?: 0
        val controlStepsPassed = result.steps.take(controlCount).let {
            controlCount > 0 && it.size == controlCount && it.all { step -> step.status == StepStatus.PASSED }
        }
        if (!verified || !controlStepsPassed || result.status !in listOf(ExecutionStatus.PASSED, ExecutionStatus.FAILED) ||
            result.steps.any { it.status == StepStatus.UNSUPPORTED }) return@runCatching null
        val events = observed.getAsJsonArray("events").map { it.asString }.toSet()
        val status = observed.get("lastApiStatus")?.takeUnless {it.isJsonNull}?.asInt
        val rotation=scenario?.steps?.any {it.action == com.pranay.reproai.ai.TestAction.ROTATE_DEVICE} == true &&
            scenario.assertions.map {it.target}.containsAll(listOf("CHECKOUT_STATE_LOST","CHECKOUT_INVALID"))
        if(rotation) {
            val checkout=observed.getAsJsonObject("checkout") ?: return@runCatching null
            val before=checkout.get("orientationBefore")?.asInt
            val after=checkout.get("orientationAfter")?.asInt
            if(before != 1 || after != 2 || !events.containsAll(setOf("PAYMENT_METHOD_SELECTED","ORIENTATION_CHANGED","CHECKOUT_RECREATED"))) return@runCatching null
            return@runCatching when {
                state.purpose == ExecutionPurpose.REPRODUCE && events.containsAll(setOf("CHECKOUT_STATE_LOST","CHECKOUT_INVALID","PAYMENT_BLOCKED")) &&
                    checkout.get("isValid")?.asBoolean == false &&
                    (checkout.get("selectedPaymentMethod") == null || checkout.get("selectedPaymentMethod").isJsonNull) -> "BUG REPRODUCED"
                state.purpose == ExecutionPurpose.VERIFY_FIX && events.containsAll(setOf("CHECKOUT_STATE_RESTORED","CHECKOUT_VALID","PAYMENT_AVAILABLE")) &&
                    !events.contains("CHECKOUT_STATE_LOST") && checkout.get("isValid")?.asBoolean == true &&
                    checkout.get("selectedPaymentMethod")?.asString == "UPI" -> "FIX VERIFIED"
                else -> null
            }
        }
        if(scenario?.assertions?.any {it.type == "ASSERT_API_STATUS" && it.target == "/payment" && it.expected == "401"} != true) return@runCatching null
        when {
            state.purpose == ExecutionPurpose.REPRODUCE && status == 401 &&
                observed.get("paymentStatus")?.asString == "FAILED" &&
                events.containsAll(setOf("TOKEN_EXPIRED", "PAYMENT_FAILED")) -> "BUG REPRODUCED"
            state.purpose == ExecutionPurpose.VERIFY_FIX && status == 200 &&
                observed.get("paymentStatus")?.asString == "SUCCESS" &&
                events.containsAll(setOf("TOKEN_REFRESHED", "PAYMENT_SUCCESS")) &&
                "TOKEN_EXPIRED" !in events && "PAYMENT_FAILED" !in events -> "FIX VERIFIED"
            else -> null
        }
    }.getOrNull()
    if (measuredOutcome != null) return measuredOutcome
    if (result.status != ExecutionStatus.PASSED) return "EXECUTION ${result.status}"
    return "EXECUTION PASSED: PRODUCT OUTCOME UNCONFIRMED"
}


@Composable
private fun RunnerStepRow(step: ExecutionStepResult, target: String) {
    var expanded by rememberSaveable(step.index,step.status) {mutableStateOf(step.status in listOf(StepStatus.FAILED,StepStatus.UNSUPPORTED))}
    val mark = when(step.status) {StepStatus.PASSED -> "\u2713";StepStatus.RUNNING -> "\u2192";StepStatus.FAILED,StepStatus.UNSUPPORTED -> "!";else -> "\u00b7"}
    Column(Modifier.fillMaxWidth().clickable {expanded = !expanded}.heightIn(min=48.dp).padding(vertical=8.dp)) {
        Row {
            TechnicalText(mark,Modifier.padding(end=8.dp),when(step.status) {StepStatus.PASSED -> StatusSuccess;StepStatus.FAILED,StepStatus.UNSUPPORTED -> StatusError;else -> TextSecondary})
            Column(Modifier.weight(1f)) {
                Row {
                    TechnicalText("%02d %s".format(step.index+1,step.action),Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp));StatusBadge(step.status.name)
                }
                if(target.isNotBlank()) TechnicalText(target,color=TextSecondary)
                if(expanded) {
                    Text(step.message,style=MaterialTheme.typography.bodySmall,color=TextSecondary,modifier=Modifier.padding(top=5.dp))
                    step.evidence.entrySet().forEach {(key,value) -> MetadataRow(key,value.toString(),true)}
                }
            }
        }
    }
    HorizontalDivider(color=DarkBorder,thickness=.5.dp)
}

@Composable
fun RunnerExecutionScreen(state: RunnerState, scenario: TestScenario?, onCancel: () -> Unit,
    onVerify: () -> Unit, onHome: () -> Unit, onViewTimeline: () -> Unit = {}, onCreateReport: () -> Unit = {},
    onRetry: (() -> Unit)? = null, onReconnect: (() -> Unit)? = null, onReset: (() -> Unit)? = null) {
    var now by remember {mutableStateOf(System.currentTimeMillis())}
    LaunchedEffect(state.result?.execution_id,state.result?.terminal) {
        while(state.result?.terminal == false) {now=System.currentTimeMillis();kotlinx.coroutines.delay(250)}
    }
    val result = state.result
    val active = state.phase in listOf(ExecutionPhase.Connecting,ExecutionPhase.Submitting,ExecutionPhase.Running)
    val outcome = executionTitle(state,scenario)
    val reproduced = outcome == "BUG REPRODUCED"
    val fixed = outcome == "FIX VERIFIED"
    val title = when {active -> "Reproduction run";reproduced -> "Incident reproduced";fixed -> "Fix verified";else -> "Execution result"}
    val badge = when {state.phase == ExecutionPhase.Error -> "Error";active -> state.phase.name;reproduced -> "Reproduced";fixed -> "Verified";result?.execution_mode == ExecutionMode.MOCK -> "Mock";else -> result?.status?.name ?: "Error"}
    val elapsed = if(result?.terminal == true) result.duration_ms else result?.let {r -> runCatching {
        (now-java.time.Instant.parse(r.started_at).toEpochMilli()).coerceAtLeast(0).toDouble()
    }.getOrElse {r.steps.sumOf {it.duration_ms}}} ?: 0.0
    PhonePage(title,badge=badge,onBack=onHome,bottom={
        if(active) BottomActions("Cancel execution",onCancel,enabled=result != null)
        else if(state.phase == ExecutionPhase.Error && onRetry != null) BottomActions("Retry",onRetry,"Return home",onHome,enabled=scenario != null)
        else BottomActions("Verify fix",onVerify,"Return home",onHome,enabled=scenario != null)
    }) {
        item {
            Text(scenario?.name ?: result?.scenario_name ?: "No scenario available",style=MaterialTheme.typography.titleLarge)
            if(!active && !reproduced && !fixed) Text(outcome,style=MaterialTheme.typography.bodySmall,
                color=TextSecondary,modifier=Modifier.padding(top=6.dp))
            MetadataRow("Purpose",if(state.purpose == ExecutionPurpose.REPRODUCE) "Reproduce" else "Verify fix")
            result?.let {
                MetadataRow("Mode",it.execution_mode.name,true)
                MetadataRow("Device",if(it.execution_mode == ExecutionMode.ADB) "Authorized Android device" else "Simulated device")
                MetadataRow("Elapsed","%.1fs".format(elapsed/1000),true)
                it.network_strategy?.let {strategy ->
                    MetadataRow("Strategy",strategy,true)
                    if(strategy == "DEMOSHOP_DEMO_HOOK") Text("DemoShop simulates the transition. Physical radios remain unchanged.",
                        style=MaterialTheme.typography.bodySmall,color=TextSecondary)
                }
                if(it.execution_mode == ExecutionMode.MOCK) Text("Simulated execution. No device reproduction or fix is proven.",
                    style=MaterialTheme.typography.bodySmall,color=TextSecondary)
                if(active) LinearProgressIndicator(progress={it.steps.count {step -> step.status in listOf(StepStatus.PASSED,StepStatus.FAILED,StepStatus.UNSUPPORTED,StepStatus.SKIPPED)}.toFloat()/it.steps.size.coerceAtLeast(1)},
                    modifier=Modifier.fillMaxWidth().padding(top=12.dp).height(3.dp),color=PrimaryBlue,trackColor=DarkSurfaceVariant)
                else {
                    MetadataRow(if(fixed) "Original failure checks" else "Assertions","${it.assertions_passed} / ${it.assertions_passed+it.assertions_failed} matched",true)
                    if(fixed) Text("The same scenario reached its expected healthy state. Original failure assertions correctly no longer match.",
                        style=MaterialTheme.typography.bodySmall,color=TextSecondary)
                    SectionLabel("Observed evidence")
                    val events = runCatching {it.observed_state?.getAsJsonArray("events")?.map {event -> event.asString}.orEmpty()}.getOrDefault(emptyList())
                    if(events.isEmpty()) Text("No measured device events.",style=MaterialTheme.typography.bodySmall,color=TextSecondary)
                    events.forEach {event -> TechnicalText(event,Modifier.padding(vertical=3.dp))}
                    val api = runCatching {it.observed_state?.get("lastApiStatus")?.takeUnless {value -> value.isJsonNull}?.asInt}.getOrNull()
                    if(api != null) {
                        SectionLabel(if(fixed) "Current payment" else "Failure signature")
                        MetadataRow("Endpoint","/payment",true)
                        MetadataRow("Observed","HTTP $api",true)
                    }
                }
                SectionLabel(if(active) "Execution" else "Execution details","Tap a step for evidence")
            }
            state.message?.let {Text(it,color=StatusError,style=MaterialTheme.typography.bodyMedium,modifier=Modifier.padding(vertical=8.dp))}
            if(state.phase == ExecutionPhase.Error || (result?.status == ExecutionStatus.ERROR)) {
                onReconnect?.let {TextButton(onClick=it,modifier=Modifier.heightIn(min=48.dp)) {Text("Reconnect runner")}}
                onReset?.let {TextButton(onClick=it,modifier=Modifier.heightIn(min=48.dp)) {Text("Reset demo")}}
                if(state.phase != ExecutionPhase.Error) onRetry?.let {TextButton(onClick=it,modifier=Modifier.heightIn(min=48.dp)) {Text("Retry")}}
            }
        }
        result?.let {r ->
            items(r.steps,key={it.index}) {step ->
                val target = step.target ?: scenario?.steps?.getOrNull(step.index)?.value.orEmpty()
                RunnerStepRow(step,target)
            }
            item {
                r.failure_reason?.let {Text(it,color=if(fixed) TextSecondary else StatusError,
                    style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(top=10.dp))}
                if(!active) {
                    MetadataRow("Execution ID",r.execution_id,true)
                    Text("Verify Fix reruns the same scenario. Enable the matching DemoShop debug fix first.",
                        style=MaterialTheme.typography.bodySmall,color=TextSecondary,modifier=Modifier.padding(top=12.dp))
                    SectionLabel("Incident actions")
                    TextButton(onClick=onViewTimeline) {Text("View timeline")}
                    TextButton(onClick=onCreateReport) {Text("Create report")}
                }
            }
        }
    }
}
