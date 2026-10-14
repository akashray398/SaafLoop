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
 * Recommended Master Logo: The "Loop + Leaf + Pin Anchor" Emblem.
 * Iconically combines the Continuous S-Loop Ribbon, Unfolding Green Leaf,
 * and Luminous Location Pin Anchor for instant recognition on any Android display.
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

        // 1. 3D Drop Shadow Layer for Depth
        val loopPath = Path().apply {
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
            path = loopPath,
            color = Color.Black.copy(alpha = 0.4f * loopProgress),
            style = Stroke(
                width = strokePx * 1.3f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 2. Flowing SuperScript "S" Eco Loop Ribbon (Mint #7CEBC0 & Electric Lime #C7FF4D)
        drawPath(
            path = loopPath,
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
                width = strokePx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            ),
            alpha = loopProgress.coerceIn(0f, 1f)
        )

        // Specular Top Edge Highlight
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

        // 3. Top Unfolding Green Leaf Accent
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
            alpha = leafProgress.coerceIn(0f, 1f)
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
            color = Color(0xFF071713).copy(alpha = 0.5f * leafProgress),
            style = Stroke(width = strokePx * 0.22f, cap = StrokeCap.Round)
        )

        // 4. Luminous Location Pin Anchor inside upper loop head
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

        // Pin Shadow
        drawPath(
            path = pinPath,
            color = Color.Black.copy(alpha = 0.35f * communityProgress),
            style = Stroke(width = strokePx * 0.3f)
        )

        // Pin Core Fill
        drawPath(
            path = pinPath,
            brush = Brush.verticalGradient(
                colors = listOf(NeoElectricLime, NeoMint)
            ),
            alpha = communityProgress.coerceIn(0f, 1f)
        )

        // Pin Inner Anchor Dot
        drawCircle(
            color = Color(0xFF071713),
            radius = w * 0.03f,
            center = Offset(w * 0.48f, pinCenterY - pinHeight * 0.1f),
            alpha = communityProgress.coerceIn(0f, 1f)
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
