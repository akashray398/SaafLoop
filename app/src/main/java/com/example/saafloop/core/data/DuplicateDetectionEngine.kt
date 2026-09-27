package com.example.saafloop.core.data

import com.example.saafloop.core.model.CaseReport
import com.example.saafloop.core.model.CaseStatus
import com.example.saafloop.core.model.DuplicateMatchItem
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object DuplicateDetectionEngine {

    /**
     * Calculates geodesic distance in meters between two lat/lng coordinates.
     */
    fun distanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusMeters = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusMeters * c
    }

    /**
     * Evaluates public cases to find potential nearby duplicates within [maxRadiusMeters].
     *
     * Rules:
     * - Constrained query radius (default 500m).
     * - Explanatory match reasons ("Active report 120m away in same category").
     * - Verified clean sites flagged as potential repeat dumping spots without forced auto-merges.
     */
    fun findNearbyMatches(
        targetLat: Double,
        targetLng: Double,
        targetCategory: String,
        candidateCases: List<CaseReport>,
        maxRadiusMeters: Double = 500.0
    ): List<DuplicateMatchItem> {
        if (targetLat == 0.0 && targetLng == 0.0) return emptyList()

        return candidateCases.mapNotNull { caseReport ->
            val dist = distanceInMeters(targetLat, targetLng, caseReport.latitude, caseReport.longitude)
            if (dist > maxRadiusMeters) return@mapNotNull null

            val isCategoryMatch = caseReport.category.equals(targetCategory, ignoreCase = true)
            val distInt = dist.toInt()

            val (reason, confidence) = when {
                caseReport.status == CaseStatus.VERIFIED_CLEAN -> {
                    Pair("Previously clean site ${distInt}m away (Possible repeat dumping)", 0.65f)
                }
                isCategoryMatch -> {
                    Pair("Active report ${distInt}m away in same category", 0.90f)
                }
                else -> {
                    Pair("Nearby active report ${distInt}m away", 0.70f)
                }
            }

            DuplicateMatchItem(
                caseReport = caseReport,
                distanceMeters = distInt,
                matchReason = reason,
                confidenceScore = confidence
            )
        }.sortedBy { it.distanceMeters }
    }
}
