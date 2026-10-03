package com.pranay.reproai.ui.screens.analysis

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.pranay.reproai.ui.components.*
import com.pranay.reproai.ui.theme.*
import com.pranay.reproai.ai.AnalysisResult
import com.pranay.reproai.data.model.AIAnalysis

@Composable
fun AiAnalysisScreen(analysis: AIAnalysis, result: AnalysisResult? = null, onViewTimeline: () -> Unit,
    onGenerateReproduction: () -> Unit, sessionId: String? = null, onBack: (() -> Unit)? = null,
    onCreateReport: () -> Unit = {}) {
    PhonePage(sessionId?.let { "Incident RPA-${it.take(8).uppercase()}" } ?: "Incident analysis",
        badge=if(result != null) "Analyzed" else "Unavailable",onBack=onBack,
        bottom={BottomActions("Open reproduction",onGenerateReproduction,enabled=result != null)}) {
        if(result == null) item { Text("Analyze a captured incident to view evidence and diagnosis.", color=TextSecondary) }
        else {
            item {
                Text(result.issueTitle,style=MaterialTheme.typography.titleLarge)
                Text("${result.relevantEventIds.size} relevant events",style=MaterialTheme.typography.bodySmall,
                    color=TextSecondary,modifier=Modifier.padding(top=4.dp))
                SectionLabel("Evidence")
            }
            val evidence = result.failureSequence.filter { event ->
                val title = event.title.uppercase()
                title.contains("PAY_BUTTON") || title.contains("NETWORK") || title.contains("TOKEN") ||
                    title.contains("API RESPONSE") || title.contains("PAYMENT_FAILED") || title.contains("PAYMENT_SUCCESS")
            }.ifEmpty { result.failureSequence }.takeLast(6)
            items(evidence) { event ->
                Row(Modifier.fillMaxWidth().padding(vertical=6.dp)) {
                    TechnicalText(eventTime(event.timestamp), Modifier.padding(end=10.dp),TextSecondary)
                    TechnicalText(event.title,Modifier.weight(1f))
                }
                HorizontalDivider(color=DarkBorder,thickness=.5.dp)
            }
            item {
                TextButton(onClick=onViewTimeline) {Text("View full timeline")}
                SectionLabel("Diagnosis")
                Text("Probable cause",style=MaterialTheme.typography.bodySmall,color=TextSecondary)
                Text(result.probableCause,style=MaterialTheme.typography.bodyLarge,modifier=Modifier.padding(top=4.dp))
                MetadataRow("Evidence strength",when {result.confidence >= 80 -> "High"; result.confidence >= 50 -> "Moderate"; else -> "Low"})
                MetadataRow("Likely trigger",result.probableTrigger)
                SectionLabel("Likely area")
                result.relevantComponents.forEach {TechnicalText(it,Modifier.padding(vertical=3.dp))}
                SectionLabel("Reproduction")
                Text("${result.testScenario.steps.size} actions / ${result.testScenario.assertions.size} assertions", style=MaterialTheme.typography.bodyMedium,color=TextSecondary)
                SectionLabel("Report")
                TextButton(onClick=onCreateReport) {Icon(Icons.Default.Description,null,Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text("Create report")}
            }
        }
    }
}
