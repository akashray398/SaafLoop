package com.example.saafloop.core.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.saafloop.MainActivity
import com.example.saafloop.R
import com.example.saafloop.core.model.NotificationItem
import com.example.saafloop.core.model.NotificationPriority

object NotificationChannelManager {

    const val CHANNEL_REPORT_UPDATES = "channel_report_updates"
    const val CHANNEL_TASK_OPERATIONS = "channel_task_operations"
    const val CHANNEL_COMMUNITY_ACTIVITIES = "channel_community_activities"
    const val CHANNEL_SYSTEM_ALERTS = "channel_system_alerts"
    const val CHANNEL_AI_INSIGHTS = "channel_ai_insights"

    /**
     * Initializes Android System Notification Channels on Android 8.0 (API 26+).
     */
    fun createNotificationChannels(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return

                val reportChannel = NotificationChannel(
                    CHANNEL_REPORT_UPDATES,
                    "Report Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Updates on public waste reports and verification status"
                }

                val taskChannel = NotificationChannel(
                    CHANNEL_TASK_OPERATIONS,
                    "Task Operations",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts for field task assignments, completion reviews, and deadlines"
                }

                val communityChannel = NotificationChannel(
                    CHANNEL_COMMUNITY_ACTIVITIES,
                    "Community Activities",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Updates and reminders for local community cleanup drives"
                }

                val systemChannel = NotificationChannel(
                    CHANNEL_SYSTEM_ALERTS,
                    "System & Security Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Important operational announcements and security alerts"
                }

                val aiChannel = NotificationChannel(
                    CHANNEL_AI_INSIGHTS,
                    "AI Civic Insights",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Smart report analysis and hotspot intelligence alerts"
                }

                manager.createNotificationChannels(
                    listOf(reportChannel, taskChannel, communityChannel, systemChannel, aiChannel)
                )
            }
        } catch (_: Exception) {
            // Safe fallback for JVM unit tests or unmocked Android framework classes
        }
    }

    /**
     * Posts a local Android system notification for an event.
     */
    @SuppressLint("MissingPermission")
    fun postSystemNotification(context: Context, item: NotificationItem) {
        try {
            createNotificationChannels(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                if (item.deepLink.isNotBlank()) {
                    data = Uri.parse(item.deepLink)
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                item.notificationId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val importance = when (item.priority) {
                NotificationPriority.CRITICAL, NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
                NotificationPriority.NORMAL -> NotificationCompat.PRIORITY_DEFAULT
                NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
            }

            val builder = NotificationCompat.Builder(context, item.channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(item.title)
                .setContentText(item.body)
                .setPriority(importance)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(item.notificationId.hashCode(), builder.build())
        } catch (_: Exception) {
            // Safe fallback for JVM unit tests or unmocked Android framework classes
        }
    }
}
