package com.example.saafloop.core.model

/**
 * Case lifecycle states for the SaafLoop waste reporting and verification loop.
 */
enum class CaseStatus(val label: String, val description: String) {
    DRAFT("Draft", "Local offline draft on device"),
    SUBMITTED("Submitted", "Report received and waiting for triage"),
    UNDER_REVIEW("Under Review", "Being reviewed by municipal/community coordinators"),
    NEEDS_INFORMATION("Needs Info", "Coordinator requested clarification from reporter"),
    VERIFIED("Verified", "Validated report confirmed for cleanup assignment"),
    ASSIGNED("Assigned", "Assigned to a participating cleanup team"),
    IN_PROGRESS("In Progress", "Cleanup drive or response actively underway"),
    AWAITING_VERIFICATION("Awaiting Verification", "Cleanup finished, pending resident/community check"),
    VERIFIED_CLEAN("Verified Clean", "Independently verified and confirmed clean"),
    RESOLVED("Resolved", "Cleanup completed and closed"),
    CLOSED("Closed", "Case closed and archived"),
    REJECTED("Rejected", "Invalid, duplicate, or out of scope report"),
    DUPLICATE("Duplicate", "Merged as duplicate into primary report"),
    ESCALATED("Escalated", "Escalated to municipal authorities"),
    REOPENED("Reopened", "Repeat dumping detected at site")
}
