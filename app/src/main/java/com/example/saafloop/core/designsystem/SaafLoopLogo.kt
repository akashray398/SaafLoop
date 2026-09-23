package com.example.saafloop.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SaafLoopLogo(
    modifier: Modifier = Modifier,
    strokeWidth: Float = 8f
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tealColor = MaterialTheme.colorScheme.secondary

    Canvas(modifier = modifier.size(100.dp)) {
        val width = size.width
        val height = size.height
        
        // 1. Draw the Location Pin outer shape
        val pinPath = Path().apply {
            moveTo(width / 2, height * 0.9f)
            // Left curve up to the top circle
            cubicTo(
                width * 0.15f, height * 0.6f,
                width * 0.15f, height * 0.25f,
                width / 2, height * 0.1f
            )
            // Right curve down to the tip
            cubicTo(
                width * 0.85f, height * 0.25f,
                width * 0.85f, height * 0.6f,
                width / 2, height * 0.9f
            )
            close()
        }
        
        drawPath(
            path = pinPath,
            color = primaryColor,
            style = Stroke(width = strokeWidth * 1.5f)
        )

        // 2. Draw the inner continuous cleanup loop (a dynamic recycling style loop inside the pin head)
        val centerX = width / 2
        val centerY = height * 0.42f
        val radius = width * 0.18f

        // Draw a clean continuous circular loop using fresh teal
        drawArc(
            color = tealColor,
            startAngle = 45f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(centerX - radius, centerY - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // Draw the inner dot/loop connection to express the "loop" clearly
        drawCircle(
            color = tealColor,
            radius = strokeWidth * 1.2f,
            center = Offset(centerX, centerY),
            style = Stroke(width = strokeWidth / 2f)
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
