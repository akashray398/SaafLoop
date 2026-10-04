package com.example.saafloop

import com.example.saafloop.core.model.CasePriority
import com.example.saafloop.core.model.CivicMapMarker
import com.example.saafloop.core.model.CivicMapType
import com.example.saafloop.core.util.GeoUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoUtilsTest {

    @Test
    fun calculateDistanceMeters_returnsCorrectHaversineDistance() {
        // Delhi Connaught Place to India Gate (~2.3 km)
        val lat1 = 28.6315
        val lon1 = 77.2167
        val lat2 = 28.6129
        val lon2 = 77.2295

        val distance = GeoUtils.calculateDistanceMeters(lat1, lon1, lat2, lon2)
        assertTrue("Distance should be approximately 2300 meters", distance in 2000f..2600f)
    }

    @Test
    fun formatDistance_formatsMetersAndKilometersCorrectly() {
        assertEquals("320m away", GeoUtils.formatDistance(320f))
        assertEquals("1.4 km away", GeoUtils.formatDistance(1400f))
        assertEquals("Nearby", GeoUtils.formatDistance(null))
    }

    @Test
    fun isWithinGeofence_detectsTaskArrivalCorrectly() {
        val userLat = 28.6139
        val userLng = 77.2090

        // Point within 30 meters
        val nearLat = 28.6141
        val nearLng = 77.2091
        assertTrue(GeoUtils.isWithinGeofence(userLat, userLng, nearLat, nearLng, 50f))

        // Point 5 km away
        val farLat = 28.6800
        val farLng = 77.2500
        assertFalse(GeoUtils.isWithinGeofence(userLat, userLng, farLat, farLng, 50f))
    }

    @Test
    fun clusterMarkers_clustersNearbyMarkersAtLowZoom() {
        val marker1 = CivicMapMarker(
            id = "m1", type = CivicMapType.REPORT, latitude = 28.6139, longitude = 77.2090,
            title = "Garbage", category = "Garbage", areaName = "Sector 68", statusLabel = "Reported",
            originalEntityId = "c1"
        )
        val marker2 = CivicMapMarker(
            id = "m2", type = CivicMapType.REPORT, latitude = 28.6140, longitude = 77.2091,
            title = "Bin", category = "Bin", areaName = "Sector 68", statusLabel = "Reported",
            originalEntityId = "c2"
        )
        val farMarker = CivicMapMarker(
            id = "m3", type = CivicMapType.REPORT, latitude = 28.7000, longitude = 77.3000,
            title = "Plastic", category = "Plastic", areaName = "Sector 90", statusLabel = "Reported",
            originalEntityId = "c3"
        )

        val clustered = GeoUtils.clusterMarkers(listOf(marker1, marker2, farMarker), clusterRadiusMeters = 200.0)

        assertEquals(2, clustered.size)
        val cluster = clustered.find { it.type == CivicMapType.CLUSTER }
        assertTrue(cluster != null)
        assertEquals(2, cluster?.clusterCount)
    }

    @Test
    fun findNearbyMarkers_findsCandidatesWithin500Meters() {
        val primary = CivicMapMarker(
            id = "p1", type = CivicMapType.REPORT, latitude = 28.6139, longitude = 77.2090,
            title = "Garbage", category = "Garbage", areaName = "Sector 68", statusLabel = "Reported",
            originalEntityId = "c1"
        )
        val nearbyCandidate = CivicMapMarker(
            id = "p2", type = CivicMapType.REPORT, latitude = 28.6142, longitude = 77.2092,
            title = "Dump", category = "Garbage", areaName = "Sector 68", statusLabel = "Reported",
            originalEntityId = "c2"
        )
        val farCandidate = CivicMapMarker(
            id = "p3", type = CivicMapType.REPORT, latitude = 28.6500, longitude = 77.2500,
            title = "Far Dump", category = "Garbage", areaName = "Sector 12", statusLabel = "Reported",
            originalEntityId = "c3"
        )

        val candidates = GeoUtils.findNearbyMarkers(
            targetLat = primary.latitude,
            targetLng = primary.longitude,
            allMarkers = listOf(primary, nearbyCandidate, farCandidate),
            radiusMeters = 500f,
            excludeId = primary.id
        )

        assertEquals(1, candidates.size)
        assertEquals("p2", candidates.first().id)
    }
}
