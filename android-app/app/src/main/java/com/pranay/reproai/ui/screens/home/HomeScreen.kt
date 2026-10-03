package com.pranay.reproai.ui.screens.home

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
import com.pranay.reproai.data.model.DebugSession

@Composable
fun HomeScreen(runnerSettings: @Composable () -> Unit = {}, runnerStatus: String = "None",
    recentSessions: List<DebugSession>, onStartSession: () -> Unit, onSessionSelected: (DebugSession) -> Unit,
    summaries: Map<String, HomeIncidentSummary> = emptyMap(),
    onViewReport: ((DebugSession) -> Unit)? = null,
    demoSettings: (@Composable () -> Unit)? = null,
    activeSession: DebugSession? = null, connectionLabel: String = "Offline", onOpenActive: () -> Unit = onStartSession) {
    var settingsExpanded by rememberSaveable { mutableStateOf(false) }
    var showAll by rememberSaveable { mutableStateOf(false) }
    PhonePage("ReproAI", "Mobile debugging workspace", badge=connectionLabel) {
        item {
            Surface(color=DarkSurface, shape=RoundedCornerShape(8.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Live debugging", style=MaterialTheme.typography.titleMedium)
                    Text(activeSession?.let { "Session RPA-${it.id.take(8).uppercase()}" } ?: "No active session",
                        style=MaterialTheme.typography.bodySmall, color=TextSecondary, modifier=Modifier.padding(vertical=8.dp))
                    PrimaryActionButton(if(activeSession == null) "Start session" else "Open live session",
                        if(activeSession == null) onStartSession else onOpenActive, icon=Icons.Default.PlayArrow)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                MetricTile("Incidents", recentSessions.size.toString(), Modifier.weight(1f))
                MetricTile("Verified", summaries.values.count { it.status == "VERIFIED" }.toString(), Modifier.weight(1f), TextPrimary)
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
                Text("Runner connection", modifier=Modifier.weight(1f), style=MaterialTheme.typography.titleMedium)
                TextButton(onClick={settingsExpanded = !settingsExpanded}) { Text(if(settingsExpanded) "Close" else "Configure") }
            }
            if(settingsExpanded) runnerSettings()
            if(settingsExpanded) demoSettings?.invoke()
            HorizontalDivider(color=DarkBorder, thickness=.5.dp)
            Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
                Text("Recent incidents", modifier=Modifier.weight(1f), style=MaterialTheme.typography.titleMedium)
                if(recentSessions.size > 3) TextButton(onClick={showAll = !showAll}) { Text(if(showAll) "Show recent" else "View all") }
            }
            if(recentSessions.isEmpty()) Text("Captured incidents will appear here.", color=TextSecondary,
                style=MaterialTheme.typography.bodyMedium, modifier=Modifier.padding(vertical=16.dp))
        }
        items(if(showAll) recentSessions else recentSessions.take(3), key={it.id}) { session ->
            val summary = summaries[session.id]
            val displayed = if(summary != null) session.copy(title=summary.title,issueTitle=summary.title,status=summary.status) else session
            IncidentRow(displayed, "RPA-${session.id.take(8).uppercase()}", {onSessionSelected(session)})
            onViewReport?.let { open -> TextButton(onClick={open(session)},modifier=Modifier.heightIn(min=48.dp)) {Text("View report")} }
        }
    }
}
