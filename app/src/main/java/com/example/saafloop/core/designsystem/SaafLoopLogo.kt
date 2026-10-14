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
 * Premium 3D-Effect SaafLoop Master Logo Emblem:
 * Features multi-layer depth, 3D ribbon shading, specular highlights,
 * luminous location pin anchor, city skyline, and community group silhouettes.
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
    Canvas(modifier = modifier.size(130.dp)) {
        val w = size.width
        val h = size.height
        val strokePx = w * 0.085f

        // Stage 1: Initial Journey Light Path & Spark
        if (stage >= 1) {
            val sparkPath = Path().apply {
                moveTo(w * 0.22f, h * 0.78f)
                cubicTo(
                    w * 0.12f, h * 0.50f,
                    w * 0.28f, h * 0.20f,
                    w * 0.58f, h * 0.18f
                )
            }
            drawPath(
                path = sparkPath,
                brush = Brush.linearGradient(
                    colors = listOf(NeoControlledCyan, NeoElectricLime)
                ),
                style = Stroke(
                    width = strokePx * 0.6f,
                    cap = StrokeCap.Round
                ),
                alpha = sparkProgress.coerceIn(0f, 1f)
            )

            // Luminous Head Spark
            drawCircle(
                color = NeoElectricLime,
                radius = strokePx * 0.75f,
                center = Offset(w * 0.58f, h * 0.18f),
                alpha = sparkProgress.coerceIn(0f, 1f)
            )
        }

        // Stage 2: 3D Ribbon Loop Formation with Depth Shadow & Highlights
        if (stage >= 2) {
            val loopPath = Path().apply {
                moveTo(w * 0.50f, h * 0.18f)
                cubicTo(
                    w * 0.82f, h * 0.20f,
                    w * 0.88f, h * 0.58f,
                    w * 0.65f, h * 0.82f
                )
                cubicTo(
                    w * 0.40f, h * 0.92f,
                    w * 0.18f, h * 0.75f,
                    w * 0.18f, h * 0.52f
                )
                cubicTo(
                    w * 0.18f, h * 0.32f,
                    w * 0.35f, h * 0.18f,
                    w * 0.50f, h * 0.18f
                )
            }

            // 3D Ambient Drop Shadow Layer
            drawPath(
                path = loopPath,
                color = Color.Black.copy(alpha = 0.4f * loopProgress),
                style = Stroke(
                    width = strokePx * 1.3f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 3D Multi-Color Gradient Core Ribbon
            drawPath(
                path = loopPath,
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF0A3326),
                        NeoControlledCyan,
                        NeoMint,
                        NeoElectricLime,
                        Color(0xFF0A3326)
                    ),
                    center = Offset(w * 0.5f, h * 0.5f)
                ),
                style = Stroke(
                    width = strokePx * 1.1f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                ),
                alpha = loopProgress.coerceIn(0f, 1f)
            )

            // 3D Specular Top Edge Highlight
            drawPath(
                path = loopPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.7f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.5f)
                    )
                ),
                style = Stroke(
                    width = strokePx * 0.25f,
                    cap = StrokeCap.Round
                ),
                alpha = loopProgress.coerceIn(0f, 1f)
            )
        }

        // Stage 3: Top Unfolding 3D Leaf Accent
        if (stage >= 3) {
            val topLeafPath = Path().apply {
                moveTo(w * 0.52f, h * 0.18f)
                cubicTo(
                    w * 0.50f, h * 0.03f,
                    w * 0.70f, h * 0.00f,
                    w * 0.82f, h * 0.08f
                )
                cubicTo(
                    w * 0.82f, h * 0.22f,
                    w * 0.66f, h * 0.26f,
                    w * 0.52f, h * 0.18f
                )
                close()
            }

            // 3D Leaf Body Gradient
            drawPath(
                path = topLeafPath,
                brush = Brush.verticalGradient(
                    colors = listOf(NeoElectricLime, NeoMint)
                ),
                alpha = leafProgress.coerceIn(0f, 1f)
            )

            // Leaf Vein Line
            val veinPath = Path().apply {
                moveTo(w * 0.52f, h * 0.18f)
                cubicTo(
                    w * 0.64f, h * 0.12f,
                    w * 0.74f, h * 0.08f,
                    w * 0.82f, h * 0.08f
                )
            }
            drawPath(
                path = veinPath,
                color = Color(0xFF071713).copy(alpha = 0.6f * leafProgress),
                style = Stroke(width = strokePx * 0.25f, cap = StrokeCap.Round)
            )
        }

        // Stage 4: City Skyline, Community Group & Luminous Pin
        if (stage >= 4) {
            val pAlpha = communityProgress.coerceIn(0f, 1f)

            // City Skyline Silhouette
            val skylinePath = Path().apply {
                moveTo(w * 0.30f, h * 0.70f)
                lineTo(w * 0.30f, h * 0.60f)
                lineTo(w * 0.36f, h * 0.60f)
                lineTo(w * 0.36f, h * 0.54f)
                lineTo(w * 0.44f, h * 0.54f)
                lineTo(w * 0.44f, h * 0.62f)
                lineTo(w * 0.52f, h * 0.62f)
                lineTo(w * 0.52f, h * 0.52f)
                lineTo(w * 0.60f, h * 0.52f)
                lineTo(w * 0.60f, h * 0.64f)
                lineTo(w * 0.68f, h * 0.64f)
                lineTo(w * 0.68f, h * 0.70f)
                close()
            }
            drawPath(
                path = skylinePath,
                color = NeoControlledCyan.copy(alpha = 0.4f * pAlpha)
            )

            // Community 3-People Group Silhouette
            // Center Person
            drawCircle(
                color = NeoMint.copy(alpha = 0.95f * pAlpha),
                radius = w * 0.035f,
                center = Offset(w * 0.50f, h * 0.62f)
            )
            val centerBody = Path().apply {
                moveTo(w * 0.45f, h * 0.71f)
                cubicTo(
                    w * 0.45f, h * 0.66f,
                    w * 0.55f, h * 0.66f,
                    w * 0.55f, h * 0.71f
                )
                close()
            }
            drawPath(path = centerBody, color = NeoMint.copy(alpha = 0.95f * pAlpha))

            // Left & Right Companions
            drawCircle(
                color = NeoMint.copy(alpha = 0.8f * pAlpha),
                radius = w * 0.028f,
                center = Offset(w * 0.41f, h * 0.64f)
            )
            drawCircle(
                color = NeoMint.copy(alpha = 0.8f * pAlpha),
                radius = w * 0.028f,
                center = Offset(w * 0.59f, h * 0.64f)
            )

            // 3D Luminous Location Pin Anchor
            val pinPath = Path().apply {
                moveTo(w * 0.50f, h * 0.48f)
                cubicTo(
                    w * 0.40f, h * 0.38f,
                    w * 0.38f, h * 0.28f,
                    w * 0.50f, h * 0.28f
                )
                cubicTo(
                    w * 0.62f, h * 0.28f,
                    w * 0.60f, h * 0.38f,
                    w * 0.50f, h * 0.48f
                )
                close()
            }

            // Pin Shadow
            drawPath(
                path = pinPath,
                color = Color.Black.copy(alpha = 0.35f * pAlpha),
                style = Stroke(width = strokePx * 0.3f)
            )

            // Pin Core Fill
            drawPath(
                path = pinPath,
                brush = Brush.verticalGradient(
                    colors = listOf(NeoElectricLime, NeoMint)
                ),
                alpha = pAlpha
            )

            // Inner Pin Anchor Dot
            drawCircle(
                color = Color(0xFF071713),
                radius = w * 0.032f,
                center = Offset(w * 0.50f, h * 0.34f),
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
