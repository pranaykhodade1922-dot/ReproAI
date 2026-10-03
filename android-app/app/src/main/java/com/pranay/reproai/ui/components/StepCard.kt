package com.pranay.reproai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pranay.reproai.data.model.ReproductionStep
import com.pranay.reproai.data.model.TestStepProgress
import com.pranay.reproai.data.model.TestStepStatus
import com.pranay.reproai.ui.theme.DarkBorder
import com.pranay.reproai.ui.theme.DarkSurface
import com.pranay.reproai.ui.theme.PrimaryIndigo
import com.pranay.reproai.ui.theme.SecondaryCyan
import com.pranay.reproai.ui.theme.StatusError
import com.pranay.reproai.ui.theme.StatusSuccess
import com.pranay.reproai.ui.theme.TextMuted
import com.pranay.reproai.ui.theme.TextPrimary
import com.pranay.reproai.ui.theme.TextSecondary

@Composable
fun StepCard(
    step: ReproductionStep,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(PrimaryIndigo.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${step.stepNumber}",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryIndigo
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = step.title,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                if (step.description.isNotEmpty()) {
                    Text(
                        text = step.description,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun RunningStepCard(
    progress: TestStepProgress,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusBg) = when (progress.status) {
        TestStepStatus.COMPLETED -> StatusSuccess to StatusSuccess.copy(alpha = 0.15f)
        TestStepStatus.RUNNING -> SecondaryCyan to SecondaryCyan.copy(alpha = 0.15f)
        TestStepStatus.PENDING -> TextMuted to DarkBorder
        TestStepStatus.FAILED -> StatusError to StatusError.copy(alpha = 0.15f)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, if (progress.status == TestStepStatus.RUNNING) SecondaryCyan else DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = progress.title,
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                color = if (progress.status == TestStepStatus.PENDING) TextMuted else TextPrimary,
                modifier = Modifier.weight(1f)
            )

            when (progress.status) {
                TestStepStatus.COMPLETED -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = StatusSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                }
                TestStepStatus.RUNNING -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = SecondaryCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "running",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SecondaryCyan
                        )
                    }
                }
                TestStepStatus.PENDING -> {
                    Text(
                        text = "pending",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }
                TestStepStatus.FAILED -> {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Failed",
                        tint = StatusError,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
