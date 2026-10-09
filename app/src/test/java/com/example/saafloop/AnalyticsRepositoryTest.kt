package com.example.saafloop

import android.content.Context
import android.content.ContextWrapper
import com.example.saafloop.core.data.AnalyticsRepositoryImpl
import com.example.saafloop.core.model.AnalyticsTimeRange
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AnalyticsRepositoryTest {

    private lateinit var fakeContext: Context
    private lateinit var repository: AnalyticsRepositoryImpl

    @Before
    fun setUp() {
        fakeContext = AnalyticsTestContext()
        repository = AnalyticsRepositoryImpl(fakeContext)
    }

    @Test
    fun observePersonalImpact_returnsValidUserMetrics() = runBlocking {
        val metrics = repository.observePersonalImpact("user_100", AnalyticsTimeRange.ALL_TIME).first()

        assertNotNull(metrics)
        assertTrue(metrics.totalSubmittedReports >= 0)
        assertTrue(metrics.resolvedReports >= 0)
        assertTrue(metrics.contributionScore >= 0)
    }

    @Test
    fun observeOperationalAnalytics_calculatesVolumeAndUniqueIssuesCorrectly() = runBlocking {
        val analytics = repository.observeOperationalAnalytics(AnalyticsTimeRange.LAST_30_DAYS).first()

        assertNotNull(analytics)
        assertTrue(analytics.totalSubmittedVolume >= analytics.uniqueIssueCount)
        assertEquals(analytics.totalSubmittedVolume - analytics.duplicateMergedCount, analytics.uniqueIssueCount)
        assertTrue(analytics.categoryDistribution.isNotEmpty())
        assertTrue(analytics.dailyTrendData.isNotEmpty())
    }

    @Test
    fun observeOperationalAnalytics_calculatesSlaTargetPerformance() = runBlocking {
        val analytics = repository.observeOperationalAnalytics(AnalyticsTimeRange.LAST_30_DAYS).first()

        assertTrue(analytics.medianReviewTimeMinutes > 0)
        assertTrue(analytics.medianResolutionTimeHours > 0)
        assertTrue(analytics.slaTargetMetPercentage in 0f..1f)
    }

    @Test
    fun observePlatformDataQuality_returnsDataQualityMetrics() = runBlocking {
        val quality = repository.observePlatformDataQuality().first()

        assertNotNull(quality)
        assertTrue(quality.dataQualityScore in 0.9f..1.0f)
        assertTrue(quality.auditStatusMessage.isNotBlank())
    }
}

private class AnalyticsTestContext : ContextWrapper(null) {
    override fun getApplicationContext(): Context = this
}
