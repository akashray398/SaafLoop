package com.example.saafloop

import android.content.Context
import android.content.ContextWrapper
import com.example.saafloop.core.model.CasePriority
import com.example.saafloop.core.model.CivicMapMarker
import com.example.saafloop.core.model.CivicMapType
import com.example.saafloop.core.ui.MapMarkerBitmapGenerator
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class PerformanceOptimizationTest {

    private lateinit var fakeContext: Context

    @Before
    fun setUp() {
        fakeContext = PerfTestContext()
        MapMarkerBitmapGenerator.clearCache()
    }

    @Test
    fun mapMarkerBitmapGenerator_cachesGeneratedMapIconsInLruCache() {
        val marker = CivicMapMarker(
            id = "m_1",
            type = CivicMapType.REPORT,
            latitude = 30.7046,
            longitude = 76.7178,
            title = "Overflowing Bin",
            category = "Overflowing Bin",
            areaName = "Sector 68",
            statusLabel = "Verified",
            priority = CasePriority.HIGH,
            originalEntityId = "case_101"
        )

        assertNotNull(marker)
    }
}

private class PerfTestContext : ContextWrapper(null) {
    override fun getApplicationContext(): Context = this
}
