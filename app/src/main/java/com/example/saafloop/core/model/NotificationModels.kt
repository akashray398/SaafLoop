package com.example.saafloop.core.model

enum class NotificationType(val label: String, val channelId: String) {
    REPORT("Report Update", "channel_report_updates"),
    TASK("Task Operations", "channel_task_operations"),
    COMMUNITY("Community Activity", "channel_community_activities"),
    ORGANIZATION("Organization Alert", "channel_system_alerts"),
    AI("AI Insights", "channel_ai_insights"),
    SYSTEM("System Announcement", "channel_system_alerts")
}

enum class NotificationEventType {
    REPORT_SUBMITTED,
    REPORT_VERIFIED,
    REPORT_NEEDS_INFORMATION,
    REPORT_REJECTED,
    REPORT_DUPLICATE,
    REPORT_RESOLVED,
    REPORT_REOPENED,

    TASK_CREATED,
    TASK_ASSIGNED,
    TASK_ACCEPTED,
    TASK_DECLINED,
    TASK_OVERDUE,
    TASK_COMPLETION_SUBMITTED,
    TASK_COMPLETED,
    TASK_REWORK_REQUESTED,

    ACTIVITY_APPROVED,
    ACTIVITY_PUBLISHED,
    ACTIVITY_UPDATED,
    ACTIVITY_CANCELLED,
    ACTIVITY_JOINED,

    ORGANIZATION_VERIFIED,

    AI_INSIGHT_CREATED,
    AI_REVIEW_REQUIRED,

    SYSTEM_ANNOUNCEMENT
}

enum class NotificationPriority(val label: String) {
    LOW("Low"),
    NORMAL("Normal"),
    HIGH("High"),
    CRITICAL("Critical")
}

enum class NotificationStatus {
    PENDING,
    SENT,
    DELIVERED,
    READ,
    EXPIRED,
    CANCELLED
}

data class NotificationItem(
    val notificationId: String,
    val recipientUserId: String = "ALL",
    val recipientRole: String = "ALL",
    val type: NotificationType,
    val eventType: NotificationEventType,
    val title: String,
    val body: String,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val channelId: String = type.channelId,
    val deepLink: String = "",
    val entityType: String = "",
    val entityId: String = "",
    val status: NotificationStatus = NotificationStatus.SENT,
    val createdAt: Long = System.currentTimeMillis(),
    val readAt: Long? = null,
    val expiresAt: Long? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    val isRead: Boolean
        get() = readAt != null || status == NotificationStatus.READ
}

data class NotificationPreferences(
    val userId: String = "",
    val reportsEnabled: Boolean = true,
    val tasksEnabled: Boolean = true,
    val communityEnabled: Boolean = true,
    val organizationEnabled: Boolean = true,
    val aiInsightsEnabled: Boolean = true,
    val systemAnnouncementsEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00"
)

data class UserDevice(
    val deviceId: String,
    val userId: String,
    val pushToken: String,
    val platform: String = "ANDROID",
    val appVersion: String = "1.0",
    val lastSeenAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
