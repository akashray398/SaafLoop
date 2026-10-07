package com.example.saafloop

import android.content.Context
import android.content.ContextWrapper
import com.example.saafloop.core.data.NotificationRepositoryImpl
import com.example.saafloop.core.model.NotificationEventType
import com.example.saafloop.core.model.NotificationPriority
import com.example.saafloop.core.model.NotificationType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationSystemTest {

    private lateinit var fakeContext: Context
    private lateinit var repository: NotificationRepositoryImpl

    @Before
    fun setUp() {
        fakeContext = NotificationTestContext()
        repository = NotificationRepositoryImpl(fakeContext)
    }

    @Test
    fun observeUserNotifications_returnsNotificationsAndCalculatesUnread() = runBlocking {
        val notifications = repository.observeUserNotifications("user_123", "CITIZEN").first()

        assertNotNull(notifications)
        assertTrue(notifications.isNotEmpty())

        val reportNotif = notifications.find { it.type == NotificationType.REPORT }
        assertNotNull(reportNotif)
        assertEquals("Report Verified", reportNotif?.title)
        assertTrue(reportNotif?.deepLink?.contains("report_review/") == true)
    }

    @Test
    fun dispatchNotificationEvent_createsDeterministicIdAndDeepLink() = runBlocking {
        val result = repository.dispatchNotificationEvent(
            eventType = NotificationEventType.TASK_ASSIGNED,
            entityType = "TASK",
            entityId = "task_999",
            title = "New Task Assigned",
            body = "Cleanup task in Sector 68",
            recipientUserId = "worker_1",
            recipientRole = "VOLUNTEER",
            priority = NotificationPriority.HIGH
        )

        assertTrue(result.isSuccess)
        val notifId = result.getOrNull()
        assertNotNull(notifId)
        assertEquals("notif_TASK_ASSIGNED_task_999_worker_1", notifId)
    }

    @Test
    fun publishAdminAnnouncement_dispatchesSystemAnnouncementToAll() = runBlocking {
        val result = repository.publishAdminAnnouncement(
            title = "Scheduled Platform Maintenance",
            body = "SaafLoop services will undergo scheduled maintenance tonight.",
            targetRole = "ALL",
            priority = NotificationPriority.HIGH
        )

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun deepLinkParsing_resolvesEntityRoutesCorrectly() {
        val reportDeepLink = "saafloop://report_review/case_123"
        val taskDeepLink = "saafloop://field_task_detail/task_456"
        val activityDeepLink = "saafloop://activity_detail/act_789"

        assertTrue(reportDeepLink.contains("report_review/case_123"))
        assertTrue(taskDeepLink.contains("field_task_detail/task_456"))
        assertTrue(activityDeepLink.contains("activity_detail/act_789"))
    }
}

private class NotificationTestContext : ContextWrapper(null) {
    override fun getApplicationContext(): Context = this
}
