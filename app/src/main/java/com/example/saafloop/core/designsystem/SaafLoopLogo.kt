package com.example.saafloop.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.saafloop.ui.theme.NeoControlledCyan
import com.example.saafloop.ui.theme.NeoElectricLime
import com.example.saafloop.ui.theme.NeoMint

/**
 * Signature SaafLoop Logo matching the Neo-Eco Master Blueprint:
 * 1. Flowing ribbon loop (Emerald -> Cyan -> Electric Lime)
 * 2. Unfolding leaf at top right
 * 3. Luminous Location Pin Anchor
 * 4. City Skyline & Community Volunteer Silhouettes inside
 */
@Composable
fun SaafLoopLogo(
    modifier: Modifier = Modifier,
    stage: Int = 5,
    sparkProgress: Float = 1f,
    loopProgress: Float = 1f,
    leafProgress: Float = 1f,
    communityProgress: Float = 1f
) {
    Canvas(modifier = modifier.size(120.dp)) {
        val width = size.width
        val height = size.height
        val strokePx = width * 0.08f

        // Stage 1: Journey Spark / Path
        if (stage >= 1) {
            val sparkPath = Path().apply {
                moveTo(width * 0.25f, height * 0.78f)
                cubicTo(
                    width * 0.15f, height * 0.50f,
                    width * 0.30f, height * 0.20f,
                    width * 0.58f, height * 0.18f
                )
            }
            drawPath(
                path = sparkPath,
                brush = Brush.linearGradient(
                    colors = listOf(NeoControlledCyan, NeoElectricLime)
                ),
                style = Stroke(
                    width = strokePx * 0.7f,
                    cap = StrokeCap.Round
                ),
                alpha = sparkProgress.coerceIn(0f, 1f)
            )

            // Luminous Journey Spark Head
            drawCircle(
                color = NeoElectricLime,
                radius = strokePx * 0.8f,
                center = Offset(width * 0.58f, height * 0.18f),
                alpha = sparkProgress.coerceIn(0f, 1f)
            )
        }

        // Stage 2: Ribbon Loop Formation
        if (stage >= 2) {
            val outerLoopPath = Path().apply {
                moveTo(width * 0.50f, height * 0.18f)
                cubicTo(
                    width * 0.82f, height * 0.20f,
                    width * 0.88f, height * 0.58f,
                    width * 0.65f, height * 0.82f
                )
                cubicTo(
                    width * 0.40f, height * 0.92f,
                    width * 0.18f, height * 0.75f,
                    width * 0.18f, height * 0.52f
                )
                cubicTo(
                    width * 0.18f, height * 0.32f,
                    width * 0.35f, height * 0.18f,
                    width * 0.50f, height * 0.18f
                )
            }

            drawPath(
                path = outerLoopPath,
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF0F4A38),
                        NeoControlledCyan,
                        NeoMint,
                        NeoElectricLime,
                        Color(0xFF0F4A38)
                    ),
                    center = Offset(width * 0.5f, height * 0.5f)
                ),
                style = Stroke(
                    width = strokePx * 1.1f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                ),
                alpha = loopProgress.coerceIn(0f, 1f)
            )
        }

        // Stage 3: Top Unfolding Leaf
        if (stage >= 3) {
            val topLeafPath = Path().apply {
                moveTo(width * 0.52f, height * 0.18f)
                cubicTo(
                    width * 0.50f, height * 0.04f,
                    width * 0.68f, height * 0.01f,
                    width * 0.80f, height * 0.08f
                )
                cubicTo(
                    width * 0.80f, height * 0.20f,
                    width * 0.65f, height * 0.24f,
                    width * 0.52f, height * 0.18f
                )
                close()
            }

            drawPath(
                path = topLeafPath,
                brush = Brush.linearGradient(
                    colors = listOf(NeoMint, NeoElectricLime)
                ),
                alpha = leafProgress.coerceIn(0f, 1f)
            )

            // Leaf Vein Line
            val veinPath = Path().apply {
                moveTo(width * 0.52f, height * 0.18f)
                cubicTo(
                    width * 0.62f, height * 0.12f,
                    width * 0.72f, height * 0.08f,
                    width * 0.80f, height * 0.08f
                )
            }
            drawPath(
                path = veinPath,
                color = Color(0xFF071713).copy(alpha = 0.6f * leafProgress),
                style = Stroke(width = strokePx * 0.25f, cap = StrokeCap.Round)
            )
        }

        // Stage 4: Location Pin, City & Community Group Silhouettes
        if (stage >= 4) {
            val pAlpha = communityProgress.coerceIn(0f, 1f)

            // City Skyline Buildings Silhouette inside loop
            val skylinePath = Path().apply {
                moveTo(width * 0.30f, height * 0.70f)
                lineTo(width * 0.30f, height * 0.60f)
                lineTo(width * 0.36f, height * 0.60f)
                lineTo(width * 0.36f, height * 0.54f)
                lineTo(width * 0.44f, height * 0.54f)
                lineTo(width * 0.44f, height * 0.62f)
                lineTo(width * 0.52f, height * 0.62f)
                lineTo(width * 0.52f, height * 0.52f)
                lineTo(width * 0.60f, height * 0.52f)
                lineTo(width * 0.60f, height * 0.64f)
                lineTo(width * 0.68f, height * 0.64f)
                lineTo(width * 0.68f, height * 0.70f)
                close()
            }
            drawPath(
                path = skylinePath,
                color = NeoControlledCyan.copy(alpha = 0.35f * pAlpha)
            )

            // Community 3-People Group Silhouette inside loop
            // Center Person Head & Body
            drawCircle(
                color = NeoMint.copy(alpha = 0.9f * pAlpha),
                radius = width * 0.035f,
                center = Offset(width * 0.50f, height * 0.62f)
            )
            val centerBody = Path().apply {
                moveTo(width * 0.45f, height * 0.72f)
                cubicTo(
                    width * 0.45f, height * 0.66f,
                    width * 0.55f, height * 0.66f,
                    width * 0.55f, height * 0.72f
                )
                close()
            }
            drawPath(path = centerBody, color = NeoMint.copy(alpha = 0.9f * pAlpha))

            // Left Person
            drawCircle(
                color = NeoMint.copy(alpha = 0.75f * pAlpha),
                radius = width * 0.028f,
                center = Offset(width * 0.41f, height * 0.64f)
            )
            // Right Person
            drawCircle(
                color = NeoMint.copy(alpha = 0.75f * pAlpha),
                radius = width * 0.028f,
                center = Offset(width * 0.59f, height * 0.64f)
            )

            // Luminous Location Pin Anchor above community group
            val pinPath = Path().apply {
                moveTo(width * 0.50f, height * 0.48f)
                cubicTo(
                    width * 0.40f, height * 0.38f,
                    width * 0.38f, height * 0.28f,
                    width * 0.50f, height * 0.28f
                )
                cubicTo(
                    width * 0.62f, height * 0.28f,
                    width * 0.60f, height * 0.38f,
                    width * 0.50f, height * 0.48f
                )
                close()
            }
            drawPath(
                path = pinPath,
                color = NeoElectricLime.copy(alpha = pAlpha)
            )

            // Inner Pin Dot
            drawCircle(
                color = Color(0xFF071713),
                radius = width * 0.03f,
                center = Offset(width * 0.50f, height * 0.34f),
                alpha = pAlpha
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SaafLoopLogoPreview() {
    Box(modifier = Modifier.size(140.dp)) {
        SaafLoopLogo(stage = 5)
    }
}
