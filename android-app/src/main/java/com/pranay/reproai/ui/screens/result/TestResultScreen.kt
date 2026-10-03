package com.pranay.reproai.ui.screens.result

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
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
import com.pranay.reproai.data.model.TestResultData
import com.pranay.reproai.ui.components.PrimaryButton
import com.pranay.reproai.ui.components.SecondaryButton
import com.pranay.reproai.ui.theme.DarkBackground
import com.pranay.reproai.ui.theme.DarkBorder
import com.pranay.reproai.ui.theme.DarkSurface
import com.pranay.reproai.ui.theme.StatusError
import com.pranay.reproai.ui.theme.StatusSuccess
import com.pranay.reproai.ui.theme.TextMuted
import com.pranay.reproai.ui.theme.TextPrimary
import com.pranay.reproai.ui.theme.TextSecondary

@Composable
fun TestResultScreen(
    result: TestResultData,
    onVerifyFix: () -> Unit,
    onViewTimeline: () -> Unit,
    onReturnHome: () -> Unit
) {
    val isReproduced = result.isBugReproduced
    val statusColor = if (isReproduced) StatusError else StatusSuccess
    val statusIcon = if (isReproduced) Icons.Default.Error else Icons.Default.CheckCircle

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Status Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(statusColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = result.statusTitle,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result.detailsMessage,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Result Details Breakdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "EXPECTED RESULT",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
                Text(
                    text = result.expectedText,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    color = StatusSuccess
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ACTUAL RESULT",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
                Text(
                    text = result.actualText,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isReproduced) StatusError else StatusSuccess
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "EXECUTION TIME",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
                Text(
                    text = result.durationText,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action Buttons
        if (isReproduced) {
            PrimaryButton(
                text = "VERIFY FIX",
                onClick = onVerifyFix,
                icon = Icons.Default.Refresh
            )

            Spacer(modifier = Modifier.height(12.dp))

            SecondaryButton(
                text = "VIEW TIMELINE",
                onClick = onViewTimeline,
                icon = Icons.Default.Timeline
            )
        } else {
            PrimaryButton(
                text = "RETURN HOME",
                onClick = onReturnHome,
                icon = Icons.Default.Home
            )
        }
    }
}
