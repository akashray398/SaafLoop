package com.example.saafloop.core.domain

import com.example.saafloop.core.data.AnalyticsRepository
import com.example.saafloop.core.model.AnalyticsTimeRange
import com.example.saafloop.core.model.DataQualityMetrics
import com.example.saafloop.core.model.OperationalAnalyticsData
import kotlinx.coroutines.flow.Flow

class GetCoordinatorAnalyticsUseCase(
    private val repository: AnalyticsRepository
) {
    fun observeAnalytics(timeRange: AnalyticsTimeRange, sectorFilter: String? = null): Flow<OperationalAnalyticsData> {
        return repository.observeOperationalAnalytics(timeRange, sectorFilter)
    }

    fun observeDataQuality(): Flow<DataQualityMetrics> {
        return repository.observePlatformDataQuality()
    }
}
