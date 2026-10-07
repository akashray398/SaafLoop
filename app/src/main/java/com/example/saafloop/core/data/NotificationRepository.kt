package com.example.saafloop.core.data

import android.content.Context
import com.example.saafloop.core.model.NotificationEventType
import com.example.saafloop.core.model.NotificationItem
import com.example.saafloop.core.model.NotificationPreferences
import com.example.saafloop.core.model.NotificationPriority
import com.example.saafloop.core.model.NotificationStatus
import com.example.saafloop.core.model.NotificationType
import com.example.saafloop.core.model.UserDevice
import com.example.saafloop.core.util.NotificationChannelManager
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.UUID

interface NotificationRepository {
    fun observeUserNotifications(userId: String, userRole: String): Flow<List<NotificationItem>>
    fun observeUnreadCount(userId: String, userRole: String): Flow<Int>
    suspend fun markNotificationAsRead(notificationId: String): Result<Unit>
    suspend fun markAllNotificationsAsRead(userId: String): Result<Unit>
    fun observeNotificationPreferences(userId: String): Flow<NotificationPreferences>
    suspend fun updateNotificationPreferences(userId: String, prefs: NotificationPreferences): Result<Unit>
    suspend fun registerUserDevice(device: UserDevice): Result<Unit>
    suspend fun unregisterUserDevice(deviceId: String): Result<Unit>
    suspend fun dispatchNotificationEvent(
        eventType: NotificationEventType,
        entityType: String,
        entityId: String,
        title: String,
        body: String,
        recipientUserId: String = "ALL",
        recipientRole: String = "ALL",
        priority: NotificationPriority = NotificationPriority.NORMAL
    ): Result<String>
    suspend fun publishAdminAnnouncement(
        title: String,
        body: String,
        targetRole: String = "ALL",
        priority: NotificationPriority = NotificationPriority.HIGH
    ): Result<String>
}

class NotificationRepositoryImpl(private val context: Context) : NotificationRepository {

    private fun isFirebaseConfigured(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty() && FirebaseFirestore.getInstance().app != null
        } catch (_: Exception) {
            false
        }
    }

    override fun observeUserNotifications(userId: String, userRole: String): Flow<List<NotificationItem>> = callbackFlow {
        if (!isFirebaseConfigured()) {
            val initialFallback = createLocalSampleNotifications()
            trySend(initialFallback)
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collection("notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(createLocalSampleNotifications())
                    return@addSnapshotListener
                }

                val items = snapshot.documents.mapNotNull { doc ->
                    try {
                        val recUser = doc.getString("recipientUserId") ?: "ALL"
                        val recRole = doc.getString("recipientRole") ?: "ALL"

                        // Filter by authorization / recipient rules
                        val isForUser = recUser == "ALL" || recUser == userId
                        val isForRole = recRole == "ALL" || recRole.equals(userRole, ignoreCase = true)

                        if (isForUser && isForRole) {
                            val notifType = try { NotificationType.valueOf(doc.getString("type") ?: "SYSTEM") } catch (_: Exception) { NotificationType.SYSTEM }
                            val eventType = try { NotificationEventType.valueOf(doc.getString("eventType") ?: "SYSTEM_ANNOUNCEMENT") } catch (_: Exception) { NotificationEventType.SYSTEM_ANNOUNCEMENT }
                            val notifPriority = try { NotificationPriority.valueOf(doc.getString("priority") ?: "NORMAL") } catch (_: Exception) { NotificationPriority.NORMAL }
                            val notifStatus = try { NotificationStatus.valueOf(doc.getString("status") ?: "SENT") } catch (_: Exception) { NotificationStatus.SENT }

                            NotificationItem(
                                notificationId = doc.getString("notificationId") ?: doc.id,
                                recipientUserId = recUser,
                                recipientRole = recRole,
                                type = notifType,
                                eventType = eventType,
                                title = doc.getString("title") ?: "SaafLoop Update",
                                body = doc.getString("body") ?: "",
                                priority = notifPriority,
                                channelId = doc.getString("channelId") ?: notifType.channelId,
                                deepLink = doc.getString("deepLink") ?: "",
                                entityType = doc.getString("entityType") ?: "",
                                entityId = doc.getString("entityId") ?: "",
                                status = notifStatus,
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                readAt = doc.getLong("readAt"),
                                expiresAt = doc.getLong("expiresAt")
                            )
                        } else null
                    } catch (_: Exception) {
                        null
                    }
                }

                trySend(items)
            }

        awaitClose { listener.remove() }
    }

    override fun observeUnreadCount(userId: String, userRole: String): Flow<Int> {
        return observeUserNotifications(userId, userRole).map { notifications ->
            notifications.count { !it.isRead }
        }
    }

    override suspend fun markNotificationAsRead(notificationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured() || notificationId.isBlank()) return@withContext Result.success(Unit)

        try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("notifications")
                .document(notificationId)
                .update(
                    mapOf(
                        "status" to NotificationStatus.READ.name,
                        "readAt" to System.currentTimeMillis()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAllNotificationsAsRead(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.success(Unit)

        try {
            val firestore = FirebaseFirestore.getInstance()
            val query = firestore.collection("notifications")
                .whereEqualTo("status", NotificationStatus.SENT.name)
                .get()
                .await()

            val batch = firestore.batch()
            query.documents.forEach { doc ->
                batch.update(doc.reference, mapOf("status" to NotificationStatus.READ.name, "readAt" to System.currentTimeMillis()))
            }
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeNotificationPreferences(userId: String): Flow<NotificationPreferences> = callbackFlow {
        trySend(NotificationPreferences(userId = userId))
        close()
    }

    override suspend fun updateNotificationPreferences(
        userId: String,
        prefs: NotificationPreferences
    ): Result<Unit> = withContext(Dispatchers.IO) {
        Result.success(Unit)
    }

    override suspend fun registerUserDevice(device: UserDevice): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.success(Unit)

        try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("user_devices")
                .document(device.deviceId)
                .set(device)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unregisterUserDevice(deviceId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.success(Unit)

        try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("user_devices")
                .document(deviceId)
                .update("isActive", false)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun dispatchNotificationEvent(
        eventType: NotificationEventType,
        entityType: String,
        entityId: String,
        title: String,
        body: String,
        recipientUserId: String,
        recipientRole: String,
        priority: NotificationPriority
    ): Result<String> = withContext(Dispatchers.IO) {
        val notificationId = "notif_${eventType.name}_${entityId}_${recipientUserId}"

        val notifType = when (eventType) {
            NotificationEventType.REPORT_SUBMITTED,
            NotificationEventType.REPORT_VERIFIED,
            NotificationEventType.REPORT_NEEDS_INFORMATION,
            NotificationEventType.REPORT_REJECTED,
            NotificationEventType.REPORT_DUPLICATE,
            NotificationEventType.REPORT_RESOLVED,
            NotificationEventType.REPORT_REOPENED -> NotificationType.REPORT

            NotificationEventType.TASK_CREATED,
            NotificationEventType.TASK_ASSIGNED,
            NotificationEventType.TASK_ACCEPTED,
            NotificationEventType.TASK_DECLINED,
            NotificationEventType.TASK_OVERDUE,
            NotificationEventType.TASK_COMPLETION_SUBMITTED,
            NotificationEventType.TASK_COMPLETED,
            NotificationEventType.TASK_REWORK_REQUESTED -> NotificationType.TASK

            NotificationEventType.ACTIVITY_APPROVED,
            NotificationEventType.ACTIVITY_PUBLISHED,
            NotificationEventType.ACTIVITY_UPDATED,
            NotificationEventType.ACTIVITY_CANCELLED,
            NotificationEventType.ACTIVITY_JOINED -> NotificationType.COMMUNITY

            NotificationEventType.ORGANIZATION_VERIFIED -> NotificationType.ORGANIZATION

            NotificationEventType.AI_INSIGHT_CREATED,
            NotificationEventType.AI_REVIEW_REQUIRED -> NotificationType.AI

            NotificationEventType.SYSTEM_ANNOUNCEMENT -> NotificationType.SYSTEM
        }

        val deepLink = when (entityType.uppercase()) {
            "REPORT" -> "saafloop://report_review/$entityId"
            "TASK" -> "saafloop://field_task_detail/$entityId"
            "ACTIVITY" -> "saafloop://activity_detail/$entityId"
            else -> "saafloop://notification_center"
        }

        val notifItem = NotificationItem(
            notificationId = notificationId,
            recipientUserId = recipientUserId,
            recipientRole = recipientRole,
            type = notifType,
            eventType = eventType,
            title = title,
            body = body,
            priority = priority,
            channelId = notifType.channelId,
            deepLink = deepLink,
            entityType = entityType,
            entityId = entityId,
            status = NotificationStatus.SENT,
            createdAt = System.currentTimeMillis()
        )

        if (isFirebaseConfigured()) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("notifications")
                    .document(notificationId)
                    .set(notifItem)
                    .await()
            } catch (_: Exception) {
                // Fallback local dispatch
            }
        }

        // Post system notification on device
        NotificationChannelManager.postSystemNotification(context, notifItem)

        Result.success(notificationId)
    }

    override suspend fun publishAdminAnnouncement(
        title: String,
        body: String,
        targetRole: String,
        priority: NotificationPriority
    ): Result<String> = withContext(Dispatchers.IO) {
        dispatchNotificationEvent(
            eventType = NotificationEventType.SYSTEM_ANNOUNCEMENT,
            entityType = "SYSTEM",
            entityId = "announcement_${UUID.randomUUID()}",
            title = title,
            body = body,
            recipientUserId = "ALL",
            recipientRole = targetRole,
            priority = priority
        )
    }

    private fun createLocalSampleNotifications(): List<NotificationItem> {
        val now = System.currentTimeMillis()
        return listOf(
            NotificationItem(
                notificationId = "n_1",
                recipientUserId = "ALL",
                recipientRole = "ALL",
                type = NotificationType.REPORT,
                eventType = NotificationEventType.REPORT_VERIFIED,
                title = "Report Verified",
                body = "Your waste report at Sector 68 has been verified and queued for cleanup dispatch.",
                priority = NotificationPriority.NORMAL,
                deepLink = "saafloop://report_review/case_sample_1",
                entityType = "REPORT",
                entityId = "case_sample_1",
                createdAt = now - 10 * 60 * 1000L
            ),
            NotificationItem(
                notificationId = "n_2",
                recipientUserId = "ALL",
                recipientRole = "VOLUNTEER",
                type = NotificationType.TASK,
                eventType = NotificationEventType.TASK_ASSIGNED,
                title = "New Field Task Assigned",
                body = "You have been assigned to Sector 70 Overflowing Bin cleanup drive.",
                priority = NotificationPriority.HIGH,
                deepLink = "saafloop://field_task_detail/task_sample_1",
                entityType = "TASK",
                entityId = "task_sample_1",
                createdAt = now - 45 * 60 * 1000L
            ),
            NotificationItem(
                notificationId = "n_3",
                recipientUserId = "ALL",
                recipientRole = "ALL",
                type = NotificationType.COMMUNITY,
                eventType = NotificationEventType.ACTIVITY_PUBLISHED,
                title = "Weekend Cleanup Drive",
                body = "SaafLoop Community Drive starts Sunday 8 AM in Phase 7 Market.",
                priority = NotificationPriority.NORMAL,
                deepLink = "saafloop://activity_detail/act_sample_1",
                entityType = "ACTIVITY",
                entityId = "act_sample_1",
                createdAt = now - 3 * 60 * 60 * 1000L
            ),
            NotificationItem(
                notificationId = "n_4",
                recipientUserId = "ALL",
                recipientRole = "COORDINATOR",
                type = NotificationType.AI,
                eventType = NotificationEventType.AI_INSIGHT_CREATED,
                title = "Hotspot Insight Available",
                body = "Sector 68 has 3 recurring waste reports in the last 30 days. Tap to inspect.",
                priority = NotificationPriority.LOW,
                deepLink = "saafloop://coordinator_dashboard",
                entityType = "AI",
                entityId = "ai_insight_68",
                createdAt = now - 12 * 60 * 60 * 1000L
            )
        )
    }
}
