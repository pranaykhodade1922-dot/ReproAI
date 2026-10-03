package com.pranay.reproai.ui.screens.analysis

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timeline
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
import com.pranay.reproai.data.model.AIAnalysis
import com.pranay.reproai.ui.components.PrimaryButton
import com.pranay.reproai.ui.components.SecondaryButton
import com.pranay.reproai.ui.theme.DarkBackground
import com.pranay.reproai.ui.theme.DarkBorder
import com.pranay.reproai.ui.theme.DarkSurface
import com.pranay.reproai.ui.theme.DarkSurfaceVariant
import com.pranay.reproai.ui.theme.PrimaryIndigo
import com.pranay.reproai.ui.theme.SecondaryCyan
import com.pranay.reproai.ui.theme.StatusSuccess
import com.pranay.reproai.ui.theme.TextMuted
import com.pranay.reproai.ui.theme.TextPrimary
import com.pranay.reproai.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiAnalysisScreen(
    analysis: AIAnalysis,
    onViewTimeline: () -> Unit,
    onGenerateReproduction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "AI DIAGNOSTICS",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryCyan,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "ReproAI Analysis",
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Confidence Score Chip
            Box(
                modifier = Modifier
                    .background(StatusSuccess.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = StatusSuccess
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${analysis.confidencePercentage}% Match",
                        color = StatusSuccess,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Issue Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "IDENTIFIED ISSUE",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = analysis.issueName,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "PROBABLE TRIGGER",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = analysis.probableTrigger,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Likely Cause Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ROOT CAUSE DIAGNOSIS",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = analysis.likelyCause,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Relevant Components
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PrimaryIndigo
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RELEVANT COMPONENTS",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    analysis.relevantComponents.forEach { comp ->
                        Box(
                            modifier = Modifier
                                .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = comp,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        PrimaryButton(
            text = "GENERATE REPRODUCTION",
            onClick = onGenerateReproduction,
            icon = Icons.Default.PlayArrow
        )

        Spacer(modifier = Modifier.height(12.dp))

        SecondaryButton(
            text = "VIEW FAILURE TIMELINE",
            onClick = onViewTimeline,
            icon = Icons.Default.Timeline
        )
    }
}
