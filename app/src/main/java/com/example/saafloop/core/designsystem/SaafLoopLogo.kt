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
 * Signature SaafLoop Logo Component:
 * S-Loop + Unfolding Leaf + Luminous Location Pin Anchor.
 * Fuses circular sustainability, map spot reporting, and environmental renewal.
 */
@Composable
fun SaafLoopLogo(
    modifier: Modifier = Modifier,
    progress: Float = 1f
) {
    val alphaVal = progress.coerceIn(0f, 1f)

    Canvas(modifier = modifier.size(120.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.08f

        // 1. Flowing SuperScript "S" Eco Loop Ribbon (Mint -> Lime Gradient)
        val sLoopPath = Path().apply {
            moveTo(w * 0.62f, h * 0.28f)
            cubicTo(
                w * 0.35f, h * 0.22f,
                w * 0.22f, h * 0.42f,
                w * 0.50f, h * 0.52f
            )
            cubicTo(
                w * 0.78f, h * 0.62f,
                w * 0.65f, h * 0.82f,
                w * 0.38f, h * 0.78f
            )
            cubicTo(
                w * 0.18f, h * 0.74f,
                w * 0.18f, h * 0.54f,
                w * 0.35f, h * 0.48f
            )
        }

        drawPath(
            path = sLoopPath,
            brush = Brush.linearGradient(
                colors = listOf(
                    NeoControlledCyan,
                    NeoMint,
                    NeoElectricLime
                ),
                start = Offset(w * 0.2f, h * 0.2f),
                end = Offset(w * 0.8f, h * 0.8f)
            ),
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            ),
            alpha = alphaVal
        )

        // 2. Top Unfolding Leaf at the head of the S-Loop
        val leafPath = Path().apply {
            moveTo(w * 0.62f, h * 0.28f)
            cubicTo(
                w * 0.58f, h * 0.08f,
                w * 0.78f, h * 0.04f,
                w * 0.88f, h * 0.16f
            )
            cubicTo(
                w * 0.88f, h * 0.28f,
                w * 0.74f, h * 0.32f,
                w * 0.62f, h * 0.28f
            )
            close()
        }

        drawPath(
            path = leafPath,
            brush = Brush.horizontalGradient(
                colors = listOf(NeoMint, NeoElectricLime)
            ),
            alpha = alphaVal
        )

        // Leaf Vein Accent
        val veinPath = Path().apply {
            moveTo(w * 0.62f, h * 0.28f)
            cubicTo(
                w * 0.72f, h * 0.20f,
                w * 0.80f, h * 0.17f,
                w * 0.88f, h * 0.16f
            )
        }
        drawPath(
            path = veinPath,
            color = Color(0xFF071713).copy(alpha = 0.5f * alphaVal),
            style = Stroke(width = strokeWidth * 0.22f, cap = StrokeCap.Round)
        )

        // 3. Luminous Location Pin Anchor inside upper loop head
        val pinWidth = w * 0.18f
        val pinHeight = h * 0.24f
        val pinCenterY = h * 0.38f

        val pinPath = Path().apply {
            moveTo(w * 0.48f, pinCenterY + pinHeight / 2f)
            cubicTo(
                w * 0.48f - pinWidth / 2f, pinCenterY + pinHeight * 0.1f,
                w * 0.48f - pinWidth / 2f, pinCenterY - pinHeight / 2f,
                w * 0.48f, pinCenterY - pinHeight / 2f
            )
            cubicTo(
                w * 0.48f + pinWidth / 2f, pinCenterY - pinHeight / 2f,
                w * 0.48f + pinWidth / 2f, pinCenterY + pinHeight * 0.1f,
                w * 0.48f, pinCenterY + pinHeight / 2f
            )
            close()
        }

        drawPath(
            path = pinPath,
            color = NeoElectricLime.copy(alpha = alphaVal)
        )

        // Pin Inner Anchor Dot
        drawCircle(
            color = Color(0xFF071713),
            radius = w * 0.03f,
            center = Offset(w * 0.48f, pinCenterY - pinHeight * 0.1f),
            alpha = alphaVal
        )

        // 4. Community 3-People Group Silhouette in lower loop
        drawCircle(
            color = NeoMint.copy(alpha = 0.95f * alphaVal),
            radius = w * 0.032f,
            center = Offset(w * 0.52f, h * 0.68f)
        )
        val centerBody = Path().apply {
            moveTo(w * 0.46f, h * 0.77f)
            cubicTo(
                w * 0.46f, h * 0.72f,
                w * 0.58f, h * 0.72f,
                w * 0.58f, h * 0.77f
            )
            close()
        }
        drawPath(path = centerBody, color = NeoMint.copy(alpha = 0.95f * alphaVal))

        // Left Companion
        drawCircle(
            color = NeoMint.copy(alpha = 0.75f * alphaVal),
            radius = w * 0.026f,
            center = Offset(w * 0.43f, h * 0.70f)
        )

        // Right Companion
        drawCircle(
            color = NeoMint.copy(alpha = 0.75f * alphaVal),
            radius = w * 0.026f,
            center = Offset(w * 0.61f, h * 0.70f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SaafLoopLogoPreview() {
    Box(modifier = Modifier.size(140.dp)) {
        SaafLoopLogo()
    }
}
