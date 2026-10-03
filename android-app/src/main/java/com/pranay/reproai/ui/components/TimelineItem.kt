package com.pranay.reproai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.TouchApp
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
import com.pranay.reproai.data.model.DebugEvent
import com.pranay.reproai.data.model.DebugEventType
import com.pranay.reproai.ui.theme.DarkBorder
import com.pranay.reproai.ui.theme.DarkSurface
import com.pranay.reproai.ui.theme.PrimaryIndigo
import com.pranay.reproai.ui.theme.SecondaryCyan
import com.pranay.reproai.ui.theme.StatusError
import com.pranay.reproai.ui.theme.StatusWarning
import com.pranay.reproai.ui.theme.TextMuted
import com.pranay.reproai.ui.theme.TextPrimary
import com.pranay.reproai.ui.theme.TextSecondary

@Composable
fun TimelineItem(
    event: DebugEvent,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    val (icon, tintColor) = when (event.type) {
        DebugEventType.UI_ACTION -> Icons.Default.TouchApp to PrimaryIndigo
        DebugEventType.NETWORK_CHANGE -> Icons.Default.NetworkCheck to SecondaryCyan
        DebugEventType.RETRY -> Icons.Default.Cached to StatusWarning
        DebugEventType.ERROR -> Icons.Default.ErrorOutline to StatusError
        DebugEventType.SYSTEM_EVENT -> Icons.Default.BugReport to TextSecondary
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Node column with line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(tintColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = tintColor
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(DarkBorder)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Content Card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.title,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = event.timestamp,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }
                if (event.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.description,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
