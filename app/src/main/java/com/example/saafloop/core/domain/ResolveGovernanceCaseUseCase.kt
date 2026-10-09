package com.example.saafloop.core.domain

import com.example.saafloop.core.data.AdminGovernanceRepository
import com.example.saafloop.core.model.GovernanceAppeal
import com.example.saafloop.core.model.GovernanceCase
import com.example.saafloop.core.model.GovernanceCaseStatus

class ResolveGovernanceCaseUseCase(
    private val repository: AdminGovernanceRepository
) {
    suspend fun createCase(case: GovernanceCase): Result<String> {
        return repository.createGovernanceCase(case)
    }

    suspend fun resolveCase(
        caseId: String,
        newStatus: GovernanceCaseStatus,
        decisionReason: String,
        reviewerUid: String
    ): Result<Unit> {
        return repository.resolveGovernanceCase(caseId, newStatus, decisionReason, reviewerUid)
    }

    suspend fun submitAppeal(appeal: GovernanceAppeal): Result<String> {
        return repository.submitAppeal(appeal)
    }

    suspend fun resolveAppeal(
        appealId: String,
        outcomeCode: String,
        decisionNotes: String,
        reviewerUid: String
    ): Result<Unit> {
        return repository.resolveAppeal(appealId, outcomeCode, decisionNotes, reviewerUid)
    }

    suspend fun updateOrgVerification(
        reviewId: String,
        newStatus: String,
        decisionNotes: String,
        reviewerUid: String
    ): Result<Unit> {
        return repository.updateOrgVerificationStatus(reviewId, newStatus, decisionNotes, reviewerUid)
    }
}
