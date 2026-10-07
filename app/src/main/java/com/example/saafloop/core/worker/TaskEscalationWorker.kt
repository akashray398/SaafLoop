package com.example.saafloop.core.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.saafloop.core.data.NotificationRepository
import com.example.saafloop.core.data.NotificationRepositoryImpl
import com.example.saafloop.core.model.NotificationEventType
import com.example.saafloop.core.model.NotificationPriority
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TaskEscalationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl(context)

    override suspend fun doWork(): Result {
        val isFirebaseReady = try {
            FirebaseApp.getApps(applicationContext).isNotEmpty() && FirebaseFirestore.getInstance().app != null
        } catch (_: Exception) {
            false
        }

        if (!isFirebaseReady) return Result.success()

        try {
            val firestore = FirebaseFirestore.getInstance()
            val now = System.currentTimeMillis()

            val snapshot = firestore.collection("tasks")
                .whereIn("status", listOf("ASSIGNED", "IN_PROGRESS"))
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                val taskId = doc.getString("taskId") ?: doc.id
                val title = doc.getString("title") ?: "Field Cleanup Task"
                val dueAt = doc.getLong("dueAt")
                val assignedToUid = doc.getString("assignedToUid") ?: "ALL"

                if (dueAt != null && now > dueAt) {
                    notificationRepository.dispatchNotificationEvent(
                        eventType = NotificationEventType.TASK_OVERDUE,
                        entityType = "TASK",
                        entityId = taskId,
                        title = "Task Overdue Escalation",
                        body = "Task '$title' has exceeded its resolution target. Action required.",
                        recipientUserId = assignedToUid,
                        recipientRole = "FIELD_WORKER",
                        priority = NotificationPriority.HIGH
                    )

                    // Also notify coordinators
                    notificationRepository.dispatchNotificationEvent(
                        eventType = NotificationEventType.TASK_OVERDUE,
                        entityType = "TASK",
                        entityId = taskId,
                        title = "Overdue Field Task Alert",
                        body = "Task '$title' assigned to worker is overdue.",
                        recipientUserId = "ALL",
                        recipientRole = "COORDINATOR",
                        priority = NotificationPriority.HIGH
                    )
                }
            }

            return Result.success()
        } catch (_: Exception) {
            return Result.retry()
        }
    }

    companion object {
        const val WORK_TAG = "TaskEscalationWorker"
    }
}
