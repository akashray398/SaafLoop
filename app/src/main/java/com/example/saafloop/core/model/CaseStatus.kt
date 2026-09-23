package com.example.saafloop.core.model

/**
 * Case lifecycle states for the SaafLoop waste reporting and verification loop.
 */
enum class CaseStatus(val label: String, val description: String) {
    SUBMITTED("Submitted", "Report received and waiting for triage"),
    UNDER_REVIEW("Under Review", "Being reviewed by municipal/community coordinators"),
    ASSIGNED("Assigned", "Assigned to a participating cleanup team"),
    IN_PROGRESS("In Progress", "Cleanup drive or response actively underway"),
    AWAITING_VERIFICATION("Awaiting Verification", "Cleanup finished, pending resident/community check"),
    VERIFIED_CLEAN("Verified Clean", "Independently verified and confirmed clean"),
    REJECTED("Rejected", "Invalid, duplicate, or out of scope report"),
    REOPENED("Reopened", "Repeat dumping detected at site")
}
