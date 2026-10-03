package com.pranay.reproai.ui.screens.describe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pranay.reproai.ui.components.PrimaryButton
import com.pranay.reproai.ui.components.SecondaryButton
import com.pranay.reproai.ui.theme.DarkBackground
import com.pranay.reproai.ui.theme.DarkBorder
import com.pranay.reproai.ui.theme.DarkSurface
import com.pranay.reproai.ui.theme.DarkSurfaceVariant
import com.pranay.reproai.ui.theme.PrimaryIndigo
import com.pranay.reproai.ui.theme.SecondaryCyan
import com.pranay.reproai.ui.theme.TextMuted
import com.pranay.reproai.ui.theme.TextPrimary
import com.pranay.reproai.ui.theme.TextSecondary

@Composable
fun DescribeBugScreen(
    descriptionText: String,
    isAnalyzing: Boolean,
    isListeningVoice: Boolean,
    onDescriptionChanged: (String) -> Unit,
    onVoiceToggle: () -> Unit,
    onAnalyzeBug: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Header
        Text(
            text = "STEP 2 OF 5",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = SecondaryCyan,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "What went wrong?",
            fontSize = 24.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Describe the bug in plain language or use voice dictation. ReproAI will cross-reference with captured logs.",
            fontSize = 13.sp,
            color = TextSecondary,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Text Input Field
        OutlinedTextField(
            value = descriptionText,
            onValueChange = onDescriptionChanged,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            shape = RoundedCornerShape(14.dp),
            placeholder = {
                Text(
                    text = "e.g., Payment failed after switching networks...",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = PrimaryIndigo,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Voice Input Card Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onVoiceToggle() },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isListeningVoice) PrimaryIndigo.copy(alpha = 0.15f) else DarkSurface
            ),
            border = BorderStroke(1.dp, if (isListeningVoice) PrimaryIndigo else DarkBorder)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (isListeningVoice) PrimaryIndigo else DarkSurfaceVariant,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isListeningVoice) "Listening..." else "Tap to dictation bug description",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isListeningVoice) "Speak now..." else "Mock voice-to-text input",
                        fontSize = 12.sp,
                        color = if (isListeningVoice) PrimaryIndigo else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Actions
        PrimaryButton(
            text = "ANALYZE BUG",
            onClick = onAnalyzeBug,
            isLoading = isAnalyzing,
            icon = Icons.Default.AutoAwesome
        )

        Spacer(modifier = Modifier.height(12.dp))

        SecondaryButton(
            text = "CANCEL",
            onClick = onCancel,
            icon = Icons.Default.Close
        )
    }
}
