package com.example.saafloop.core.util

import com.example.saafloop.core.model.CivicMapMarker
import com.example.saafloop.core.model.CivicMapType
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Geospatial utilities for distance calculation, clustering, geofencing,
 * and location accuracy formatting.
 */
object GeoUtils {

    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Calculates distance in meters between two lat/lng coordinates using the Haversine formula.
     */
    fun calculateDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        if (lat1 == 0.0 && lon1 == 0.0 || lat2 == 0.0 && lon2 == 0.0) return 0f

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (EARTH_RADIUS_METERS * c).toFloat()
    }

    /**
     * Formats distance in meters to user-friendly string (e.g. "320m away" or "1.4 km away").
     */
    fun formatDistance(meters: Float?): String {
        if (meters == null || meters <= 0f) return "Nearby"
        return if (meters < 1000f) {
            "${meters.toInt()}m away"
        } else {
            val km = meters / 1000f
            String.format("%.1f km away", km)
        }
    }

    /**
     * Clusters nearby markers that fall within [clusterRadiusMeters].
     * Returns a list containing individual markers and cluster markers.
     */
    fun clusterMarkers(
        markers: List<CivicMapMarker>,
        clusterRadiusMeters: Double = 150.0
    ): List<CivicMapMarker> {
        if (markers.size <= 1) return markers

        val clustered = mutableListOf<CivicMapMarker>()
        val visited = BooleanArray(markers.size)

        for (i in markers.indices) {
            if (visited[i]) continue

            val clusterGroup = mutableListOf<CivicMapMarker>()
            clusterGroup.add(markers[i])
            visited[i] = true

            for (j in i + 1 until markers.size) {
                if (visited[j]) continue
                val dist = calculateDistanceMeters(
                    markers[i].latitude, markers[i].longitude,
                    markers[j].latitude, markers[j].longitude
                )
                if (dist <= clusterRadiusMeters) {
                    clusterGroup.add(markers[j])
                    visited[j] = true
                }
            }

            if (clusterGroup.size > 1) {
                val avgLat = clusterGroup.map { it.latitude }.average()
                val avgLng = clusterGroup.map { it.longitude }.average()
                val primary = clusterGroup.first()

                clustered.add(
                    CivicMapMarker(
                        id = "cluster_${primary.id}_${clusterGroup.size}",
                        type = CivicMapType.CLUSTER,
                        latitude = avgLat,
                        longitude = avgLng,
                        title = "${clusterGroup.size} Civic Items",
                        category = "Multiple Issues",
                        areaName = primary.areaName,
                        statusLabel = "${clusterGroup.size} Reports / Tasks",
                        clusterCount = clusterGroup.size,
                        originalEntityId = primary.originalEntityId,
                        timestamp = primary.timestamp
                    )
                )
            } else {
                clustered.add(markers[i])
            }
        }

        return clustered
    }

    /**
     * Checks if user is within the geofence radius of a task location (e.g., 50 meters).
     */
    fun isWithinGeofence(
        userLat: Double,
        userLng: Double,
        targetLat: Double,
        targetLng: Double,
        geofenceRadiusMeters: Float = 50f
    ): Boolean {
        if (userLat == 0.0 || userLng == 0.0 || targetLat == 0.0 || targetLng == 0.0) return false
        val dist = calculateDistanceMeters(userLat, userLng, targetLat, targetLng)
        return dist <= geofenceRadiusMeters
    }

    /**
     * Returns nearby markers within a given radius (e.g. 500 meters) of a target point.
     */
    fun findNearbyMarkers(
        targetLat: Double,
        targetLng: Double,
        allMarkers: List<CivicMapMarker>,
        radiusMeters: Float = 500f,
        excludeId: String = ""
    ): List<CivicMapMarker> {
        if (targetLat == 0.0 || targetLng == 0.0) return emptyList()

        return allMarkers.mapNotNull { marker ->
            if (marker.id == excludeId || marker.type == CivicMapType.CLUSTER) return@mapNotNull null
            val dist = calculateDistanceMeters(targetLat, targetLng, marker.latitude, marker.longitude)
            if (dist <= radiusMeters) {
                marker.copy(distanceMeters = dist)
            } else {
                null
            }
        }.sortedBy { it.distanceMeters ?: Float.MAX_VALUE }
    }
}
