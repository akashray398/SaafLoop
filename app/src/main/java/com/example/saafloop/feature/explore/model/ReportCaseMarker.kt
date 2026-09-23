package com.example.saafloop.feature.explore.model

/**
 * Case status definitions for the SaafLoop waste reporting loop.
 */
enum class CaseStatus {
    REPORTED,
    IN_PROGRESS,
    AWAITING_VERIFICATION,
    VERIFIED_CLEAN
}

/**
 * Interface and domain model for future map case markers (Section 4/6).
 *
 * NOTE FOR SECTION 3:
 * No fake markers are displayed on the map in Section 3 to adhere to the
 * "do not display invented markers" requirement.
 */
interface ReportCaseMarker {
    val id: String
    val latitude: Double
    val longitude: Double
    val status: CaseStatus
    val title: String
    val timestamp: Long
}
