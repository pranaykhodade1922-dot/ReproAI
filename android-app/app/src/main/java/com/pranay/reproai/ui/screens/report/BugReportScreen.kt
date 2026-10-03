package com.pranay.reproai.ui.screens.report

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pranay.reproai.ai.AnalysisResult
import com.pranay.reproai.data.local.ReproDatabase
import com.pranay.reproai.data.repository.DebugSessionRepository
import com.pranay.reproai.report.*
import com.pranay.reproai.ui.components.*
import com.pranay.reproai.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant

@Composable
fun BugReportScreen(sessionId: String, currentAnalysis: AnalysisResult?, onBack: () -> Unit) {
    val context = LocalContext.current
    var report by remember(sessionId) { mutableStateOf<IncidentReport?>(null) }
    var error by remember(sessionId) { mutableStateOf<String?>(null) }
    LaunchedEffect(sessionId,currentAnalysis) {
        try {
            report = withContext(Dispatchers.IO) {
                val db = ReproDatabase.getInstance(context)
                IncidentReportRepository(db,DebugSessionRepository(db.debugSessionDao(),db.debugEventDao())).load(sessionId,currentAnalysis)
                    ?.let(IncidentReportExporter::sanitized)
            }
            if (report == null) error = "This incident is no longer available."
        } catch(e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            error = "Report could not be loaded. Return to the incident and try again."
        }
    }
    report?.let { IncidentReportScreen(it,onBack) } ?: PhonePage("Incident report",onBack=onBack) {
        item { Text(error ?: "Loading report...",color=TextSecondary) }
    }
}

@Composable
fun IncidentReportScreen(report: IncidentReport, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }
    var exporting by rememberSaveable { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var preparedExport by remember { mutableStateOf<Pair<java.io.File,ReportFormat>?>(null) }
    var savePickerOpen by remember { mutableStateOf(false) }
    fun notify(message: String) { scope.launch { snack.showSnackbar(message) } }
    val saveFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
        val file = preparedExport?.first
        savePickerOpen=false;preparedExport=null
        if(uri != null && file != null) scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    requireNotNull(context.contentResolver.openOutputStream(uri)).use { out -> file.inputStream().use { it.copyTo(out) } }
                }
                notify("Saved to Files; ready for phone-to-PC transfer")
            } catch(e: Exception) {
                if(e is kotlinx.coroutines.CancellationException) throw e
                notify("File could not be saved. Export again and choose another location.")
            }
        }
    }
    PhonePage("Incident report",report.incidentId,onBack,badge=report.status.name.replace('_',' '),bottom={
        Surface(color=DarkSurface) { Column(Modifier.fillMaxWidth()) {
            SnackbarHost(snack)
            HorizontalDivider(color=DarkBorder,thickness=.5.dp)
            Column(Modifier.padding(horizontal=16.dp,vertical=8.dp)) {
                PrimaryActionButton("Share report",onClick={
                    runCatching { ReportSharing.shareText(context,report) }.onFailure { notify("No sharing app available") }
                },enabled=!busy)
                Row(Modifier.fillMaxWidth()) {
                    TextButton(onClick={
                        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                            .setPrimaryClip(ClipData.newPlainText("Incident report",IncidentReportExporter.summary(report)))
                        notify("Report summary copied")
                    },modifier=Modifier.weight(1f).heightIn(min=48.dp)) { Text("Copy summary") }
                    TextButton(onClick={exporting=true},enabled=!busy,modifier=Modifier.weight(1f).heightIn(min=48.dp)) {Text("Export")}
                }
            }
        } }
    }) {
        item {
            SectionLabel("Summary")
            Text(report.issueTitle,style=MaterialTheme.typography.titleLarge)
            if(report.description.isNotBlank()) Text(report.description,style=MaterialTheme.typography.bodyMedium,
                color=TextSecondary,modifier=Modifier.padding(vertical=8.dp))
            MetadataRow("App",report.environment.application ?: "Not available yet")
            MetadataRow("Captured",report.environment.capturedAt?.let { Instant.ofEpochMilli(it).toString() } ?: "Not available yet")
            MetadataRow("Likely category",report.likelyOwnerCategory.name.lowercase().replaceFirstChar { it.uppercase() })
            Text("Inferred category; ownership is not guaranteed.",style=MaterialTheme.typography.bodySmall,color=TextSecondary)
            SectionLabel("Environment")
            MetadataRow("Device","${report.environment.manufacturer} ${report.environment.model}")
            MetadataRow("Android",report.environment.androidVersion)
            MetadataRow("Package",report.environment.packageName ?: "Not available yet",true)
            MetadataRow("Runner mode",report.environment.runnerMode ?: "Not available yet",true)
            MetadataRow("Execution strategy",report.environment.networkStrategy ?: "Not available yet",true)
            if(report.environment.networkStrategy == "DEMOSHOP_DEMO_HOOK") Text(
                "DemoShop simulates the network transition and payment API; physical radios remain unchanged.",
                style=MaterialTheme.typography.bodySmall,color=TextSecondary)
            MetadataRow("Session ID",report.sessionId,true)
            SectionLabel("Evidence","Observed")
            if(report.evidence.isEmpty()) Text("Not available yet",color=TextSecondary)
        }
        items(report.evidence) { evidence ->
            var expanded by rememberSaveable(evidence.timestamp,evidence.title) { mutableStateOf(false) }
            Column(Modifier.fillMaxWidth().padding(vertical=6.dp)) {
                TechnicalText(evidence.title)
                MetadataRow(eventTime(evidence.timestamp),"${evidence.source} / ${evidence.type}",true)
                if(evidence.description.isNotBlank()) {
                    TextButton(onClick={expanded=!expanded},modifier=Modifier.heightIn(min=48.dp)) { Text(if(expanded) "Hide details" else "Details") }
                    if(expanded) Text(evidence.description,style=MaterialTheme.typography.bodySmall,color=TextSecondary)
                }
                HorizontalDivider(color=DarkBorder,thickness=.5.dp)
            }
        }
        item {
            SectionLabel("Diagnosis","Inferred")
            report.diagnosis?.let {
                MetadataRow("Probable trigger",it.probableTrigger)
                MetadataRow("Probable cause",it.probableCause)
                MetadataRow("Evidence strength",IncidentReportExporter.strength(it.confidence))
                MetadataRow("Components",it.relevantComponents.joinToString())
            } ?: Text("Not available yet",color=TextSecondary)
            SectionLabel("Reproduction")
            report.reproduction?.let {
                MetadataRow("Scenario",it.scenario.name)
                TechnicalText(it.scenario.id)
                it.scenario.preconditions.forEach { p -> MetadataRow(p.type,p.value) }
                it.scenario.steps.forEachIndexed { i,s -> NumberedRow(i+1,s.description ?: s.action.name,
                    listOfNotNull(s.action.name,s.target,s.value).joinToString(" / ")) }
                MetadataRow("Expected",it.expectedBehavior)
                MetadataRow("Actual",it.actualBehavior)
                it.scenario.assertions.forEach { a -> TechnicalText("${a.type} ${a.target.orEmpty()} = ${a.expected}") }
            } ?: Text("Not available yet",color=TextSecondary)
            SectionLabel("Execution")
            report.execution?.let { ExecutionDetails(it) } ?: Text("Not available yet",color=TextSecondary)
            SectionLabel("Verification")
            report.verification?.let {
                MetadataRow("Before",it.originalFailure.joinToString().ifBlank { "Original execution unavailable" })
                MetadataRow("After",it.execution.observedSignature.joinToString().ifBlank { "Measured signature unavailable" })
                MetadataRow("Same recorded scenario",it.sameScenario?.toString() ?: "Not available yet")
                ExecutionDetails(it.execution)
                Text("Original failure assertions remain unchanged; fix verification uses measured success evidence.",
                    style=MaterialTheme.typography.bodySmall,color=TextSecondary)
            } ?: Text("Not available yet",color=TextSecondary)
        }
    }
    if(exporting) AlertDialog(onDismissRequest={exporting=false},title={Text("Export report")},text={Column(Modifier.verticalScroll(rememberScrollState())) {
        ReportFormat.entries.forEach { format -> TextButton(onClick={
            exporting=false; busy=true
            scope.launch {
                try {
                    val file=withContext(Dispatchers.IO) { ReportSharing.exportFile(context,report,format) }
                    if(format in listOf(ReportFormat.SCENARIO_JSON,ReportFormat.DEVELOPER_PACKAGE)) preparedExport=file to format
                    else {
                        ReportSharing.shareFile(context,file,format)
                        notify("${format.extension.uppercase()} report saved")
                    }
                } catch(e: Exception) {
                    if(e is kotlinx.coroutines.CancellationException) throw e
                    notify("Could not export report. Try again.")
                } finally { busy=false }
            }
        },enabled=format != ReportFormat.SCENARIO_JSON || report.reproduction != null,
            modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)) {Text("Export ${when(format) {
                ReportFormat.JSON -> "JSON";ReportFormat.MARKDOWN -> "Markdown";ReportFormat.TEXT -> "Text"
                ReportFormat.SCENARIO_JSON -> "TestScenario JSON";ReportFormat.DEVELOPER_PACKAGE -> "developer package"
            }}")} }
    }},confirmButton={TextButton(onClick={exporting=false}) {Text("Cancel")}})
    if(preparedExport != null && !savePickerOpen) AlertDialog(onDismissRequest={preparedExport=null},
        title={Text("Developer artifact ready")},text={Text("Save to Files for Office Kit transfer, or share through an available app.")},
        confirmButton={TextButton(onClick={savePickerOpen=true;saveFile.launch(requireNotNull(preparedExport).first.name)}) {Text("Save to Files")}},
        dismissButton={TextButton(onClick={val export=requireNotNull(preparedExport);preparedExport=null
            runCatching {ReportSharing.shareFile(context,export.first,export.second)}.onFailure {notify("No sharing app available")} }) {Text("Share file")}})
}

@Composable
private fun ExecutionDetails(execution: ReportExecution) {
    MetadataRow("Outcome",execution.outcome)
    MetadataRow("Execution ID",execution.result.execution_id,true)
    MetadataRow("Execution mode",execution.result.execution_mode.name,true)
    MetadataRow("Device",execution.result.device_serial,true)
    MetadataRow("Duration","%.1f s".format(execution.result.duration_ms/1000))
    MetadataRow("Step count",execution.result.steps.size.toString())
    MetadataRow("Assertions","${execution.result.assertions_passed} matched / ${execution.result.assertions_failed} unmatched")
    execution.result.steps.filter { it.action.startsWith("ASSERT") }.forEach { MetadataRow(it.action,"${it.status}: ${it.message}") }
    MetadataRow("Measured signature",execution.observedSignature.joinToString().ifBlank { "Not available yet" })
}
