package com.example.saafloop.core.model

/**
 * Timeframe options for filtering civic analytics dashboards.
 */
enum class AnalyticsTimeRange(val label: String, val days: Int) {
    LAST_7_DAYS("7 Days", 7),
    LAST_30_DAYS("30 Days", 30),
    LAST_90_DAYS("90 Days", 90),
    THIS_YEAR("This Year", 365),
    ALL_TIME("All Time", 3650)
}

/**
 * Individual timeline contribution item for personal civic history.
 */
data class ContributionTimelineItem(
    val id: String,
    val title: String,
    val category: String,
    val areaName: String,
    val eventType: String, // "SUBMITTED", "VERIFIED", "RESOLVED", "ACTIVITY_ATTENDED"
    val timestamp: Long,
    val statusLabel: String
)

/**
 * Personal impact summary metrics for citizens and volunteers.
 */
data class PersonalImpactMetrics(
    val userId: String = "",
    val totalSubmittedReports: Int = 0,
    val underReviewReports: Int = 0,
    val verifiedReports: Int = 0,
    val resolvedReports: Int = 0,
    val communityActivitiesJoined: Int = 0,
    val activitiesCompleted: Int = 0,
    val verifiedVolunteerHours: Float = 0f,
    val contributionScore: Int = 0
)

/**
 * Operational metrics for coordinators and administrators.
 */
data class OperationalAnalyticsData(
    val reportingPeriod: AnalyticsTimeRange = AnalyticsTimeRange.LAST_30_DAYS,
    val totalSubmittedVolume: Int = 0,
    val uniqueIssueCount: Int = 0,
    val verifiedCount: Int = 0,
    val resolvedCount: Int = 0,
    val rejectedCount: Int = 0,
    val duplicateMergedCount: Int = 0,
    val reopenedCount: Int = 0,

    val totalTasksCreated: Int = 0,
    val activeTasksCount: Int = 0,
    val completedTasksCount: Int = 0,
    val overdueTasksCount: Int = 0,

    val medianReviewTimeMinutes: Long = 0,
    val medianResolutionTimeHours: Long = 0,
    val slaTargetMetPercentage: Float = 0f,
    val firstTimeApprovalRate: Float = 0f,

    val categoryDistribution: List<Pair<String, Int>> = emptyList(),
    val dailyTrendData: List<DailyAnalyticsPoint> = emptyList(),
    val sectorPerformance: List<SectorAnalyticsItem> = emptyList(),
    val lastRefreshedAt: Long = System.currentTimeMillis()
)

/**
 * Single data point for daily report trend charts.
 */
data class DailyAnalyticsPoint(
    val dateLabel: String,
    val timestamp: Long,
    val submittedCount: Int,
    val verifiedCount: Int,
    val resolvedCount: Int
)

/**
 * Performance breakdown for a specific municipal sector/ward.
 */
data class SectorAnalyticsItem(
    val sectorName: String,
    val totalReports: Int,
    val activeTasks: Int,
    val resolvedCount: Int,
    val repeatDumpingRate: Float,
    val isHotspot: Boolean = false
)

/**
 * Data quality audit indicators for administrators.
 */
data class DataQualityMetrics(
    val inconsistentTimestampCount: Int = 0,
    val unverifiedResolutionSubmissions: Int = 0,
    val orphanedTaskReferences: Int = 0,
    val dataQualityScore: Float = 0.98f,
    val auditStatusMessage: String = "All analytics data sources verified and synchronized."
)
