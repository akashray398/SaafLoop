package com.example.saafloop.core.model

enum class ActivityCategory(val label: String, val description: String) {
    CLEANUP_DRIVE("Cleanup Drive", "Community garbage collection and site cleanup"),
    AWARENESS_CAMPAIGN("Awareness Drive", "Public sanitation awareness and education"),
    PLANTATION("Plantation Drive", "Tree planting and green cover enhancement"),
    RECYCLING_COLLECTION("Recycling Collection", "Plastic and recyclable waste collection drive"),
    CIVIC_MAINTENANCE("Public Maintenance", "Community maintenance of public bins/spaces"),
    EDUCATIONAL("Educational Workshop", "School, college, or community workshop"),
    SPECIAL_DRIVE("Special Civic Drive", "Coordinator-approved community cleanup event")
}

enum class ActivityStatus(val label: String) {
    DRAFT("Draft"),
    PENDING_APPROVAL("Pending Approval"),
    APPROVED("Approved"),
    PUBLISHED("Published"),
    ACTIVE("Active Now"),
    COMPLETED("Completed"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled")
}

enum class ParticipantStatus(val label: String) {
    JOINED("Joined"),
    WAITLISTED("Waitlisted"),
    ATTENDED("Attended & Verified"),
    CANCELLED("Cancelled"),
    NO_SHOW("No Show")
}

enum class OrgVerificationStatus(val label: String) {
    PENDING("Pending Verification"),
    VERIFIED("Verified Organization"),
    REJECTED("Rejected"),
    SUSPENDED("Suspended")
}
