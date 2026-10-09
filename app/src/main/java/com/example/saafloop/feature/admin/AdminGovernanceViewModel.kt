package com.example.saafloop.feature.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saafloop.core.data.AdminGovernanceRepository
import com.example.saafloop.core.data.AdminGovernanceRepositoryImpl
import com.example.saafloop.core.data.AuthRepository
import com.example.saafloop.core.data.AuthRepositoryImpl
import com.example.saafloop.core.data.SecurityRepository
import com.example.saafloop.core.data.SecurityRepositoryImpl
import com.example.saafloop.core.domain.GetGovernanceCasesUseCase
import com.example.saafloop.core.domain.ResolveGovernanceCaseUseCase
import com.example.saafloop.core.model.GovernanceAppeal
import com.example.saafloop.core.model.GovernanceCase
import com.example.saafloop.core.model.GovernanceCaseStatus
import com.example.saafloop.core.model.OperationalEscalationItem
import com.example.saafloop.core.model.OrgVerificationReview
import com.example.saafloop.core.model.SecurityAuditEvent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminGovernanceViewModel(application: Application) : AndroidViewModel(application) {

    private val govRepository: AdminGovernanceRepository = AdminGovernanceRepositoryImpl(application)
    private val secRepository: SecurityRepository = SecurityRepositoryImpl(application)
    private val authRepository: AuthRepository = AuthRepositoryImpl(application)

    private val getUseCase = GetGovernanceCasesUseCase(govRepository)
    private val resolveUseCase = ResolveGovernanceCaseUseCase(govRepository)

    val governanceCasesState: StateFlow<List<GovernanceCase>> = getUseCase.observeCases()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val appealsState: StateFlow<List<GovernanceAppeal>> = getUseCase.observeAppeals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val orgReviewsState: StateFlow<List<OrgVerificationReview>> = getUseCase.observeOrgReviews()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val escalationsState: StateFlow<List<OperationalEscalationItem>> = getUseCase.observeEscalations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val auditLogsState: StateFlow<List<SecurityAuditEvent>> = secRepository.observeSecurityAuditLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun resolveCase(caseId: String, targetStatus: GovernanceCaseStatus, decisionReason: String) {
        val uid = authRepository.getCurrentUserUid() ?: "ADMIN_USER"
        viewModelScope.launch {
            resolveUseCase.resolveCase(caseId, targetStatus, decisionReason, uid)
        }
    }

    fun resolveAppeal(appealId: String, outcomeCode: String, decisionNotes: String) {
        val uid = authRepository.getCurrentUserUid() ?: "ADMIN_USER"
        viewModelScope.launch {
            resolveUseCase.resolveAppeal(appealId, outcomeCode, decisionNotes, uid)
        }
    }

    fun updateOrgVerification(reviewId: String, newStatus: String, decisionNotes: String) {
        val uid = authRepository.getCurrentUserUid() ?: "ADMIN_USER"
        viewModelScope.launch {
            resolveUseCase.updateOrgVerification(reviewId, newStatus, decisionNotes, uid)
        }
    }
}
