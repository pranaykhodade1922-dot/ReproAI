package com.pranay.reproai.ui.screens.running

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pranay.reproai.data.model.TestStepProgress
import com.pranay.reproai.data.model.TestStepStatus
import com.pranay.reproai.ui.components.RunningStepCard
import com.pranay.reproai.ui.theme.DarkBackground
import com.pranay.reproai.ui.theme.DarkBorder
import com.pranay.reproai.ui.theme.DarkSurface
import com.pranay.reproai.ui.theme.PrimaryIndigo
import com.pranay.reproai.ui.theme.SecondaryCyan
import com.pranay.reproai.ui.theme.TextMuted
import com.pranay.reproai.ui.theme.TextPrimary
import com.pranay.reproai.ui.theme.TextSecondary

@Composable
fun RunningTestScreen(
    testSteps: List<TestStepProgress>,
    connectedDevice: String = "iQOO Device",
    testName: String = "Payment Network Transition"
) {
    val completedCount = testSteps.count { it.status == TestStepStatus.COMPLETED }
    val progressFraction = completedCount.toFloat() / testSteps.size.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Header
        Text(
            text = "AUTOMATED RUNNER",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = SecondaryCyan,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Running Reproduction Test",
            fontSize = 22.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Device and Test Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "CONNECTED DEVICE",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                        Text(
                            text = connectedDevice,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryCyan
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TEST SUITE",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                        Text(
                            text = testName,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = PrimaryIndigo,
                    trackColor = DarkBorder
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Execution Steps
        Text(
            text = "EXECUTION PROGRESS ($completedCount/${testSteps.size})",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.8.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(testSteps) { step ->
                RunningStepCard(progress = step)
            }
        }
    }
}
