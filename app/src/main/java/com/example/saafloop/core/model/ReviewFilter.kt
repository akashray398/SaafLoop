package com.example.saafloop.core.model

data class ReviewFilter(
    val status: CaseStatus? = null,
    val priority: CasePriority? = null,
    val category: String? = null,
    val searchQuery: String = "",
    val dateRangeDays: Int? = null
)

data class CoordinatorMetrics(
    val newReportsCount: Int = 0,
    val underReviewCount: Int = 0,
    val highPriorityCount: Int = 0,
    val needsInfoCount: Int = 0,
    val verifiedCount: Int = 0,
    val escalatedCount: Int = 0,
    val averageReviewTimeMinutes: Int = 12
)
