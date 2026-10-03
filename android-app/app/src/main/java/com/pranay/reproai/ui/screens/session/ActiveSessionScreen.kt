package com.pranay.reproai.ui.screens.session

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
import com.pranay.reproai.data.model.DebugEvent

@Composable
fun ActiveSessionScreen(timerSeconds: Int, eventsCount: Int, currentScreen: String, networkType: String,
    appState: String, memoryState: String, events: List<DebugEvent> = emptyList(), onCaptureBug: () -> Unit,
    onStopSession: () -> Unit, onSimulatePaymentBug: () -> Unit = {}, onExportJson: () -> Unit = {},
    sessionId: String? = null, orientation: String = "Unknown", recording: Boolean = true, onBack: (() -> Unit)? = null) {
    var menu by remember { mutableStateOf(false) }
    PhonePage("Live session", sessionId?.let { "RPA-${it.take(8).uppercase()}" }, onBack,
        if(recording) "REC" else "Stopped", bottom={BottomActions("Capture issue", onCaptureBug, "Stop session", onStopSession)}) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
                Text("%02d:%02d".format(timerSeconds/60,timerSeconds%60), fontFamily=FontFamily.Monospace,
                    style=MaterialTheme.typography.headlineMedium, modifier=Modifier.weight(1f))
                Box {
                    IconButton(onClick={menu=true}) {Icon(Icons.Default.MoreVert, "Session actions")}
                    DropdownMenu(expanded=menu,onDismissRequest={menu=false}) {
                        DropdownMenuItem(text={Text("Export session JSON")},onClick={menu=false;onExportJson()})
                        DropdownMenuItem(text={Text("Simulate demo events")},onClick={menu=false;onSimulatePaymentBug()})
                    }
                }
            }
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical=8.dp), horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                listOf(networkType,orientation,appState,"$eventsCount events").forEach {Text(it, style=MaterialTheme.typography.bodySmall,color=TextSecondary)}
            }
            MetadataRow("Screen", currentScreen)
            SectionLabel("Live events", "Newest first")
            if(events.isEmpty()) Text("Waiting for device and app events...",color=TextSecondary,style=MaterialTheme.typography.bodyMedium)
        }
        items(events.asReversed(),key={it.id}) { LogRow(it) }
    }
}
