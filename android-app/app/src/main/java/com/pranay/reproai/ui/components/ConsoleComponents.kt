package com.pranay.reproai.ui.components
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.pranay.reproai.data.model.*

@Composable
fun ReproTopBar(title: String, subtitle: String? = null, onBack: (() -> Unit)? = null,
    isRecording: Boolean = false, actions: @Composable (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal=16.dp, vertical=10.dp), verticalAlignment=Alignment.CenterVertically) {
        if (onBack != null) IconButton(onClick=onBack, modifier=Modifier.size(48.dp)) {
            Icon(Icons.Default.ArrowBack, "Back")
        }
        Column(Modifier.weight(1f)) {
            Text(title, style=MaterialTheme.typography.headlineMedium, maxLines=2, overflow=TextOverflow.Ellipsis)
            subtitle?.let { Text(it, style=MaterialTheme.typography.bodySmall, color=TextSecondary,
                maxLines=1, overflow=TextOverflow.Ellipsis) }
        }
        Spacer(Modifier.width(8.dp))
        if (isRecording) StatusBadge("REC") else actions?.invoke()
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val color = when {
        status.contains("verified", true) || status.equals("Ready", true) || status.equals("Connected", true) -> StatusSuccess
        status.contains("reproduced", true) || status.contains("error", true) || status.contains("failed", true) || status == "REC" -> StatusError
        status.contains("running", true) -> PrimaryBlue
        else -> TextSecondary
    }
    Text(status.uppercase(), modifier.background(color.copy(alpha=.12f), RoundedCornerShape(4.dp))
        .padding(horizontal=7.dp, vertical=3.dp), color=color, style=MaterialTheme.typography.labelSmall,
        maxLines=1, overflow=TextOverflow.Ellipsis)
}

@Composable
fun IncidentRow(session: DebugSession, incidentId: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().clickable(onClick=onClick).heightIn(min=80.dp).padding(vertical=10.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically) {
            TechnicalText(incidentId, Modifier.weight(1f), PrimaryBlue)
            StatusBadge(session.status)
        }
        Text(session.analysisResult?.issueTitle ?: session.issueTitle ?: session.title,
            style=MaterialTheme.typography.titleLarge, maxLines=2, overflow=TextOverflow.Ellipsis,
            modifier=Modifier.padding(top=4.dp))
        val source = if(session.events.any { it.metadata["source"] == "DEMOSHOP" }) "DemoShop" else "Device"
        val minutes = ((System.currentTimeMillis() - (session.bugCapturedAt ?: session.startedAt))/60000).coerceAtLeast(0)
        val age = if(minutes == 0L) "Just now" else if(minutes < 60) "${minutes}m" else "${minutes/60}h"
        val api = session.events.lastOrNull { it.metadata["statusCode"] != null }?.metadata?.get("statusCode")
        Text("$source / ${session.events.size} events / ${api?.let { "HTTP $it / " }.orEmpty()}$age",
            style=MaterialTheme.typography.bodySmall, color=TextSecondary,
            maxLines=1, overflow=TextOverflow.Ellipsis, modifier=Modifier.padding(top=4.dp))
    }
    HorizontalDivider(color=DarkBorder, thickness=.5.dp)
}

@Composable
fun CodeChip(text: String, modifier: Modifier = Modifier) { TechnicalText(text, modifier.padding(vertical=4.dp)) }

@Composable
fun EventRow(event: DebugEvent, modifier: Modifier = Modifier) { Box(modifier) { LogRow(event) } }

@Composable
fun MetricTile(label: String, value: String, modifier: Modifier = Modifier, accentColor: Color = PrimaryBlue) {
    Column(modifier.padding(vertical=8.dp)) {
        Text(label, style=MaterialTheme.typography.bodySmall, color=TextSecondary)
        Text(value, style=MaterialTheme.typography.titleMedium, color=accentColor)
    }
}

@Composable
fun ExecutionStep(progress: TestStepProgress, index: Int, modifier: Modifier = Modifier) {
    val mark = when(progress.status) { TestStepStatus.COMPLETED -> "\u2713"; TestStepStatus.RUNNING -> "\u2192"; TestStepStatus.FAILED -> "!"; else -> "\u00b7" }
    Box(modifier) { NumberedRow(index+1, "$mark ${progress.title}") }
}

@Composable
fun PrimaryActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null,
    isLoading: Boolean = false, enabled: Boolean = true, containerColor: Color = Color(0xFF334B68)) {
    Button(onClick=onClick, modifier=modifier.fillMaxWidth().heightIn(min=48.dp), enabled=enabled && !isLoading,
        shape=RoundedCornerShape(8.dp), colors=ButtonDefaults.buttonColors(containerColor=containerColor, contentColor=TextPrimary),
        contentPadding=PaddingValues(horizontal=16.dp, vertical=12.dp)) {
        if(isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth=2.dp)
        else { if(icon != null) { Icon(icon, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)) }; Text(text) }
    }
}

@Composable
fun SecondaryActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true) {
    TextButton(onClick=onClick, modifier=modifier.fillMaxWidth().heightIn(min=48.dp), enabled=enabled) {
        if(icon != null) { Icon(icon, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)) }; Text(text)
    }
}
