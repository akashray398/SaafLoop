package com.example.saafloop.feature.coordinator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saafloop.core.data.AuthRepository
import com.example.saafloop.core.data.AuthRepositoryImpl
import com.example.saafloop.core.data.CoordinatorRepository
import com.example.saafloop.core.data.CoordinatorRepositoryImpl
import com.example.saafloop.core.model.AuditEvent
import com.example.saafloop.core.model.CaseReport
import com.example.saafloop.core.model.CoordinatorMetrics
import com.example.saafloop.core.model.ReviewFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CoordinatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CoordinatorRepository = CoordinatorRepositoryImpl(application)
    private val authRepository: AuthRepository = AuthRepositoryImpl(application)

    private val _filterState = MutableStateFlow(ReviewFilter())
    val filterState: StateFlow<ReviewFilter> = _filterState.asStateFlow()

    private val _selectedCaseId = MutableStateFlow<String?>(null)

    val metricsState: StateFlow<CoordinatorMetrics> = repository.observeCoordinatorMetrics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CoordinatorMetrics()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val reviewQueueState: StateFlow<List<CaseReport>> = _filterState
        .flatMapLatest { filter -> repository.observeReviewQueue(filter) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeReportDetailState: StateFlow<CaseReport?> = _selectedCaseId
        .flatMapLatest { caseId ->
            if (caseId.isNullOrBlank()) flowOf(null)
            else repository.observeReportDetails(caseId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeReportAuditHistoryState: StateFlow<List<AuditEvent>> = _selectedCaseId
        .flatMapLatest { caseId ->
            if (caseId.isNullOrBlank()) flowOf(emptyList())
            else repository.observeAuditHistory(caseId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun selectCaseForReview(caseId: String) {
        _selectedCaseId.value = caseId
        val uid = authRepository.getCurrentUserUid() ?: "COORDINATOR_LOCAL"
        viewModelScope.launch {
            repository.startReportReview(caseId, uid, "Municipal Coordinator")
        }
    }

    fun updateFilter(filter: ReviewFilter) {
        _filterState.value = filter
    }

    fun verifyReport(
        caseId: String,
        category: String,
        severity: String,
        priority: String,
        notes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = authRepository.getCurrentUserUid() ?: "COORDINATOR_LOCAL"
        viewModelScope.launch {
            val result = repository.verifyReport(
                caseId = caseId,
                category = category,
                severity = severity,
                priority = priority,
                notes = notes,
                coordinatorUid = uid,
                coordinatorName = "Municipal Coordinator"
            )
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Failed to verify report") }
        }
    }

    fun rejectReport(
        caseId: String,
        reason: String,
        notes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = authRepository.getCurrentUserUid() ?: "COORDINATOR_LOCAL"
        viewModelScope.launch {
            val result = repository.rejectReport(
                caseId = caseId,
                reason = reason,
                notes = notes,
                coordinatorUid = uid,
                coordinatorName = "Municipal Coordinator"
            )
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Failed to reject report") }
        }
    }

    fun requestMoreInfo(
        caseId: String,
        reason: String,
        message: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = authRepository.getCurrentUserUid() ?: "COORDINATOR_LOCAL"
        viewModelScope.launch {
            val result = repository.requestMoreInfo(
                caseId = caseId,
                reason = reason,
                message = message,
                coordinatorUid = uid,
                coordinatorName = "Municipal Coordinator"
            )
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Failed to request info") }
        }
    }

    fun mergeReports(
        primaryCaseId: String,
        duplicateCaseIds: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = authRepository.getCurrentUserUid() ?: "COORDINATOR_LOCAL"
        viewModelScope.launch {
            val result = repository.mergeReports(
                primaryCaseId = primaryCaseId,
                duplicateCaseIds = duplicateCaseIds,
                coordinatorUid = uid,
                coordinatorName = "Municipal Coordinator"
            )
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Failed to merge reports") }
        }
    }
}
