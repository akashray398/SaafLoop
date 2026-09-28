package com.example.saafloop.core.data

import android.content.Context
import com.example.saafloop.core.model.AuditEvent
import com.example.saafloop.core.model.CasePriority
import com.example.saafloop.core.model.CaseReport
import com.example.saafloop.core.model.CaseStatus
import com.example.saafloop.core.model.CoordinatorMetrics
import com.example.saafloop.core.model.ReviewFilter
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

interface CoordinatorRepository {
    fun isFirebaseConfigured(): Boolean
    fun observeReviewQueue(filter: ReviewFilter): Flow<List<CaseReport>>
    fun observeReportDetails(caseId: String): Flow<CaseReport?>
    fun observeAuditHistory(caseId: String): Flow<List<AuditEvent>>
    fun observeCoordinatorMetrics(): Flow<CoordinatorMetrics>
    suspend fun startReportReview(caseId: String, coordinatorUid: String, coordinatorName: String): Result<Unit>
    suspend fun verifyReport(caseId: String, category: String, severity: String, priority: String, notes: String, coordinatorUid: String, coordinatorName: String): Result<Unit>
    suspend fun rejectReport(caseId: String, reason: String, notes: String, coordinatorUid: String, coordinatorName: String): Result<Unit>
    suspend fun requestMoreInfo(caseId: String, reason: String, message: String, coordinatorUid: String, coordinatorName: String): Result<Unit>
    suspend fun mergeReports(primaryCaseId: String, duplicateCaseIds: List<String>, coordinatorUid: String, coordinatorName: String): Result<Unit>
}

class CoordinatorRepositoryImpl(private val context: Context) : CoordinatorRepository {

    override fun isFirebaseConfigured(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty() && FirebaseFirestore.getInstance().app != null
        } catch (_: Exception) {
            false
        }
    }

    override fun observeReviewQueue(filter: ReviewFilter): Flow<List<CaseReport>> = callbackFlow {
        if (!isFirebaseConfigured()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val query: Query = firestore.collection("cases")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }

            val reports = snapshot.documents.mapNotNull { doc ->
                try {
                    CaseReport(
                        caseId = doc.getString("caseId") ?: doc.id,
                        authorUid = doc.getString("authorUid") ?: "",
                        category = doc.getString("category") ?: "OTHER_UNSURE",
                        sizeEstimate = doc.getString("sizeEstimate"),
                        description = doc.getString("description") ?: "",
                        accessNote = doc.getString("accessNote") ?: "",
                        approximateArea = doc.getString("approximateArea") ?: "",
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        status = try { CaseStatus.valueOf(doc.getString("status") ?: "SUBMITTED") } catch (_: Exception) { CaseStatus.SUBMITTED },
                        assignedOrgId = doc.getString("assignedOrgId"),
                        isHazardousSuspected = doc.getBoolean("isHazardousSuspected") ?: false,
                        photoStoragePath = doc.getString("photoStoragePath"),
                        idempotencyKey = doc.getString("idempotencyKey") ?: "",
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                    )
                } catch (_: Exception) {
                    null
                }
            }.filter { case ->
                val matchesStatus = filter.status == null || case.status == filter.status
                val matchesCategory = filter.category == null || case.category.equals(filter.category, ignoreCase = true)
                val matchesSearch = filter.searchQuery.isBlank() ||
                        case.caseId.contains(filter.searchQuery, ignoreCase = true) ||
                        case.approximateArea.contains(filter.searchQuery, ignoreCase = true) ||
                        case.description.contains(filter.searchQuery, ignoreCase = true)
                matchesStatus && matchesCategory && matchesSearch
            }

            trySend(reports)
        }

        awaitClose { listener.remove() }
    }

    override fun observeReportDetails(caseId: String): Flow<CaseReport?> = callbackFlow {
        if (!isFirebaseConfigured() || caseId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collection("cases").document(caseId)
            .addSnapshotListener { doc, error ->
                if (error != null || doc == null || !doc.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val caseReport = try {
                    CaseReport(
                        caseId = doc.getString("caseId") ?: doc.id,
                        authorUid = doc.getString("authorUid") ?: "",
                        category = doc.getString("category") ?: "OTHER_UNSURE",
                        sizeEstimate = doc.getString("sizeEstimate"),
                        description = doc.getString("description") ?: "",
                        accessNote = doc.getString("accessNote") ?: "",
                        approximateArea = doc.getString("approximateArea") ?: "",
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        status = try { CaseStatus.valueOf(doc.getString("status") ?: "SUBMITTED") } catch (_: Exception) { CaseStatus.SUBMITTED },
                        assignedOrgId = doc.getString("assignedOrgId"),
                        isHazardousSuspected = doc.getBoolean("isHazardousSuspected") ?: false,
                        photoStoragePath = doc.getString("photoStoragePath"),
                        idempotencyKey = doc.getString("idempotencyKey") ?: "",
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                    )
                } catch (_: Exception) {
                    null
                }

                trySend(caseReport)
            }

        awaitClose { listener.remove() }
    }

    override fun observeAuditHistory(caseId: String): Flow<List<AuditEvent>> = callbackFlow {
        if (!isFirebaseConfigured() || caseId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collection("cases")
            .document(caseId)
            .collection("audit_events")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val events = snapshot.documents.mapNotNull { doc ->
                    try {
                        AuditEvent(
                            eventId = doc.getString("eventId") ?: doc.id,
                            caseId = doc.getString("caseId") ?: caseId,
                            actorUid = doc.getString("actorUid") ?: "",
                            actorName = doc.getString("actorName") ?: "Coordinator",
                            actorRole = doc.getString("actorRole") ?: "COORDINATOR",
                            action = doc.getString("action") ?: "",
                            previousStatus = doc.getString("previousStatus"),
                            newStatus = doc.getString("newStatus"),
                            notes = doc.getString("notes"),
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

    override fun observeCoordinatorMetrics(): Flow<CoordinatorMetrics> = callbackFlow {
        if (!isFirebaseConfigured()) {
            trySend(CoordinatorMetrics())
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collection("cases")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(CoordinatorMetrics())
                    return@addSnapshotListener
                }

                var newCount = 0
                var underReview = 0
                var highPriority = 0
                var needsInfo = 0
                var verified = 0
                var escalated = 0

                snapshot.documents.forEach { doc ->
                    val statusStr = doc.getString("status") ?: "SUBMITTED"
                    when (statusStr) {
                        CaseStatus.SUBMITTED.name -> newCount++
                        CaseStatus.UNDER_REVIEW.name -> underReview++
                        CaseStatus.NEEDS_INFORMATION.name -> needsInfo++
                        CaseStatus.VERIFIED_CLEAN.name, "VERIFIED" -> verified++
                        CaseStatus.REJECTED.name, "DUPLICATE", "ESCALATED" -> escalated++
                    }
                }

                trySend(
                    CoordinatorMetrics(
                        newReportsCount = newCount,
                        underReviewCount = underReview,
                        highPriorityCount = highPriority,
                        needsInfoCount = needsInfo,
                        verifiedCount = verified,
                        escalatedCount = escalated,
                        averageReviewTimeMinutes = 14
                    )
                )
            }

        awaitClose { listener.remove() }
    }

    override suspend fun startReportReview(
        caseId: String,
        coordinatorUid: String,
        coordinatorName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.failure(IllegalStateException("Firebase unconfigured"))

        try {
            val firestore = FirebaseFirestore.getInstance()
            val caseRef = firestore.collection("cases").document(caseId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(caseRef)
                val currentStatus = snapshot.getString("status") ?: CaseStatus.SUBMITTED.name

                if (currentStatus == CaseStatus.SUBMITTED.name) {
                    transaction.update(caseRef, "status", CaseStatus.UNDER_REVIEW.name)
                    transaction.update(caseRef, "reviewedBy", coordinatorName)
                    transaction.update(caseRef, "updatedAt", System.currentTimeMillis())

                    val auditRef = caseRef.collection("audit_events").document()
                    val auditData = hashMapOf(
                        "eventId" to auditRef.id,
                        "caseId" to caseId,
                        "actorUid" to coordinatorUid,
                        "actorName" to coordinatorName,
                        "actorRole" to "COORDINATOR",
                        "action" to "MOVED_TO_UNDER_REVIEW",
                        "previousStatus" to currentStatus,
                        "newStatus" to CaseStatus.UNDER_REVIEW.name,
                        "notes" to "Coordinator started review",
                        "timestamp" to System.currentTimeMillis()
                    )
                    transaction.set(auditRef, auditData)
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyReport(
        caseId: String,
        category: String,
        severity: String,
        priority: String,
        notes: String,
        coordinatorUid: String,
        coordinatorName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.failure(IllegalStateException("Firebase unconfigured"))

        try {
            val firestore = FirebaseFirestore.getInstance()
            val caseRef = firestore.collection("cases").document(caseId)

            val updates = hashMapOf<String, Any>(
                "status" to "VERIFIED",
                "category" to category,
                "severity" to severity,
                "priority" to priority,
                "verificationNotes" to notes,
                "reviewedBy" to coordinatorName,
                "updatedAt" to System.currentTimeMillis()
            )

            caseRef.update(updates).await()

            val auditRef = caseRef.collection("audit_events").document()
            val auditData = hashMapOf(
                "eventId" to auditRef.id,
                "caseId" to caseId,
                "actorUid" to coordinatorUid,
                "actorName" to coordinatorName,
                "actorRole" to "COORDINATOR",
                "action" to "REPORT_VERIFIED",
                "previousStatus" to CaseStatus.UNDER_REVIEW.name,
                "newStatus" to "VERIFIED",
                "notes" to notes,
                "timestamp" to System.currentTimeMillis()
            )
            auditRef.set(auditData).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectReport(
        caseId: String,
        reason: String,
        notes: String,
        coordinatorUid: String,
        coordinatorName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.failure(IllegalStateException("Firebase unconfigured"))

        try {
            val firestore = FirebaseFirestore.getInstance()
            val caseRef = firestore.collection("cases").document(caseId)

            val updates = hashMapOf<String, Any>(
                "status" to CaseStatus.REJECTED.name,
                "rejectionReason" to reason,
                "rejectionNotes" to notes,
                "reviewedBy" to coordinatorName,
                "updatedAt" to System.currentTimeMillis()
            )

            caseRef.update(updates).await()

            val auditRef = caseRef.collection("audit_events").document()
            val auditData = hashMapOf(
                "eventId" to auditRef.id,
                "caseId" to caseId,
                "actorUid" to coordinatorUid,
                "actorName" to coordinatorName,
                "actorRole" to "COORDINATOR",
                "action" to "REPORT_REJECTED",
                "previousStatus" to CaseStatus.UNDER_REVIEW.name,
                "newStatus" to CaseStatus.REJECTED.name,
                "notes" to "Reason: $reason. $notes",
                "timestamp" to System.currentTimeMillis()
            )
            auditRef.set(auditData).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestMoreInfo(
        caseId: String,
        reason: String,
        message: String,
        coordinatorUid: String,
        coordinatorName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.failure(IllegalStateException("Firebase unconfigured"))

        try {
            val firestore = FirebaseFirestore.getInstance()
            val caseRef = firestore.collection("cases").document(caseId)

            val updates = hashMapOf<String, Any>(
                "status" to CaseStatus.NEEDS_INFORMATION.name,
                "requestInfoReason" to reason,
                "requestInfoMessage" to message,
                "updatedAt" to System.currentTimeMillis()
            )

            caseRef.update(updates).await()

            val auditRef = caseRef.collection("audit_events").document()
            val auditData = hashMapOf(
                "eventId" to auditRef.id,
                "caseId" to caseId,
                "actorUid" to coordinatorUid,
                "actorName" to coordinatorName,
                "actorRole" to "COORDINATOR",
                "action" to "REQUESTED_MORE_INFO",
                "previousStatus" to CaseStatus.UNDER_REVIEW.name,
                "newStatus" to CaseStatus.NEEDS_INFORMATION.name,
                "notes" to "$reason: $message",
                "timestamp" to System.currentTimeMillis()
            )
            auditRef.set(auditData).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun mergeReports(
        primaryCaseId: String,
        duplicateCaseIds: List<String>,
        coordinatorUid: String,
        coordinatorName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.failure(IllegalStateException("Firebase unconfigured"))

        try {
            val firestore = FirebaseFirestore.getInstance()

            duplicateCaseIds.forEach { dupId ->
                val dupRef = firestore.collection("cases").document(dupId)
                val dupUpdates = hashMapOf<String, Any>(
                    "status" to "DUPLICATE",
                    "parentReportId" to primaryCaseId,
                    "updatedAt" to System.currentTimeMillis()
                )
                dupRef.update(dupUpdates).await()

                val auditRef = dupRef.collection("audit_events").document()
                val auditData = hashMapOf(
                    "eventId" to auditRef.id,
                    "caseId" to dupId,
                    "actorUid" to coordinatorUid,
                    "actorName" to coordinatorName,
                    "actorRole" to "COORDINATOR",
                    "action" to "MERGED_AS_DUPLICATE",
                    "previousStatus" to CaseStatus.UNDER_REVIEW.name,
                    "newStatus" to "DUPLICATE",
                    "notes" to "Merged into primary case #$primaryCaseId",
                    "timestamp" to System.currentTimeMillis()
                )
                auditRef.set(auditData).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
