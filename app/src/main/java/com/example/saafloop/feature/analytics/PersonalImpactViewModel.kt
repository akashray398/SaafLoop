package com.example.saafloop.feature.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saafloop.core.data.AnalyticsRepository
import com.example.saafloop.core.data.AnalyticsRepositoryImpl
import com.example.saafloop.core.data.AuthRepository
import com.example.saafloop.core.data.AuthRepositoryImpl
import com.example.saafloop.core.domain.GetPersonalImpactUseCase
import com.example.saafloop.core.model.AnalyticsTimeRange
import com.example.saafloop.core.model.ContributionTimelineItem
import com.example.saafloop.core.model.PersonalImpactMetrics
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class PersonalImpactViewModel(application: Application) : AndroidViewModel(application) {

    private val analyticsRepository: AnalyticsRepository = AnalyticsRepositoryImpl(application)
    private val authRepository: AuthRepository = AuthRepositoryImpl(application)
    private val useCase = GetPersonalImpactUseCase(analyticsRepository)

    private val _selectedTimeRange = MutableStateFlow(AnalyticsTimeRange.ALL_TIME)
    val selectedTimeRange: StateFlow<AnalyticsTimeRange> = _selectedTimeRange.asStateFlow()

    private val currentUserIdFlow = MutableStateFlow(authRepository.getCurrentUserUid() ?: "GUEST")

    @OptIn(ExperimentalCoroutinesApi::class)
    val personalMetricsState: StateFlow<PersonalImpactMetrics> = _selectedTimeRange
        .flatMapLatest { range ->
            val uid = authRepository.getCurrentUserUid() ?: "GUEST"
            useCase.observeImpact(uid, range)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PersonalImpactMetrics()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val timelineState: StateFlow<List<ContributionTimelineItem>> = currentUserIdFlow
        .flatMapLatest { uid ->
            useCase.observeTimeline(uid)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun setTimeRange(range: AnalyticsTimeRange) {
        _selectedTimeRange.value = range
    }
}
