package com.pranay.reproai.ui.screens.running

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pranay.reproai.data.remote.dto.RunnerState
import com.pranay.reproai.ui.components.*
import com.pranay.reproai.ui.theme.TextSecondary

@Composable
fun DemoSettings(state: RunnerState, busy: Boolean, onCheck: () -> Unit, onReset: () -> Unit) {
    SectionLabel("Developer demo")
    Text("Reset ends the active session and restores BUGGY mode. Historical incidents remain available.",
        style=MaterialTheme.typography.bodySmall,color=TextSecondary)
    Row {
        TextButton(onClick=onCheck,enabled=!busy,modifier=Modifier.weight(1f).heightIn(min=48.dp)) {Text("Check readiness")}
        TextButton(onClick=onReset,enabled=!busy,modifier=Modifier.weight(1f).heightIn(min=48.dp)) {Text("Reset demo")}
    }
    state.readiness?.let {
        MetadataRow("Demo readiness",if(it.ready) "READY / ${it.executionMode}" else "Needs attention")
        it.checks.forEach {check -> TechnicalText("\u2713 $check")}
        (it.issues+it.warnings).forEach {issue -> Text(issue,style=MaterialTheme.typography.bodySmall,color=TextSecondary)}
    }
}
