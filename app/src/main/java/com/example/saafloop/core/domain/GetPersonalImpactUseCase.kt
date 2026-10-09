package com.example.saafloop.core.domain

import com.example.saafloop.core.data.AnalyticsRepository
import com.example.saafloop.core.model.AnalyticsTimeRange
import com.example.saafloop.core.model.ContributionTimelineItem
import com.example.saafloop.core.model.PersonalImpactMetrics
import kotlinx.coroutines.flow.Flow

class GetPersonalImpactUseCase(
    private val repository: AnalyticsRepository
) {
    fun observeImpact(userId: String, timeRange: AnalyticsTimeRange): Flow<PersonalImpactMetrics> {
        return repository.observePersonalImpact(userId, timeRange)
    }

    fun observeTimeline(userId: String): Flow<List<ContributionTimelineItem>> {
        return repository.observePersonalTimeline(userId)
    }
}
