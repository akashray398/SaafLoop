package com.example.saafloop.core.domain

import com.example.saafloop.core.data.CivicAIRepository
import com.example.saafloop.core.model.CaseReport
import com.example.saafloop.core.model.ReportAIAnalysis
import com.example.saafloop.core.util.PriorityResult

class SuggestReportSeverityUseCase(
    private val repository: CivicAIRepository
) {
    suspend operator fun invoke(
        caseReport: CaseReport,
        imageAnalysis: ReportAIAnalysis?,
        duplicateCount: Int
    ): PriorityResult {
        return repository.suggestSeverityAndPriority(
            caseReport = caseReport,
            imageAnalysis = imageAnalysis,
            duplicateCount = duplicateCount
        )
    }
}
