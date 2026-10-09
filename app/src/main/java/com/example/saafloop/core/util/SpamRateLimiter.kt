package com.example.saafloop.core.util

import java.util.concurrent.ConcurrentHashMap

/**
 * Rate limiter preventing spam report creation or rapid API flooding.
 */
object SpamRateLimiter {

    private const val MAX_REPORTS_PER_WINDOW = 3
    private const val WINDOW_MILLIS = 5 * 60 * 1000L // 5 minutes

    private val userTimestampsMap = ConcurrentHashMap<String, MutableList<Long>>()

    /**
     * Checks if a user is allowed to submit a new report or action.
     * Returns true if allowed, false if rate limited.
     */
    fun isActionAllowed(userId: String): Boolean {
        if (userId.isBlank() || userId == "GUEST") return true

        val now = System.currentTimeMillis()
        val timestamps = userTimestampsMap.getOrPut(userId) { mutableListOf() }

        synchronized(timestamps) {
            // Remove timestamps older than WINDOW_MILLIS
            timestamps.removeAll { now - it > WINDOW_MILLIS }

            return if (timestamps.size >= MAX_REPORTS_PER_WINDOW) {
                false
            } else {
                timestamps.add(now)
                true
            }
        }
    }

    /**
     * Clears rate limit tracking for testing or reset.
     */
    fun resetForUser(userId: String) {
        userTimestampsMap.remove(userId)
    }
}
