package com.example.saafloop.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.saafloop.ui.theme.NeoControlledCyan
import com.example.saafloop.ui.theme.NeoElectricLime
import com.example.saafloop.ui.theme.NeoMint

/**
 * Signature SaafLoop Logo Component:
 * Ultra-clean, mathematically perfect Neo-Eco Circle Loop with a top leaf,
 * luminous location pin anchor, and community group emblem.
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
        val center = Offset(w * 0.5f, h * 0.52f)
        val radius = w * 0.32f
        val strokeWidth = w * 0.075f

        // 1. Perfectly Smooth Outer Eco Loop Circle
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(
                    NeoControlledCyan,
                    NeoMint,
                    NeoElectricLime,
                    NeoControlledCyan
                ),
                center = center
            ),
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            alpha = alphaVal
        )

        // 2. Top Unfolding Green Leaf
        val leafPath = Path().apply {
            moveTo(w * 0.50f, h * 0.20f)
            cubicTo(
                w * 0.48f, h * 0.04f,
                w * 0.72f, h * 0.02f,
                w * 0.82f, h * 0.12f
            )
            cubicTo(
                w * 0.82f, h * 0.24f,
                w * 0.66f, h * 0.28f,
                w * 0.50f, h * 0.20f
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

        // Leaf Vein Line
        val veinPath = Path().apply {
            moveTo(w * 0.50f, h * 0.20f)
            cubicTo(
                w * 0.64f, h * 0.14f,
                w * 0.74f, h * 0.11f,
                w * 0.82f, h * 0.12f
            )
        }
        drawPath(
            path = veinPath,
            color = Color(0xFF071713).copy(alpha = 0.5f * alphaVal),
            style = Stroke(width = strokeWidth * 0.25f, cap = StrokeCap.Round)
        )

        // 3. Inner Luminous Location Pin Anchor (Upper Center)
        val pinWidth = w * 0.20f
        val pinHeight = h * 0.26f
        val pinCenterY = h * 0.44f

        val pinPath = Path().apply {
            moveTo(w * 0.50f, pinCenterY + pinHeight / 2f)
            cubicTo(
                w * 0.50f - pinWidth / 2f, pinCenterY + pinHeight * 0.1f,
                w * 0.50f - pinWidth / 2f, pinCenterY - pinHeight / 2f,
                w * 0.50f, pinCenterY - pinHeight / 2f
            )
            cubicTo(
                w * 0.50f + pinWidth / 2f, pinCenterY - pinHeight / 2f,
                w * 0.50f + pinWidth / 2f, pinCenterY + pinHeight * 0.1f,
                w * 0.50f, pinCenterY + pinHeight / 2f
            )
            close()
        }

        drawPath(
            path = pinPath,
            color = NeoElectricLime.copy(alpha = alphaVal)
        )

        // Inner Pin Dot
        drawCircle(
            color = Color(0xFF071713),
            radius = w * 0.038f,
            center = Offset(w * 0.50f, pinCenterY - pinHeight * 0.1f),
            alpha = alphaVal
        )

        // 4. Community 3-People Group Silhouette (Lower Center)
        // Center Person Head & Shoulder
        drawCircle(
            color = NeoMint.copy(alpha = 0.95f * alphaVal),
            radius = w * 0.032f,
            center = Offset(w * 0.50f, h * 0.65f)
        )
        val centerBody = Path().apply {
            moveTo(w * 0.44f, h * 0.74f)
            cubicTo(
                w * 0.44f, h * 0.69f,
                w * 0.56f, h * 0.69f,
                w * 0.56f, h * 0.74f
            )
            close()
        }
        drawPath(path = centerBody, color = NeoMint.copy(alpha = 0.95f * alphaVal))

        // Left Person
        drawCircle(
            color = NeoMint.copy(alpha = 0.75f * alphaVal),
            radius = w * 0.026f,
            center = Offset(w * 0.41f, h * 0.67f)
        )
        val leftBody = Path().apply {
            moveTo(w * 0.36f, h * 0.74f)
            cubicTo(
                w * 0.36f, h * 0.70f,
                w * 0.46f, h * 0.70f,
                w * 0.46f, h * 0.74f
            )
            close()
        }
        drawPath(path = leftBody, color = NeoMint.copy(alpha = 0.75f * alphaVal))

        // Right Person
        drawCircle(
            color = NeoMint.copy(alpha = 0.75f * alphaVal),
            radius = w * 0.026f,
            center = Offset(w * 0.59f, h * 0.67f)
        )
        val rightBody = Path().apply {
            moveTo(w * 0.54f, h * 0.74f)
            cubicTo(
                w * 0.54f, h * 0.70f,
                w * 0.64f, h * 0.70f,
                w * 0.64f, h * 0.74f
            )
            close()
        }
        drawPath(path = rightBody, color = NeoMint.copy(alpha = 0.75f * alphaVal))
    }
}

@Preview(showBackground = true)
@Composable
fun SaafLoopLogoPreview() {
    Box(modifier = Modifier.size(140.dp)) {
        SaafLoopLogo()
    }
}
