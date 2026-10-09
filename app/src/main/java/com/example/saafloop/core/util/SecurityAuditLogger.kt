package com.example.saafloop.core.util

import android.content.Context
import com.example.saafloop.core.model.SecurityAuditEvent
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

object SecurityAuditLogger {

    suspend fun logEvent(
        context: Context,
        eventType: String,
        actorUid: String,
        actorRole: String,
        action: String,
        outcome: String,
        targetResourceId: String = "",
        metadata: Map<String, String> = emptyMap()
    ) = withContext(Dispatchers.IO) {
        val eventId = "audit_${UUID.randomUUID()}"

        // Filter out sensitive key-value pairs from metadata
        val sanitizedMetadata = metadata.filterKeys { key ->
            !key.contains("password", ignoreCase = true) &&
                    !key.contains("token", ignoreCase = true) &&
                    !key.contains("coordinate", ignoreCase = true)
        }

        val auditEvent = SecurityAuditEvent(
            eventId = eventId,
            eventType = eventType,
            actorUid = actorUid,
            actorRole = actorRole,
            targetResourceId = targetResourceId,
            action = action,
            outcome = outcome,
            timestamp = System.currentTimeMillis(),
            metadata = sanitizedMetadata
        )

        val isFirebaseReady = try {
            FirebaseApp.getApps(context).isNotEmpty() && FirebaseFirestore.getInstance().app != null
        } catch (_: Exception) {
            false
        }

        if (isFirebaseReady) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("security_audit_logs")
                    .document(eventId)
                    .set(auditEvent)
            } catch (_: Exception) {
                // Safe audit logger fallback
            }
        }
    }
}
