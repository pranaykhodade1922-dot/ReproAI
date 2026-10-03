package com.pranay.reproai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pranay.reproai.data.model.DebugSession
import com.pranay.reproai.ui.theme.DarkBorder
import com.pranay.reproai.ui.theme.DarkSurface
import com.pranay.reproai.ui.theme.StatusError
import com.pranay.reproai.ui.theme.StatusSuccess
import com.pranay.reproai.ui.theme.TextMuted
import com.pranay.reproai.ui.theme.TextPrimary
import com.pranay.reproai.ui.theme.TextSecondary

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val isVerified = status.contains("Verified", ignoreCase = true)
    val badgeColor = if (isVerified) StatusSuccess else StatusError
    val bgContainer = badgeColor.copy(alpha = 0.15f)

    Box(
        modifier = modifier
            .background(color = bgContainer, shape = RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(badgeColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status,
                color = badgeColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SessionCard(
    session: DebugSession,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.title,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status = session.status)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.category,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = session.timestamp,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
            }
        }
    }
}
