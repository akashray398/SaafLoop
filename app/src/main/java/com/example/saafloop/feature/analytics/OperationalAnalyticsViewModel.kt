package com.example.saafloop.feature.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saafloop.core.data.AnalyticsRepository
import com.example.saafloop.core.data.AnalyticsRepositoryImpl
import com.example.saafloop.core.domain.GetCoordinatorAnalyticsUseCase
import com.example.saafloop.core.model.AnalyticsTimeRange
import com.example.saafloop.core.model.DataQualityMetrics
import com.example.saafloop.core.model.OperationalAnalyticsData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class OperationalAnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AnalyticsRepository = AnalyticsRepositoryImpl(application)
    private val useCase = GetCoordinatorAnalyticsUseCase(repository)

    private val _selectedTimeRange = MutableStateFlow(AnalyticsTimeRange.LAST_30_DAYS)
    val selectedTimeRange: StateFlow<AnalyticsTimeRange> = _selectedTimeRange.asStateFlow()

    private val _selectedSector = MutableStateFlow<String?>(null)
    val selectedSector: StateFlow<String?> = _selectedSector.asStateFlow()

    private val filterParamsFlow = combine(_selectedTimeRange, _selectedSector) { range, sector ->
        Pair(range, sector)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val operationalAnalyticsState: StateFlow<OperationalAnalyticsData> = filterParamsFlow
        .flatMapLatest { (range, sector) ->
            useCase.observeAnalytics(range, sector)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = OperationalAnalyticsData()
        )

    val dataQualityState: StateFlow<DataQualityMetrics> = useCase.observeDataQuality()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DataQualityMetrics()
        )

    fun setTimeRange(range: AnalyticsTimeRange) {
        _selectedTimeRange.value = range
    }

    fun setSectorFilter(sector: String?) {
        _selectedSector.value = sector
    }
}
