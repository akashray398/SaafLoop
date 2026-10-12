package com.example.saafloop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saafloop.ui.theme.NeoControlledCyan
import com.example.saafloop.ui.theme.NeoElectricLime
import com.example.saafloop.ui.theme.NeoError
import com.example.saafloop.ui.theme.NeoMint
import com.example.saafloop.ui.theme.NeoWarning

/**
 * Reusable semantic status badge for Neo-Eco Futurism status communication.
 * Combines status text, color token, and icon (never color alone).
 */
@Composable
fun NeoStatusBadge(
    statusText: String,
    modifier: Modifier = Modifier,
    category: String = "INFO" // "SUCCESS", "WARNING", "INFO", "ERROR"
) {
    val (backgroundColor, textColor, icon) = when (category.uppercase()) {
        "SUCCESS", "RESOLVED", "VERIFIED_CLEAN" -> Triple(
            NeoMint.copy(alpha = 0.2f),
            NeoMint,
            Icons.Default.CheckCircle
        )
        "WARNING", "IN_PROGRESS", "PENDING" -> Triple(
            NeoWarning.copy(alpha = 0.2f),
            NeoWarning,
            Icons.Default.Schedule
        )
        "ERROR", "CRITICAL", "REJECTED" -> Triple(
            NeoError.copy(alpha = 0.2f),
            NeoError,
            Icons.Default.Warning
        )
        else -> Triple(
            NeoControlledCyan.copy(alpha = 0.2f),
            NeoControlledCyan,
            Icons.Default.Info
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
