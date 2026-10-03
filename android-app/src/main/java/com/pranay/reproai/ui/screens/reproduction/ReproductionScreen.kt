package com.pranay.reproai.ui.screens.reproduction

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pranay.reproai.data.model.ReproductionStep
import com.pranay.reproai.data.model.TestScenario
import com.pranay.reproai.ui.components.PrimaryButton
import com.pranay.reproai.ui.components.StepCard
import com.pranay.reproai.ui.theme.DarkBackground
import com.pranay.reproai.ui.theme.DarkBorder
import com.pranay.reproai.ui.theme.DarkSurface
import com.pranay.reproai.ui.theme.SecondaryCyan
import com.pranay.reproai.ui.theme.StatusError
import com.pranay.reproai.ui.theme.StatusSuccess
import com.pranay.reproai.ui.theme.TextMuted
import com.pranay.reproai.ui.theme.TextPrimary
import com.pranay.reproai.ui.theme.TextSecondary

@Composable
fun ReproductionScreen(
    steps: List<ReproductionStep>,
    scenario: TestScenario,
    onGenerateTest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Header
        Text(
            text = "SCENARIO GENERATOR",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = SecondaryCyan,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Reproduction Generated",
            fontSize = 22.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${steps.size} steps identified to reproduce the issue",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Steps List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(steps) { step ->
                StepCard(step = step)
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                // Expected vs Actual Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "EXPECTED BEHAVIOR",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = StatusSuccess
                        )
                        Text(
                            text = scenario.expectedBehavior,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "ACTUAL BEHAVIOR",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = StatusError
                        )
                        Text(
                            text = scenario.actualBehavior,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button
        PrimaryButton(
            text = "GENERATE TEST",
            onClick = onGenerateTest,
            icon = Icons.Default.PlayArrow
        )
    }
}
