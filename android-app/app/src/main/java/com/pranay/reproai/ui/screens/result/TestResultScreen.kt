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
import androidx.compose.material.icons.filled.Description
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
import com.pranay.reproai.ui.components.PrimaryActionButton
import com.pranay.reproai.ui.components.ReproTopBar
import com.pranay.reproai.ui.components.SecondaryActionButton
import com.pranay.reproai.ui.components.StatusBadge
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
    onCreateReport: () -> Unit = {},
    onReturnHome: () -> Unit
) {
    val isReproduced = result.isBugReproduced
    val statusColor = if (isReproduced) StatusError else StatusSuccess
    val statusText = if (isReproduced) "BUG REPRODUCED" else "FIX VERIFIED"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        ReproTopBar(
            title = "Reproduction Result",
            subtitle = "Run Execution Complete"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StatusBadge(status = statusText)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = result.statusTitle,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = result.detailsMessage,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "EXPECTED RESULT:", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                    Text(text = result.expectedText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "OBSERVED RESULT:", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                    Text(text = result.actualText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = statusColor)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "EXECUTION TIME:", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                    Text(text = result.durationText, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (isReproduced) {
            PrimaryActionButton(
                text = "VERIFY FIX",
                onClick = onVerifyFix,
                icon = Icons.Default.Refresh
            )

            Spacer(modifier = Modifier.height(8.dp))

            SecondaryActionButton(
                text = "CREATE REPORT",
                onClick = onCreateReport,
                icon = Icons.Default.Description
            )

            Spacer(modifier = Modifier.height(8.dp))

            SecondaryActionButton(
                text = "VIEW TIMELINE",
                onClick = onViewTimeline,
                icon = Icons.Default.Timeline
            )
        } else {
            PrimaryActionButton(
                text = "RETURN TO SESSIONS",
                onClick = onReturnHome,
                icon = Icons.Default.Home
            )
        }
    }
}
