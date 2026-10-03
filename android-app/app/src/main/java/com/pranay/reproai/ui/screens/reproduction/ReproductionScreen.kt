package com.pranay.reproai.ui.screens.reproduction

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
import com.pranay.reproai.data.model.ReproductionStep
import com.pranay.reproai.data.model.TestScenario

@Composable
fun ReproductionScreen(steps: List<ReproductionStep>, scenario: TestScenario, analysisResult: AnalysisResult? = null,
    onGenerateTest: () -> Unit, onBack: (() -> Unit)? = null, busy: Boolean = false, runnerMessage: String? = null,
    onReconnect: (() -> Unit)? = null, onReset: (() -> Unit)? = null) {
    var jsonOpen by rememberSaveable { mutableStateOf(false) }
    val typed = analysisResult?.testScenario
    PhonePage("Reproduction",analysisResult?.let { "From ${it.relevantEventIds.size} evidence events" },onBack,
        bottom={BottomActions("Run reproduction",onGenerateTest,"View JSON",{jsonOpen=true},enabled=typed != null && !busy,loading=busy)}) {
        item {
            runnerMessage?.let {Text(it,color=StatusError,style=MaterialTheme.typography.bodySmall)}
            if(runnerMessage != null) {
                onReconnect?.let {TextButton(onClick=it,enabled=!busy,modifier=Modifier.heightIn(min=48.dp)) {Text("Reconnect runner")}}
                onReset?.let {TextButton(onClick=it,enabled=!busy,modifier=Modifier.heightIn(min=48.dp)) {Text("Reset demo")}}
            }
            typed?.let {
                Text(it.name,style=MaterialTheme.typography.titleLarge)
                SectionLabel("Preconditions")
                if(it.preconditions.isEmpty()) Text("None specified",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
                it.preconditions.forEach {pre -> MetadataRow(pre.type,pre.value,true)}
            }
            SectionLabel("Steps")
        }
        if(typed != null) itemsIndexed(typed.steps) {index,step ->
            NumberedRow(index+1,"${step.action} ${step.target ?: step.value.orEmpty()}",step.description)
        } else items(steps) {NumberedRow(it.stepNumber,it.title,it.description)}
        item {
            SectionLabel("Expected")
            Text(analysisResult?.expectedBehavior ?: scenario.expectedBehavior,style=MaterialTheme.typography.bodyLarge)
            SectionLabel("Actual")
            Text(analysisResult?.actualBehavior ?: scenario.actualBehavior,style=MaterialTheme.typography.bodyLarge)
            SectionLabel("Test scenario")
            typed?.let {
                it.steps.forEach {step -> TechnicalText("${step.action} ${step.target.orEmpty()} ${step.value.orEmpty()}".trim(),Modifier.padding(vertical=4.dp))}
                it.assertions.forEach {assertion -> TechnicalText("${assertion.type} ${assertion.target.orEmpty()} = ${assertion.expected}",Modifier.padding(vertical=4.dp))}
            } ?: Text("No runnable scenario is available.",color=TextSecondary)
        }
    }
    if(jsonOpen) AlertDialog(onDismissRequest={jsonOpen=false},title={Text("Test scenario JSON")},
        text={ androidx.compose.foundation.text.selection.SelectionContainer {
            TechnicalText(com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(typed),
                Modifier.heightIn(max=400.dp).verticalScroll(rememberScrollState()).horizontalScroll(rememberScrollState()))
        }},confirmButton={TextButton(onClick={jsonOpen=false}) {Text("Close")}})
}
