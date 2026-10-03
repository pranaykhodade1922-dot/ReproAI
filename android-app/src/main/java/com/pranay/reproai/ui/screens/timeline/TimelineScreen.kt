package com.pranay.reproai.ui.screens.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pranay.reproai.data.model.DebugEvent
import com.pranay.reproai.ui.components.PrimaryButton
import com.pranay.reproai.ui.components.SecondaryButton
import com.pranay.reproai.ui.components.TimelineItem
import com.pranay.reproai.ui.theme.DarkBackground
import com.pranay.reproai.ui.theme.SecondaryCyan
import com.pranay.reproai.ui.theme.TextMuted
import com.pranay.reproai.ui.theme.TextPrimary
import com.pranay.reproai.ui.theme.TextSecondary

@Composable
fun TimelineScreen(
    events: List<DebugEvent>,
    onBack: () -> Unit,
    onGenerateReproduction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Header
        Text(
            text = "EVENT TRACE LOGS",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = SecondaryCyan,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Failure Timeline",
            fontSize = 24.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${events.size} captured events leading to the failure state",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Vertical Timeline List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            itemsIndexed(events) { index, event ->
                TimelineItem(
                    event = event,
                    isLast = index == events.lastIndex
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        PrimaryButton(
            text = "GENERATE REPRODUCTION",
            onClick = onGenerateReproduction,
            icon = Icons.Default.PlayArrow
        )

        Spacer(modifier = Modifier.height(10.dp))

        SecondaryButton(
            text = "BACK",
            onClick = onBack,
            icon = Icons.Default.ArrowBack
        )
    }
}
