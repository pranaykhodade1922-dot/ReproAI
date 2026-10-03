package com.pranay.reproai.ui.screens.session

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pranay.reproai.ui.components.MetricCard
import com.pranay.reproai.ui.components.PrimaryButton
import com.pranay.reproai.ui.components.SecondaryButton
import com.pranay.reproai.ui.theme.DarkBackground
import com.pranay.reproai.ui.theme.DarkBorder
import com.pranay.reproai.ui.theme.DarkSurface
import com.pranay.reproai.ui.theme.PrimaryIndigo
import com.pranay.reproai.ui.theme.SecondaryCyan
import com.pranay.reproai.ui.theme.StatusError
import com.pranay.reproai.ui.theme.TextMuted
import com.pranay.reproai.ui.theme.TextPrimary
import com.pranay.reproai.ui.theme.TextSecondary

@Composable
fun ActiveSessionScreen(
    timerSeconds: Int,
    eventsCount: Int,
    currentScreen: String,
    networkType: String,
    appState: String,
    memoryState: String,
    onCaptureBug: () -> Unit,
    onStopSession: () -> Unit
) {
    val minutes = timerSeconds / 60
    val seconds = timerSeconds % 60
    val timeFormatted = String.format("%02d:%02d:%02d", minutes / 60, minutes % 60, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ACTIVE DEBUG SESSION",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "Live Monitoring",
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Recording indicator
            Box(
                modifier = Modifier
                    .background(StatusError.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(StatusError, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Recording",
                        color = StatusError,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Timer Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SESSION DURATION",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = timeFormatted,
                    fontSize = 36.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Metrics Grid (2 columns)
        Text(
            text = "CAPTURED METRICS",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.8.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MetricCard(
                label = "Events Captured",
                value = "$eventsCount",
                icon = Icons.Default.Timeline,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Current Screen",
                value = currentScreen,
                icon = Icons.Default.PhoneAndroid,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MetricCard(
                label = "Network",
                value = networkType,
                icon = Icons.Default.NetworkCheck,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "App State",
                value = appState,
                icon = Icons.Default.BugReport,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        MetricCard(
            label = "Memory Usage",
            value = memoryState,
            icon = Icons.Default.Memory,
            accentColor = SecondaryCyan
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Actions
        PrimaryButton(
            text = "CAPTURE BUG",
            onClick = onCaptureBug,
            icon = Icons.Default.BugReport
        )

        Spacer(modifier = Modifier.height(12.dp))

        SecondaryButton(
            text = "STOP SESSION",
            onClick = onStopSession,
            icon = Icons.Default.Stop
        )
    }
}
