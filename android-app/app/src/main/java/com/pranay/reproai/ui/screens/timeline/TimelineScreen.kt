package com.pranay.reproai.ui.screens.timeline

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
fun TimelineScreen(events: List<DebugEvent>, onBack: () -> Unit, onGenerateReproduction: () -> Unit) {
    var selected by rememberSaveable { mutableStateOf("All") }
    val filtered = events.filter { event -> when(selected) {
        "Device" -> eventSource(event) == "DEVICE"
        "App" -> eventSource(event) == "DEMOSHOP" || eventSource(event) == "APP"
        "Network" -> event.type.name.contains("NETWORK")
        "API" -> event.type.name.startsWith("API_")
        "Errors" -> event.type.name.contains("ERROR") || event.title.contains("401") || event.title.contains("EXPIRED")
        else -> true
    } }
    PhonePage("Event timeline","${filtered.size} of ${events.size} events",onBack,
        bottom={BottomActions("Open reproduction",onGenerateReproduction)}) {
        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                listOf("All","Device","App","Network","API","Errors").forEach { filter ->
                    FilterChip(selected=selected==filter,onClick={selected=filter},label={Text(filter)},modifier=Modifier.heightIn(min=48.dp))
                }
            }
            if(filtered.isEmpty()) Text("No events match this filter.",style=MaterialTheme.typography.bodyMedium,color=TextSecondary,modifier=Modifier.padding(vertical=16.dp))
        }
        items(filtered,key={it.id}) {LogRow(it,details=true)}
    }
}
