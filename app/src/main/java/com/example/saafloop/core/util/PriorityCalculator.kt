package com.example.saafloop.core.util

import com.example.saafloop.core.model.CasePriority
import com.example.saafloop.core.model.CaseReport
import com.example.saafloop.core.model.IssueSeverity

data class PriorityResult(
    val priority: CasePriority,
    val reasons: List<String>
)

object PriorityCalculator {

    fun calculatePriority(
        caseReport: CaseReport,
        severity: IssueSeverity = IssueSeverity.MODERATE,
        duplicateCount: Int = 0
    ): PriorityResult {
        val reasons = mutableListOf<String>()
        var score = 0

        // 1. Severity weight
        when (severity) {
            IssueSeverity.CRITICAL -> {
                score += 40
                reasons.add("Critical waste severity")
            }
            IssueSeverity.SEVERE -> {
                score += 30
                reasons.add("Severe waste accumulation")
            }
            IssueSeverity.MODERATE -> {
                score += 20
                reasons.add("Moderate waste volume")
            }
            IssueSeverity.MINOR -> {
                score += 10
                reasons.add("Minor waste spot")
            }
        }

        // 2. Hazardous warning
        if (caseReport.isHazardousSuspected) {
            score += 30
            reasons.add("Suspected hazardous / sharp waste")
        }

        // 3. Confirmations & Duplicates
        if (duplicateCount >= 3) {
            score += 20
            reasons.add("Multiple resident confirmations ($duplicateCount matches)")
        } else if (duplicateCount >= 1) {
            score += 10
            reasons.add("Confirmed by nearby residents")
        }

        // 4. Pending Duration
        val pendingHours = (System.currentTimeMillis() - caseReport.createdAt) / (1000 * 60 * 60)
        if (pendingHours >= 18) {
            score += 15
            reasons.add("Pending review for over $pendingHours hours")
        }

        val calculatedPriority = when {
            score >= 60 -> CasePriority.CRITICAL
            score >= 40 -> CasePriority.HIGH
            score >= 25 -> CasePriority.MEDIUM
            else -> CasePriority.LOW
        }

        return PriorityResult(
            priority = calculatedPriority,
            reasons = reasons
        )
    }
}
