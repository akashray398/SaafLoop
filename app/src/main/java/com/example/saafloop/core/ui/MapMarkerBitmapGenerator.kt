package com.example.saafloop.core.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.example.saafloop.core.model.CasePriority
import com.example.saafloop.core.model.CivicMapMarker
import com.example.saafloop.core.model.CivicMapType
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory

/**
 * Creates custom vector/bitmap map pin icons for MapLibre based on civic entity type,
 * status, priority, and cluster count.
 */
object MapMarkerBitmapGenerator {

    fun createMapIcon(context: Context, marker: CivicMapMarker): Icon {
        val density = context.resources.displayMetrics.density
        val sizePx = (36 * density).toInt()
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 12 * density
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        when (marker.type) {
            CivicMapType.CLUSTER -> {
                paint.color = Color.parseColor("#1B5E20") // Deep Green
                canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - 2f, paint)

                paint.color = Color.parseColor("#81C784")
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f * density
                canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - 3f, paint)

                val countText = if (marker.clusterCount > 99) "99+" else marker.clusterCount.toString()
                val textY = sizePx / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
                canvas.drawText(countText, sizePx / 2f, textY, textPaint)
            }

            CivicMapType.REPORT -> {
                // Pin color based on status or priority
                val pinColor = when {
                    marker.priority == CasePriority.CRITICAL || marker.priority == CasePriority.HIGH -> Color.parseColor("#D32F2F") // Red
                    marker.statusLabel.contains("Being Addressed", ignoreCase = true) -> Color.parseColor("#0288D1") // Blue
                    marker.statusLabel.contains("Verified", ignoreCase = true) -> Color.parseColor("#2E7D32") // Green
                    else -> Color.parseColor("#E65100") // Amber/Orange
                }

                drawPinWithSymbol(canvas, sizePx, density, paint, textPaint, pinColor, "●")
            }

            CivicMapType.FIELD_TASK -> {
                // Diamond shape for active field task
                val pinColor = Color.parseColor("#7B1FA2") // Purple/Indigo
                paint.color = pinColor
                paint.style = Paint.Style.FILL

                val path = Path().apply {
                    moveTo(sizePx / 2f, 2f * density)
                    lineTo(sizePx - 2f * density, sizePx / 2f)
                    lineTo(sizePx / 2f, sizePx - 2f * density)
                    lineTo(2f * density, sizePx / 2f)
                    close()
                }
                canvas.drawPath(path, paint)

                paint.color = Color.WHITE
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f * density
                canvas.drawPath(path, paint)

                val textY = sizePx / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
                canvas.drawText("◆", sizePx / 2f, textY, textPaint)
            }

            CivicMapType.COMMUNITY_ACTIVITY -> {
                // Star / Activity pin
                val pinColor = Color.parseColor("#00796B") // Teal/Green
                drawPinWithSymbol(canvas, sizePx, density, paint, textPaint, pinColor, "★")
            }

            CivicMapType.RESOLVED_ISSUE -> {
                // Checkmark / Resolved area pin
                val pinColor = Color.parseColor("#2E7D32") // Emerald Green
                drawPinWithSymbol(canvas, sizePx, density, paint, textPaint, pinColor, "✓")
            }
        }

        return IconFactory.getInstance(context).fromBitmap(bitmap)
    }

    private fun drawPinWithSymbol(
        canvas: Canvas,
        sizePx: Int,
        density: Float,
        paint: Paint,
        textPaint: Paint,
        color: Int,
        symbol: String
    ) {
        // Base pin circle
        paint.color = color
        paint.style = Paint.Style.FILL
        canvas.drawCircle(sizePx / 2f, sizePx / 2f - 2f * density, sizePx / 2f - 4f * density, paint)

        // White border
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f * density
        canvas.drawCircle(sizePx / 2f, sizePx / 2f - 2f * density, sizePx / 2f - 4f * density, paint)

        // Symbol text
        textPaint.color = Color.WHITE
        val textY = (sizePx / 2f - 2f * density) - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(symbol, sizePx / 2f, textY, textPaint)
    }
}
