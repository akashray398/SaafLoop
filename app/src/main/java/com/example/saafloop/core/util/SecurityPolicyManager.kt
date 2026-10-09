package com.example.saafloop.core.util

import com.example.saafloop.core.model.CaseStatus
import com.example.saafloop.core.model.PermissionCapability
import com.example.saafloop.core.model.SecurityRole
import kotlin.math.roundToInt

/**
 * Enforces Role-Based Access Control (RBAC), report state machine transitions,
 * location anonymization, and prompt/input sanitization.
 */
object SecurityPolicyManager {

    /**
     * Evaluates permission capability according to the Section 4 RBAC matrix.
     */
    fun hasPermission(role: SecurityRole, capability: PermissionCapability): Boolean {
        return when (role) {
            SecurityRole.PLATFORM_ADMIN -> true

            SecurityRole.ORGANIZATION_ADMIN -> when (capability) {
                PermissionCapability.CREATE_REPORT,
                PermissionCapability.VIEW_PUBLIC_REPORTS,
                PermissionCapability.VIEW_PRIVATE_DETAILS,
                PermissionCapability.MANAGE_ORG_MEMBERS,
                PermissionCapability.PUBLISH_ACTIVITIES,
                PermissionCapability.VIEW_SYSTEM_ANALYTICS -> true
                else -> false
            }

            SecurityRole.COORDINATOR -> when (capability) {
                PermissionCapability.CREATE_REPORT,
                PermissionCapability.VIEW_PUBLIC_REPORTS,
                PermissionCapability.VIEW_PRIVATE_DETAILS,
                PermissionCapability.VERIFY_REPORTS,
                PermissionCapability.ASSIGN_TASKS,
                PermissionCapability.UPDATE_ASSIGNED_TASK,
                PermissionCapability.PUBLISH_ACTIVITIES,
                PermissionCapability.VIEW_SYSTEM_ANALYTICS -> true
                else -> false
            }

            SecurityRole.FIELD_WORKER -> when (capability) {
                PermissionCapability.CREATE_REPORT,
                PermissionCapability.VIEW_PUBLIC_REPORTS,
                PermissionCapability.UPDATE_ASSIGNED_TASK -> true
                else -> false
            }

            SecurityRole.VOLUNTEER -> when (capability) {
                PermissionCapability.CREATE_REPORT,
                PermissionCapability.VIEW_PUBLIC_REPORTS,
                PermissionCapability.PUBLISH_ACTIVITIES -> true
                else -> false
            }

            SecurityRole.CITIZEN -> when (capability) {
                PermissionCapability.CREATE_REPORT,
                PermissionCapability.VIEW_PUBLIC_REPORTS -> true
                else -> false
            }
        }
    }

    /**
     * Validates whether a state transition is allowed for a user role.
     */
    fun isValidReportStateTransition(
        currentStatus: CaseStatus,
        targetStatus: CaseStatus,
        role: SecurityRole
    ): Boolean {
        if (currentStatus == targetStatus) return true

        // Citizens can only submit drafts
        if (role == SecurityRole.CITIZEN || role == SecurityRole.VOLUNTEER) {
            return (currentStatus == CaseStatus.DRAFT && targetStatus == CaseStatus.SUBMITTED)
        }

        // Coordinators & Admins handle verification & triage transitions
        if (role == SecurityRole.COORDINATOR || role == SecurityRole.ORGANIZATION_ADMIN || role == SecurityRole.PLATFORM_ADMIN) {
            return when (currentStatus) {
                CaseStatus.SUBMITTED -> targetStatus == CaseStatus.UNDER_REVIEW || targetStatus == CaseStatus.REJECTED || targetStatus == CaseStatus.DUPLICATE
                CaseStatus.UNDER_REVIEW -> targetStatus == CaseStatus.VERIFIED || targetStatus == CaseStatus.NEEDS_INFORMATION || targetStatus == CaseStatus.REJECTED || targetStatus == CaseStatus.DUPLICATE
                CaseStatus.VERIFIED -> targetStatus == CaseStatus.ASSIGNED || targetStatus == CaseStatus.IN_PROGRESS
                CaseStatus.ASSIGNED -> targetStatus == CaseStatus.IN_PROGRESS || targetStatus == CaseStatus.AWAITING_VERIFICATION
                CaseStatus.IN_PROGRESS -> targetStatus == CaseStatus.AWAITING_VERIFICATION || targetStatus == CaseStatus.VERIFIED_CLEAN
                CaseStatus.AWAITING_VERIFICATION -> targetStatus == CaseStatus.VERIFIED_CLEAN || targetStatus == CaseStatus.RESOLVED || targetStatus == CaseStatus.REOPENED
                CaseStatus.VERIFIED_CLEAN -> targetStatus == CaseStatus.RESOLVED || targetStatus == CaseStatus.REOPENED
                CaseStatus.RESOLVED -> targetStatus == CaseStatus.REOPENED || targetStatus == CaseStatus.CLOSED
                else -> true
            }
        }

        return false
    }

    /**
     * Anonymizes exact lat/lng coordinates to a ~1.1 km grid when anonymization is requested.
     */
    fun anonymizeLocationCoordinates(
        lat: Double,
        lng: Double,
        anonymize: Boolean = true
    ): Pair<Double, Double> {
        if (!anonymize || (lat == 0.0 && lng == 0.0)) return Pair(lat, lng)

        // Round to 2 decimal places (~1.1 km grid)
        val roundedLat = (lat * 100.0).roundToInt() / 100.0
        val roundedLng = (lng * 100.0).roundToInt() / 100.0
        return Pair(roundedLat, roundedLng)
    }

    /**
     * Sanitizes user input text to strip HTML tags, script tags, and prompt injection syntax.
     */
    fun sanitizeUserInputText(input: String): String {
        if (input.isBlank()) return ""

        var clean = input
            .replace(Regex("<[^>]*>"), "") // Remove HTML tags
            .replace(Regex("(?i)system instruction:"), "")
            .replace(Regex("(?i)ignore previous instructions"), "")
            .replace(Regex("(?i)grant admin"), "")
            .trim()

        if (clean.length > 300) {
            clean = clean.take(300)
        }
        return clean
    }
}
