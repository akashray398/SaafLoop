package com.example.saafloop.core.model

import com.example.saafloop.feature.explore.model.CaseStatus

enum class CivicMapType(val label: String) {
    REPORT("Public Issue"),
    FIELD_TASK("Field Task"),
    COMMUNITY_ACTIVITY("Community Activity"),
    RESOLVED_ISSUE("Resolved Area"),
    CLUSTER("Cluster")
}

data class CivicMapMarker(
    val id: String,
    val type: CivicMapType,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val category: String,
    val areaName: String,
    val statusLabel: String,
    val rawStatus: String = "",
    val priority: CasePriority? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val originalEntityId: String,
    val photoPath: String? = null,
    val clusterCount: Int = 1,
    val distanceMeters: Float? = null,
    val description: String = "",
    val isDuplicateCandidate: Boolean = false,
    val isPrivateLocation: Boolean = false,
    val organizerOrAssignee: String? = null
)

enum class TimeFilterRange(val label: String) {
    ALL_TIME("All Time"),
    TODAY("Today"),
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days"),
    LAST_3_MONTHS("Last 3 Months")
}

data class MapFilterState(
    val selectedTypes: Set<CivicMapType> = setOf(
        CivicMapType.REPORT,
        CivicMapType.FIELD_TASK,
        CivicMapType.COMMUNITY_ACTIVITY,
        CivicMapType.RESOLVED_ISSUE
    ),
    val selectedStatuses: Set<String> = setOf(
        "Reported",
        "Verified",
        "Being Addressed",
        "Resolved"
    ),
    val selectedCategories: Set<String> = emptySet(),
    val selectedTimeRange: TimeFilterRange = TimeFilterRange.ALL_TIME,
    val selectedPriorities: Set<CasePriority> = emptySet(),
    val showHeatmap: Boolean = false,
    val showAnalyticsMode: Boolean = false
)

data class SectorOverview(
    val sectorName: String,
    val openIssues: Int,
    val activeTasks: Int,
    val resolvedThisMonth: Int,
    val isRepeatProblemArea: Boolean = false,
    val recurringReportsCount: Int = 0
)

data class GeoAnalyticsState(
    val totalOpenIssues: Int = 0,
    val totalActiveTasks: Int = 0,
    val totalResolved: Int = 0,
    val repeatProblemAreasCount: Int = 0,
    val sectorSummaries: List<SectorOverview> = emptyList()
)

data class LocationConfirmationState(
    val latitude: Double,
    val longitude: Double,
    val areaName: String,
    val estimatedAccuracyMeters: Float = 15f,
    val isLowAccuracy: Boolean = false
)

enum class CivicMapViewMode {
    MAP,
    LIST
}
