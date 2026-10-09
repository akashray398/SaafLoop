package com.example.saafloop.core.model

/**
 * Roles in SaafLoop for RBAC authorization.
 */
enum class SecurityRole(val label: String, val hierarchyLevel: Int) {
    CITIZEN("Citizen Resident", 1),
    VOLUNTEER("Volunteer Helper", 2),
    FIELD_WORKER("Field Operations Worker", 3),
    COORDINATOR("Municipal Coordinator", 4),
    ORGANIZATION_ADMIN("Organization Administrator", 5),
    PLATFORM_ADMIN("Platform Administrator", 6)
}

/**
 * Capability permissions evaluated by the Security Policy Engine.
 */
enum class PermissionCapability {
    CREATE_REPORT,
    VIEW_PUBLIC_REPORTS,
    VIEW_PRIVATE_DETAILS,
    VERIFY_REPORTS,
    ASSIGN_TASKS,
    UPDATE_ASSIGNED_TASK,
    MANAGE_ORG_MEMBERS,
    PUBLISH_ACTIVITIES,
    VIEW_SYSTEM_ANALYTICS,
    MANAGE_ROLES,
    MANAGE_SECURITY_POLICIES
}

/**
 * User-configurable privacy preferences.
 */
data class PrivacySettings(
    val userId: String = "",
    val anonymizeExactLocation: Boolean = true,
    val hideProfileFromPublic: Boolean = false,
    val allowNotificationAlerts: Boolean = true,
    val shareAnalyticsConsent: Boolean = true
)

/**
 * Audit log entry for security and operational monitoring.
 */
data class SecurityAuditEvent(
    val eventId: String,
    val eventType: String, // "AUTH_SUCCESS", "AUTH_FAILURE", "REPORT_VERIFIED", "ROLE_CHANGED", etc.
    val actorUid: String,
    val actorRole: String,
    val targetResourceId: String = "",
    val action: String,
    val outcome: String, // "SUCCESS", "DENIED", "FAILED"
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Abuse or spam flag report submitted by users for moderation.
 */
data class AbuseReport(
    val abuseId: String,
    val targetEntityType: String, // "REPORT", "COMMENT", "ACTIVITY", "USER"
    val targetEntityId: String,
    val reporterUid: String,
    val reason: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "PENDING_REVIEW"
)
