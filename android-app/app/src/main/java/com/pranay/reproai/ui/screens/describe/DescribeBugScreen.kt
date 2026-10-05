package com.pranay.reproai.ui.screens.describe

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
fun DescribeBugScreen(descriptionText: String, isAnalyzing: Boolean, isListeningVoice: Boolean,
    onDescriptionChanged: (String) -> Unit, onVoiceToggle: () -> Unit, onAnalyzeBug: () -> Unit, onCancel: () -> Unit,
    sessionId: String? = null, events: List<DebugEvent> = emptyList()) {
    PhonePage("Capture issue", sessionId?.let { "RPA-${it.take(8).uppercase()}" }, onCancel,
        bottom={BottomActions("Analyze incident",onAnalyzeBug,"Cancel",onCancel,loading=isAnalyzing)}) {
        item {
            SectionLabel("Captured context")
            MetadataRow("Events", "${events.size} captured")
            MetadataRow("Last signal",(events.lastOrNull { it.type.name in listOf("ERROR","API_RESPONSE") } ?: events.lastOrNull())?.title ?: "No recorded events",true)
            SectionLabel("Describe what happened")
            OutlinedTextField(descriptionText,onDescriptionChanged,modifier=Modifier.fillMaxWidth(),minLines=5,
                placeholder={Text(com.pranay.reproai.ai.CaptureDescription.placeholder)},
                enabled=!isAnalyzing)
        }
    }
}
