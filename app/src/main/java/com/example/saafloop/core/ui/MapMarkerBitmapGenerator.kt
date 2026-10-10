package com.example.saafloop.core.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.example.saafloop.core.model.CasePriority
import com.example.saafloop.core.model.CivicMapMarker
import com.example.saafloop.core.model.CivicMapType
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import java.util.Collections

/**
 * Creates custom vector/bitmap map pin icons for MapLibre with LRU cache
 * to prevent unnecessary bitmap re-allocations and GC pressure during map panning.
 */
object MapMarkerBitmapGenerator {

    private const val MAX_CACHE_SIZE = 120
    private val iconCache: MutableMap<String, Icon> = Collections.synchronizedMap(
        object : LinkedHashMap<String, Icon>(MAX_CACHE_SIZE, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Icon>?): Boolean {
                return size > MAX_CACHE_SIZE
            }
        }
    )

    fun createMapIcon(context: Context, marker: CivicMapMarker): Icon {
        val cacheKey = "${marker.type}_${marker.priority}_${marker.statusLabel}_${marker.clusterCount}"
        val cachedIcon = iconCache[cacheKey]
        if (cachedIcon != null) return cachedIcon

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
                val pinColor = when {
                    marker.priority == CasePriority.CRITICAL || marker.priority == CasePriority.HIGH -> Color.parseColor("#D32F2F")
                    marker.statusLabel.contains("Being Addressed", ignoreCase = true) -> Color.parseColor("#0288D1")
                    marker.statusLabel.contains("Verified", ignoreCase = true) -> Color.parseColor("#2E7D32")
                    else -> Color.parseColor("#E65100")
                }

                drawPinWithSymbol(canvas, sizePx, density, paint, textPaint, pinColor, "●")
            }

            CivicMapType.FIELD_TASK -> {
                val pinColor = Color.parseColor("#7B1FA2")
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
                val pinColor = Color.parseColor("#00796B")
                drawPinWithSymbol(canvas, sizePx, density, paint, textPaint, pinColor, "★")
            }

            CivicMapType.RESOLVED_ISSUE -> {
                val pinColor = Color.parseColor("#2E7D32")
                drawPinWithSymbol(canvas, sizePx, density, paint, textPaint, pinColor, "✓")
            }
        }

        val generatedIcon = IconFactory.getInstance(context).fromBitmap(bitmap)
        iconCache[cacheKey] = generatedIcon
        return generatedIcon
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
        paint.color = color
        paint.style = Paint.Style.FILL
        canvas.drawCircle(sizePx / 2f, sizePx / 2f - 2f * density, sizePx / 2f - 4f * density, paint)

        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f * density
        canvas.drawCircle(sizePx / 2f, sizePx / 2f - 2f * density, sizePx / 2f - 4f * density, paint)

        textPaint.color = Color.WHITE
        val textY = (sizePx / 2f - 2f * density) - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(symbol, sizePx / 2f, textY, textPaint)
    }

    fun clearCache() {
        iconCache.clear()
    }
}
