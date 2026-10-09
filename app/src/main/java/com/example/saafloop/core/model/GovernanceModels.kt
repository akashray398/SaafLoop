package com.example.saafloop.core.model

/**
 * Categories for admin governance and trust & safety cases.
 */
enum class GovernanceCaseCategory(val label: String) {
    SUSPECTED_FALSE_REPORT("Suspected False Report"),
    HARASSMENT_ABUSE("Harassment or Abuse"),
    TASK_DISPUTE("Task Execution Dispute"),
    ORGANIZATION_VERIFICATION("Organization Verification Request"),
    UNSAFE_CLEANUP_ACTIVITY("Unsafe Activity / Event"),
    PRIVACY_COMPLAINT("Location or Privacy Complaint"),
    FRAUDULENT_EVIDENCE("Fraudulent Completion Evidence")
}

/**
 * Lifecycle state machine for admin governance cases.
 */
enum class GovernanceCaseStatus(val label: String) {
    OPEN("Open"),
    TRIAGED("Triaged"),
    UNDER_REVIEW("Under Review"),
    ACTION_REQUIRED("Action Required"),
    RESOLVED("Resolved"),
    CLOSED("Closed"),
    DISMISSED("Dismissed"),
    ESCALATED("Escalated")
}

/**
 * Detailed governance case entity for moderation and dispute resolution.
 */
data class GovernanceCase(
    val caseId: String = "",
    val category: GovernanceCaseCategory = GovernanceCaseCategory.SUSPECTED_FALSE_REPORT,
    val status: GovernanceCaseStatus = GovernanceCaseStatus.OPEN,
    val priority: CasePriority = CasePriority.MEDIUM,
    val targetResourceType: String = "REPORT", // "REPORT", "TASK", "ACTIVITY", "ORGANIZATION", "USER"
    val targetResourceId: String = "",
    val reporterUid: String = "",
    val assignedReviewerUid: String = "",
    val summary: String = "",
    val notes: String = "",
    val resolutionCode: String = "",
    val decisionReason: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null
)

/**
 * Appeal entity submitted by users to dispute a report rejection, task decision, or moderation action.
 */
data class GovernanceAppeal(
    val appealId: String = "",
    val originalResourceId: String = "",
    val appellantUid: String = "",
    val explanation: String = "",
    val status: String = "SUBMITTED", // "SUBMITTED", "UNDER_REVIEW", "UPHELD", "OVERTURNED", "PARTIALLY_UPHELD"
    val assignedReviewerUid: String = "",
    val outcomeCode: String = "",
    val decisionNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val decidedAt: Long? = null
)

/**
 * Organization verification application entity.
 */
data class OrgVerificationReview(
    val reviewId: String = "",
    val orgId: String = "",
    val orgName: String = "",
    val orgType: String = "NGO",
    val documentReferences: List<String> = emptyList(),
    val status: String = "SUBMITTED", // "SUBMITTED", "UNDER_REVIEW", "VERIFIED", "REJECTED", "SUSPENDED"
    val assignedReviewerUid: String = "",
    val decisionNotes: String = "",
    val submittedAt: Long = System.currentTimeMillis(),
    val decidedAt: Long? = null
)

/**
 * Operational escalation item for safety-critical issues or SLA deadlines.
 */
data class OperationalEscalationItem(
    val escalationId: String = "",
    val triggerCode: String = "SLA_BREACH_OVERDUE", // "SLA_BREACH_OVERDUE", "UNSAFE_CONDITIONS", "REPEAT_ABUSE"
    val priority: CasePriority = CasePriority.HIGH,
    val targetResourceId: String = "",
    val status: String = "OPEN", // "OPEN", "ACKNOWLEDGED", "RESOLVED"
    val assignedOwnerUid: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
