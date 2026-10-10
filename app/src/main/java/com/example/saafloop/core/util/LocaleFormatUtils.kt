package com.example.saafloop.core.util

import android.content.Context
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Locale-aware formatting utilities for dates, numbers, distances, and percentages.
 */
object LocaleFormatUtils {

    fun formatDate(timestamp: Long, locale: Locale = Locale.getDefault()): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", locale)
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long, locale: Locale = Locale.getDefault()): String {
        val sdf = SimpleDateFormat("h:mm a", locale)
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long, locale: Locale = Locale.getDefault()): String {
        val sdf = SimpleDateFormat("MMM d, h:mm a", locale)
        return sdf.format(Date(timestamp))
    }

    fun formatNumber(number: Number, locale: Locale = Locale.getDefault()): String {
        return NumberFormat.getInstance(locale).format(number)
    }

    fun formatDistanceMeters(distanceMeters: Float, context: Context, locale: Locale = Locale.getDefault()): String {
        return if (distanceMeters < 1000f) {
            "${formatNumber(distanceMeters.toInt(), locale)} m"
        } else {
            val km = distanceMeters / 1000f
            String.format(locale, "%.1f km", km)
        }
    }

    fun formatPercentage(percentageFloat: Float, locale: Locale = Locale.getDefault()): String {
        val percent = (percentageFloat * 100).toInt()
        return "${formatNumber(percent, locale)}%"
    }
}
