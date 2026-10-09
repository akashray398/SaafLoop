package com.example.saafloop.core.domain

import com.example.saafloop.core.data.AdminGovernanceRepository
import com.example.saafloop.core.model.GovernanceAppeal
import com.example.saafloop.core.model.GovernanceCase
import com.example.saafloop.core.model.GovernanceCaseStatus
import com.example.saafloop.core.model.OperationalEscalationItem
import com.example.saafloop.core.model.OrgVerificationReview
import kotlinx.coroutines.flow.Flow

class GetGovernanceCasesUseCase(
    private val repository: AdminGovernanceRepository
) {
    fun observeCases(statusFilter: GovernanceCaseStatus? = null): Flow<List<GovernanceCase>> {
        return repository.observeGovernanceCases(statusFilter)
    }

    fun observeAppeals(): Flow<List<GovernanceAppeal>> {
        return repository.observeGovernanceAppeals()
    }

    fun observeOrgReviews(): Flow<List<OrgVerificationReview>> {
        return repository.observeOrgVerificationQueue()
    }

    fun observeEscalations(): Flow<List<OperationalEscalationItem>> {
        return repository.observeOperationalEscalations()
    }
}
