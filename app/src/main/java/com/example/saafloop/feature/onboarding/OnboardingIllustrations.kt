package com.example.saafloop.feature.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Custom Compose Canvas illustration for Step 1: "Spot Public Waste"
 * Displays a map grid, location pin, and magnifying/camera lens in SaafLoop palette.
 */
@Composable
fun SpotItIllustration(
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val w = size.width
            val h = size.height

            // 1. Draw rounded map background card
            drawRoundRect(
                color = surfaceColor,
                topLeft = Offset(w * 0.1f, h * 0.1f),
                size = Size(w * 0.8f, h * 0.8f),
                cornerRadius = CornerRadius(24f, 24f)
            )

            // 2. Draw map grid lines
            val gridColor = primaryColor.copy(alpha = 0.15f)
            drawLine(gridColor, Offset(w * 0.2f, h * 0.35f), Offset(w * 0.8f, h * 0.35f), strokeWidth = 3f)
            drawLine(gridColor, Offset(w * 0.2f, h * 0.65f), Offset(w * 0.8f, h * 0.65f), strokeWidth = 3f)
            drawLine(gridColor, Offset(w * 0.35f, h * 0.2f), Offset(w * 0.35f, h * 0.8f), strokeWidth = 3f)
            drawLine(gridColor, Offset(w * 0.65f, h * 0.2f), Offset(w * 0.65f, h * 0.8f), strokeWidth = 3f)

            // 3. Draw location pin
            val pinCenterX = w * 0.5f
            val pinTipY = h * 0.68f
            val pinPath = Path().apply {
                moveTo(pinCenterX, pinTipY)
                cubicTo(
                    w * 0.28f, h * 0.45f,
                    w * 0.28f, h * 0.25f,
                    pinCenterX, h * 0.22f
                )
                cubicTo(
                    w * 0.72f, h * 0.25f,
                    w * 0.72f, h * 0.45f,
                    pinCenterX, pinTipY
                )
                close()
            }
            drawPath(pinPath, color = primaryColor)

            // Pin inner circle
            drawCircle(
                color = Color.White,
                radius = w * 0.08f,
                center = Offset(pinCenterX, h * 0.38f)
            )

            // 4. Draw camera lens / target scope icon at bottom right
            val scopeCenterX = w * 0.68f
            val scopeCenterY = h * 0.65f
            drawCircle(
                color = tertiaryColor,
                radius = w * 0.12f,
                center = Offset(scopeCenterX, scopeCenterY)
            )
            drawCircle(
                color = surfaceColor,
                radius = w * 0.08f,
                center = Offset(scopeCenterX, scopeCenterY)
            )
            drawCircle(
                color = tertiaryColor,
                radius = w * 0.04f,
                center = Offset(scopeCenterX, scopeCenterY)
            )

            // Pulse ring around target
            drawCircle(
                color = secondaryColor,
                radius = w * 0.16f,
                center = Offset(scopeCenterX, scopeCenterY),
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Custom Compose Canvas illustration for Step 2: "Solve It Together"
 * Displays team coordination icons, cleanup broom/hands, and response arcs.
 */
@Composable
fun SolveItIllustration(
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val w = size.width
            val h = size.height

            // 1. Central team hub circle
            drawCircle(
                color = surfaceColor,
                radius = w * 0.38f,
                center = Offset(w * 0.5f, h * 0.5f)
            )

            // 2. Connecting response ring
            drawArc(
                color = secondaryColor,
                startAngle = 0f,
                sweepAngle = 300f,
                useCenter = false,
                topLeft = Offset(w * 0.18f, h * 0.18f),
                size = Size(w * 0.64f, h * 0.64f),
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )

            // 3. Team Member Node 1 (Top Left)
            drawCircle(
                color = primaryColor,
                radius = w * 0.09f,
                center = Offset(w * 0.32f, h * 0.35f)
            )

            // Team Member Node 2 (Top Right)
            drawCircle(
                color = primaryColor,
                radius = w * 0.09f,
                center = Offset(w * 0.68f, h * 0.35f)
            )

            // 4. Central Cleanup Broom / Hand Action Icon
            val handlePath = Path().apply {
                moveTo(w * 0.38f, h * 0.68f)
                lineTo(w * 0.58f, h * 0.48f)
            }
            drawPath(
                path = handlePath,
                color = tertiaryColor,
                style = Stroke(width = 10f, cap = StrokeCap.Round)
            )

            val bristlePath = Path().apply {
                moveTo(w * 0.38f, h * 0.68f)
                lineTo(w * 0.32f, h * 0.78f)
                lineTo(w * 0.48f, h * 0.78f)
                close()
            }
            drawPath(
                path = bristlePath,
                color = tertiaryColor
            )

            // Sparkle / motion rays
            drawLine(
                color = secondaryColor,
                start = Offset(w * 0.55f, h * 0.7f),
                end = Offset(w * 0.65f, h * 0.75f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = secondaryColor,
                start = Offset(w * 0.58f, h * 0.62f),
                end = Offset(w * 0.7f, h * 0.64f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Custom Compose Canvas illustration for Step 3: "Keep It Clean"
 * Displays a clean verification shield, continuous loop checkmark, and radiant rays.
 */
@Composable
fun KeepItCleanIllustration(
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val w = size.width
            val h = size.height

            // 1. Draw protective shield background
            val shieldPath = Path().apply {
                moveTo(w * 0.5f, h * 0.12f)
                lineTo(w * 0.82f, h * 0.24f)
                cubicTo(
                    w * 0.82f, h * 0.58f,
                    w * 0.62f, h * 0.78f,
                    w * 0.5f, h * 0.88f
                )
                cubicTo(
                    w * 0.38f, h * 0.78f,
                    w * 0.18f, h * 0.58f,
                    w * 0.18f, h * 0.24f
                )
                close()
            }
            drawPath(path = shieldPath, color = surfaceColor)
            drawPath(
                path = shieldPath,
                color = secondaryColor,
                style = Stroke(width = 6f)
            )

            // 2. Draw SaafLoop verification loop inside shield
            val centerX = w * 0.5f
            val centerY = h * 0.45f
            val radius = w * 0.18f
            drawArc(
                color = secondaryColor,
                startAngle = 30f,
                sweepAngle = 300f,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )

            // 3. Draw Checkmark inside loop
            val checkPath = Path().apply {
                moveTo(w * 0.42f, h * 0.45f)
                lineTo(w * 0.48f, h * 0.52f)
                lineTo(w * 0.6f, h * 0.38f)
            }
            drawPath(
                path = checkPath,
                color = primaryColor,
                style = Stroke(width = 9f, cap = StrokeCap.Round)
            )

            // 4. Radiant verification stars / sparkles top right & bottom left
            val starColor = tertiaryColor
            drawCircle(color = starColor, radius = 8f, center = Offset(w * 0.22f, h * 0.18f))
            drawCircle(color = starColor, radius = 12f, center = Offset(w * 0.8f, h * 0.16f))
            drawCircle(color = starColor, radius = 10f, center = Offset(w * 0.82f, h * 0.72f))
        }
    }
}
