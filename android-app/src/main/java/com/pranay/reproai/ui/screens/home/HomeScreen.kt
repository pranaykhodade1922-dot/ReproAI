package com.pranay.reproai.ui.screens.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pranay.reproai.data.model.DebugSession
import com.pranay.reproai.ui.components.PrimaryButton
import com.pranay.reproai.ui.components.SessionCard
import com.pranay.reproai.ui.theme.DarkBackground
import com.pranay.reproai.ui.theme.DarkBorder
import com.pranay.reproai.ui.theme.DarkSurface
import com.pranay.reproai.ui.theme.PrimaryIndigo
import com.pranay.reproai.ui.theme.SecondaryCyan
import com.pranay.reproai.ui.theme.TextMuted
import com.pranay.reproai.ui.theme.TextPrimary
import com.pranay.reproai.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    recentSessions: List<DebugSession>,
    onStartSession: () -> Unit,
    onSessionSelected: (DebugSession) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // App Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(PrimaryIndigo.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    tint = PrimaryIndigo,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "ReproAI",
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Turn mobile bugs into reproducible tests.",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // CTA Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "READY TO DEBUG",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryCyan,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Capture system traces, logs, and network events in real-time.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(18.dp))
                PrimaryButton(
                    text = "START DEBUG SESSION",
                    onClick = onStartSession,
                    icon = Icons.Default.PlayArrow
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Recent Sessions Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "RECENT SESSIONS",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.8.sp
            )
            Text(
                text = "${recentSessions.size} saved",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Recent Sessions List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(recentSessions) { session ->
                SessionCard(
                    session = session,
                    onClick = { onSessionSelected(session) }
                )
            }
        }
    }
}
