package com.example.saafloop.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
 * Signature SaafLoop Neo-Eco Infinity Loop logo composable.
 * Symbolizes continuous circularity, environmental action, and renewal.
 */
@Composable
fun SaafLoopLogo(
    modifier: Modifier = Modifier,
    progress: Float = 1f
) {
    Canvas(modifier = modifier.size(100.dp)) {
        val width = size.width
        val height = size.height

        val strokePx = width * 0.07f

        // 1. Outer Location Pin Shield (Mint #7CEBC0)
        val pinPath = Path().apply {
            moveTo(width * 0.5f, height * 0.82f)
            cubicTo(
                width * 0.22f, height * 0.64f,
                width * 0.18f, height * 0.52f,
                width * 0.18f, height * 0.38f
            )
            cubicTo(
                width * 0.18f, height * 0.22f,
                width * 0.32f, height * 0.12f,
                width * 0.5f, height * 0.12f
            )
            cubicTo(
                width * 0.68f, height * 0.12f,
                width * 0.82f, height * 0.22f,
                width * 0.82f, height * 0.38f
            )
            cubicTo(
                width * 0.82f, height * 0.52f,
                width * 0.78f, height * 0.64f,
                width * 0.5f, height * 0.82f
            )
            close()
        }

        drawPath(
            path = pinPath,
            color = NeoMint.copy(alpha = 0.85f * progress.coerceIn(0f, 1f)),
            style = Stroke(
                width = strokePx * 0.6f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 2. Continuous Infinity Loop Ribbon (Electric Lime #C7FF4D & Mint #7CEBC0)
        val leftRibbon = Path().apply {
            moveTo(width * 0.32f, height * 0.48f)
            cubicTo(
                width * 0.32f, height * 0.38f,
                width * 0.42f, height * 0.32f,
                width * 0.5f, height * 0.42f
            )
            cubicTo(
                width * 0.58f, height * 0.52f,
                width * 0.68f, height * 0.58f,
                width * 0.68f, height * 0.48f
            )
        }

        val rightRibbon = Path().apply {
            moveTo(width * 0.68f, height * 0.48f)
            cubicTo(
                width * 0.68f, height * 0.38f,
                width * 0.58f, height * 0.32f,
                width * 0.5f, height * 0.42f
            )
            cubicTo(
                width * 0.42f, height * 0.52f,
                width * 0.32f, height * 0.58f,
                width * 0.32f, height * 0.48f
            )
        }

        drawPath(
            path = leftRibbon,
            color = NeoMint,
            style = Stroke(
                width = strokePx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        drawPath(
            path = rightRibbon,
            color = NeoElectricLime,
            style = Stroke(
                width = strokePx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 3. Central Anchor Spark (Electric Lime #C7FF4D)
        drawCircle(
            color = NeoElectricLime,
            radius = strokePx * 0.55f,
            center = Offset(width * 0.5f, height * 0.42f)
        )

        // 4. Top Renewal Leaf Accent (Controlled Cyan #57DDE0)
        val leafPath = Path().apply {
            moveTo(width * 0.5f, height * 0.12f)
            cubicTo(
                width * 0.54f, height * 0.08f,
                width * 0.60f, height * 0.06f,
                width * 0.62f, height * 0.08f
            )
            cubicTo(
                width * 0.62f, height * 0.10f,
                width * 0.58f, height * 0.14f,
                width * 0.5f, height * 0.12f
            )
            close()
        }

        drawPath(
            path = leafPath,
            color = NeoControlledCyan
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SaafLoopLogoPreview() {
    Box(modifier = Modifier.size(120.dp)) {
        SaafLoopLogo()
    }
}
