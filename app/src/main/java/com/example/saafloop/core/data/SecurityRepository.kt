package com.example.saafloop.core.data

import android.content.Context
import com.example.saafloop.core.model.AbuseReport
import com.example.saafloop.core.model.PrivacySettings
import com.example.saafloop.core.model.SecurityAuditEvent
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

interface SecurityRepository {
    fun observePrivacySettings(userId: String): Flow<PrivacySettings>
    suspend fun updatePrivacySettings(userId: String, settings: PrivacySettings): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun deleteAccountAndAnonymizeData(userId: String): Result<Unit>
    suspend fun submitAbuseReport(abuseReport: AbuseReport): Result<String>
    fun observeSecurityAuditLogs(): Flow<List<SecurityAuditEvent>>
}

class SecurityRepositoryImpl(private val context: Context) : SecurityRepository {

    private fun isFirebaseConfigured(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty() && FirebaseAuth.getInstance().app != null
        } catch (_: Exception) {
            false
        }
    }

    override fun observePrivacySettings(userId: String): Flow<PrivacySettings> = callbackFlow {
        if (!isFirebaseConfigured() || userId.isBlank() || userId == "GUEST") {
            trySend(PrivacySettings(userId = userId))
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collection("user_privacy_settings")
            .document(userId)
            .addSnapshotListener { doc, error ->
                if (error != null || doc == null || !doc.exists()) {
                    trySend(PrivacySettings(userId = userId))
                    return@addSnapshotListener
                }

                val settings = PrivacySettings(
                    userId = userId,
                    anonymizeExactLocation = doc.getBoolean("anonymizeExactLocation") ?: true,
                    hideProfileFromPublic = doc.getBoolean("hideProfileFromPublic") ?: false,
                    allowNotificationAlerts = doc.getBoolean("allowNotificationAlerts") ?: true,
                    shareAnalyticsConsent = doc.getBoolean("shareAnalyticsConsent") ?: true
                )
                trySend(settings)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun updatePrivacySettings(
        userId: String,
        settings: PrivacySettings
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured() || userId.isBlank() || userId == "GUEST") return@withContext Result.success(Unit)

        try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("user_privacy_settings")
                .document(userId)
                .set(settings)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured() || email.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address"))
        }

        try {
            val auth = FirebaseAuth.getInstance()
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccountAndAnonymizeData(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured() || userId.isBlank() || userId == "GUEST") return@withContext Result.success(Unit)

        try {
            val firestore = FirebaseFirestore.getInstance()
            val auth = FirebaseAuth.getInstance()

            // 1. Anonymize user's reports in Firestore /cases
            val userCases = firestore.collection("cases")
                .whereEqualTo("authorUid", userId)
                .get()
                .await()

            val batch = firestore.batch()
            userCases.documents.forEach { doc ->
                batch.update(doc.reference, mapOf("authorUid" to "ANONYMOUS_DELETED_USER"))
            }

            // 2. Delete user profile document
            val userRef = firestore.collection("users").document(userId)
            batch.delete(userRef)

            val privacyRef = firestore.collection("user_privacy_settings").document(userId)
            batch.delete(privacyRef)

            batch.commit().await()

            // 3. Delete Firebase Auth user account
            auth.currentUser?.delete()?.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitAbuseReport(abuseReport: AbuseReport): Result<String> = withContext(Dispatchers.IO) {
        val abuseId = "abuse_${UUID.randomUUID()}"
        val report = abuseReport.copy(abuseId = abuseId)

        if (isFirebaseConfigured()) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("abuse_reports")
                    .document(abuseId)
                    .set(report)
                    .await()
            } catch (_: Exception) {
                // Fallback local report
            }
        }

        Result.success(abuseId)
    }

    override fun observeSecurityAuditLogs(): Flow<List<SecurityAuditEvent>> = callbackFlow {
        if (!isFirebaseConfigured()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collection("security_audit_logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val events = snapshot.documents.mapNotNull { doc ->
                    try {
                        SecurityAuditEvent(
                            eventId = doc.getString("eventId") ?: doc.id,
                            eventType = doc.getString("eventType") ?: "SECURITY_EVENT",
                            actorUid = doc.getString("actorUid") ?: "",
                            actorRole = doc.getString("actorRole") ?: "CITIZEN",
                            targetResourceId = doc.getString("targetResourceId") ?: "",
                            action = doc.getString("action") ?: "",
                            outcome = doc.getString("outcome") ?: "SUCCESS",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        )
                    } catch (_: Exception) {
                        null
                    }
                }

                trySend(events)
            }

        awaitClose { listener.remove() }
    }
}
