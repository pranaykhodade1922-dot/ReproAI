package com.pranay.reproai.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.pranay.reproai.data.model.DebugEvent
import com.pranay.reproai.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** System bar insets are consumed once by MainActivity's root surface. */
@Composable
fun PhonePage(title: String, subtitle: String? = null, onBack: (() -> Unit)? = null,
    badge: String? = null, bottom: @Composable () -> Unit = {},
    content: LazyListScope.() -> Unit) {
    Scaffold(modifier = Modifier.fillMaxSize().imePadding(), containerColor = DarkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { Column {
            ReproTopBar(title, subtitle, onBack, actions = { badge?.let { StatusBadge(it) } })
            HorizontalDivider(color = DarkBorder, thickness = .5.dp)
        } }, bottomBar = { bottom() }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            content = content)
    }
}

@Composable
fun BottomActions(primary: String, onPrimary: () -> Unit, secondary: String? = null,
    onSecondary: () -> Unit = {}, enabled: Boolean = true, loading: Boolean = false) {
    Surface(color = DarkSurface) { Column(Modifier.fillMaxWidth()) {
        HorizontalDivider(color = DarkBorder, thickness = .5.dp)
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            PrimaryActionButton(primary, onPrimary, enabled = enabled, isLoading = loading)
            if (secondary != null) TextButton(onClick = onSecondary,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(secondary) }
        }
    } }
}

@Composable
fun SectionLabel(text: String, trailing: String? = null) {
    Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        trailing?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = TextSecondary) }
    }
}

@Composable
fun TechnicalText(text: String, modifier: Modifier = Modifier, color: androidx.compose.ui.graphics.Color = TextPrimary) {
    Text(text, modifier, color = color, fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 18.sp)
}

@Composable
fun MetadataRow(label: String, value: String, technical: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary,
            modifier = Modifier.weight(.38f))
        Text(value, style = MaterialTheme.typography.bodySmall,
            fontFamily = if (technical) FontFamily.Monospace else FontFamily.SansSerif,
            modifier = Modifier.weight(.62f))
    }
}

fun eventSource(event: DebugEvent): String = event.metadata["source"] ?:
    if (event.type.name.contains("NETWORK") || event.type.name.contains("ORIENTATION") ||
        event.type.name.contains("APP_")) "DEVICE" else "REPROAI"

fun eventTime(timestamp: Long, milliseconds: Boolean = false): String =
    SimpleDateFormat(if (milliseconds) "HH:mm:ss.SSS" else "HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

@Composable
fun LogRow(event: DebugEvent, details: Boolean = false) {
    var expanded by rememberSaveable(event.id) { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TechnicalText(eventTime(event.timestamp, details), color = TextSecondary)
            Spacer(Modifier.width(8.dp))
            Text(eventSource(event), style = MaterialTheme.typography.labelSmall, color = PrimaryBlue,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            if (details) Text(event.type.name, style = MaterialTheme.typography.labelSmall,
                color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f))
        }
        TechnicalText(event.title, Modifier.padding(top = 3.dp))
        if (details) {
            if (event.description.isNotBlank()) Text(event.description, style = MaterialTheme.typography.bodySmall,
                color = TextSecondary, modifier = Modifier.padding(top = 3.dp))
            if (event.metadata.isNotEmpty()) {
                TextButton(onClick = { expanded = !expanded }, contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.heightIn(min = 48.dp)) { Text(if (expanded) "Hide details" else "Details") }
                if (expanded) event.metadata.toSortedMap().forEach { (key, value) -> MetadataRow(key, value, true) }
            }
        }
    }
    HorizontalDivider(color = DarkBorder, thickness = .5.dp)
}

@Composable
fun NumberedRow(index: Int, title: String, description: String? = null) {
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.Top) {
        TechnicalText("%02d".format(index), Modifier.padding(end = 12.dp), TextSecondary)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (!description.isNullOrBlank()) Text(description, style = MaterialTheme.typography.bodySmall,
                color = TextSecondary, modifier = Modifier.padding(top = 3.dp))
        }
    }
    HorizontalDivider(color = DarkBorder, thickness = .5.dp)
}
