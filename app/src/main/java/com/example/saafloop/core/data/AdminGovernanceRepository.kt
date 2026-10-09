package com.example.saafloop.core.data

import android.content.Context
import com.example.saafloop.core.model.CasePriority
import com.example.saafloop.core.model.GovernanceAppeal
import com.example.saafloop.core.model.GovernanceCase
import com.example.saafloop.core.model.GovernanceCaseCategory
import com.example.saafloop.core.model.GovernanceCaseStatus
import com.example.saafloop.core.model.OperationalEscalationItem
import com.example.saafloop.core.model.OrgVerificationReview
import com.example.saafloop.core.util.SecurityAuditLogger
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

interface AdminGovernanceRepository {
    fun observeGovernanceCases(statusFilter: GovernanceCaseStatus? = null): Flow<List<GovernanceCase>>
    fun observeGovernanceAppeals(): Flow<List<GovernanceAppeal>>
    fun observeOrgVerificationQueue(): Flow<List<OrgVerificationReview>>
    fun observeOperationalEscalations(): Flow<List<OperationalEscalationItem>>

    suspend fun createGovernanceCase(case: GovernanceCase): Result<String>
    suspend fun resolveGovernanceCase(caseId: String, newStatus: GovernanceCaseStatus, decisionReason: String, reviewerUid: String): Result<Unit>

    suspend fun submitAppeal(appeal: GovernanceAppeal): Result<String>
    suspend fun resolveAppeal(appealId: String, outcomeCode: String, decisionNotes: String, reviewerUid: String): Result<Unit>

    suspend fun updateOrgVerificationStatus(reviewId: String, newStatus: String, decisionNotes: String, reviewerUid: String): Result<Unit>
    suspend fun resolveEscalation(escalationId: String, notes: String, ownerUid: String): Result<Unit>
}

class AdminGovernanceRepositoryImpl(private val context: Context) : AdminGovernanceRepository {

    private fun isFirebaseConfigured(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty() && FirebaseAuth.getInstance().app != null
        } catch (_: Exception) {
            false
        }
    }

    override fun observeGovernanceCases(statusFilter: GovernanceCaseStatus?): Flow<List<GovernanceCase>> = callbackFlow {
        if (!isFirebaseConfigured()) {
            trySend(createSampleGovernanceCases())
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collection("governance_cases")
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(createSampleGovernanceCases())
                    return@addSnapshotListener
                }

                val cases = snapshot.documents.mapNotNull { doc ->
                    try {
                        val statusStr = doc.getString("status") ?: "OPEN"
                        val statusEnum = try { GovernanceCaseStatus.valueOf(statusStr) } catch (_: Exception) { GovernanceCaseStatus.OPEN }
                        if (statusFilter != null && statusEnum != statusFilter) return@mapNotNull null

                        GovernanceCase(
                            caseId = doc.getString("caseId") ?: doc.id,
                            category = try { GovernanceCaseCategory.valueOf(doc.getString("category") ?: "SUSPECTED_FALSE_REPORT") } catch (_: Exception) { GovernanceCaseCategory.SUSPECTED_FALSE_REPORT },
                            status = statusEnum,
                            priority = try { CasePriority.valueOf(doc.getString("priority") ?: "MEDIUM") } catch (_: Exception) { CasePriority.MEDIUM },
                            targetResourceType = doc.getString("targetResourceType") ?: "REPORT",
                            targetResourceId = doc.getString("targetResourceId") ?: "",
                            reporterUid = doc.getString("reporterUid") ?: "",
                            assignedReviewerUid = doc.getString("assignedReviewerUid") ?: "",
                            summary = doc.getString("summary") ?: "",
                            notes = doc.getString("notes") ?: "",
                            resolutionCode = doc.getString("resolutionCode") ?: "",
                            decisionReason = doc.getString("decisionReason") ?: "",
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                            updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                            resolvedAt = doc.getLong("resolvedAt")
                        )
                    } catch (_: Exception) {
                        null
                    }
                }

                trySend(if (cases.isEmpty()) createSampleGovernanceCases() else cases)
            }

        awaitClose { listener.remove() }
    }

    override fun observeGovernanceAppeals(): Flow<List<GovernanceAppeal>> = callbackFlow {
        if (!isFirebaseConfigured()) {
            trySend(createSampleAppeals())
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collection("governance_appeals")
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(createSampleAppeals())
                    return@addSnapshotListener
                }

                val appeals = snapshot.documents.mapNotNull { doc ->
                    try {
                        GovernanceAppeal(
                            appealId = doc.getString("appealId") ?: doc.id,
                            originalResourceId = doc.getString("originalResourceId") ?: "",
                            appellantUid = doc.getString("appellantUid") ?: "",
                            explanation = doc.getString("explanation") ?: "",
                            status = doc.getString("status") ?: "SUBMITTED",
                            assignedReviewerUid = doc.getString("assignedReviewerUid") ?: "",
                            outcomeCode = doc.getString("outcomeCode") ?: "",
                            decisionNotes = doc.getString("decisionNotes") ?: "",
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                            decidedAt = doc.getLong("decidedAt")
                        )
                    } catch (_: Exception) {
                        null
                    }
                }

                trySend(if (appeals.isEmpty()) createSampleAppeals() else appeals)
            }

        awaitClose { listener.remove() }
    }

    override fun observeOrgVerificationQueue(): Flow<List<OrgVerificationReview>> = callbackFlow {
        if (!isFirebaseConfigured()) {
            trySend(createSampleOrgReviews())
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collection("org_verification_reviews")
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(createSampleOrgReviews())
                    return@addSnapshotListener
                }

                val reviews = snapshot.documents.mapNotNull { doc ->
                    try {
                        OrgVerificationReview(
                            reviewId = doc.getString("reviewId") ?: doc.id,
                            orgId = doc.getString("orgId") ?: "",
                            orgName = doc.getString("orgName") ?: "",
                            orgType = doc.getString("orgType") ?: "NGO",
                            status = doc.getString("status") ?: "SUBMITTED",
                            assignedReviewerUid = doc.getString("assignedReviewerUid") ?: "",
                            decisionNotes = doc.getString("decisionNotes") ?: "",
                            submittedAt = doc.getLong("submittedAt") ?: System.currentTimeMillis(),
                            decidedAt = doc.getLong("decidedAt")
                        )
                    } catch (_: Exception) {
                        null
                    }
                }

                trySend(if (reviews.isEmpty()) createSampleOrgReviews() else reviews)
            }

        awaitClose { listener.remove() }
    }

    override fun observeOperationalEscalations(): Flow<List<OperationalEscalationItem>> = callbackFlow {
        if (!isFirebaseConfigured()) {
            trySend(createSampleEscalations())
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collection("operational_escalations")
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(createSampleEscalations())
                    return@addSnapshotListener
                }

                val items = snapshot.documents.mapNotNull { doc ->
                    try {
                        OperationalEscalationItem(
                            escalationId = doc.getString("escalationId") ?: doc.id,
                            triggerCode = doc.getString("triggerCode") ?: "SLA_BREACH_OVERDUE",
                            priority = try { CasePriority.valueOf(doc.getString("priority") ?: "HIGH") } catch (_: Exception) { CasePriority.HIGH },
                            targetResourceId = doc.getString("targetResourceId") ?: "",
                            status = doc.getString("status") ?: "OPEN",
                            assignedOwnerUid = doc.getString("assignedOwnerUid") ?: "",
                            notes = doc.getString("notes") ?: "",
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    } catch (_: Exception) {
                        null
                    }
                }

                trySend(if (items.isEmpty()) createSampleEscalations() else items)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun createGovernanceCase(case: GovernanceCase): Result<String> = withContext(Dispatchers.IO) {
        val caseId = "case_gov_${UUID.randomUUID()}"
        val newCase = case.copy(caseId = caseId)

        if (isFirebaseConfigured()) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("governance_cases")
                    .document(caseId)
                    .set(newCase)
                    .await()
            } catch (_: Exception) {}
        }

        Result.success(caseId)
    }

    override suspend fun resolveGovernanceCase(
        caseId: String,
        newStatus: GovernanceCaseStatus,
        decisionReason: String,
        reviewerUid: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (isFirebaseConfigured()) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val updates = mapOf(
                    "status" to newStatus.name,
                    "decisionReason" to decisionReason,
                    "assignedReviewerUid" to reviewerUid,
                    "updatedAt" to System.currentTimeMillis(),
                    "resolvedAt" to System.currentTimeMillis()
                )
                firestore.collection("governance_cases")
                    .document(caseId)
                    .update(updates)
                    .await()
            } catch (_: Exception) {}
        }

        SecurityAuditLogger.logEvent(
            context = context,
            eventType = "GOVERNANCE_CASE_RESOLVED",
            actorUid = reviewerUid,
            actorRole = "COORDINATOR",
            action = "Resolved case $caseId to ${newStatus.name}",
            outcome = "SUCCESS",
            targetResourceId = caseId,
            metadata = mapOf("decisionReason" to decisionReason)
        )

        Result.success(Unit)
    }

    override suspend fun submitAppeal(appeal: GovernanceAppeal): Result<String> = withContext(Dispatchers.IO) {
        val appealId = "appeal_${UUID.randomUUID()}"
        val newAppeal = appeal.copy(appealId = appealId)

        if (isFirebaseConfigured()) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("governance_appeals")
                    .document(appealId)
                    .set(newAppeal)
                    .await()
            } catch (_: Exception) {}
        }

        Result.success(appealId)
    }

    override suspend fun resolveAppeal(
        appealId: String,
        outcomeCode: String,
        decisionNotes: String,
        reviewerUid: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (isFirebaseConfigured()) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val updates = mapOf(
                    "status" to outcomeCode,
                    "outcomeCode" to outcomeCode,
                    "decisionNotes" to decisionNotes,
                    "assignedReviewerUid" to reviewerUid,
                    "decidedAt" to System.currentTimeMillis()
                )
                firestore.collection("governance_appeals")
                    .document(appealId)
                    .update(updates)
                    .await()
            } catch (_: Exception) {}
        }

        SecurityAuditLogger.logEvent(
            context = context,
            eventType = "APPEAL_DECIDED",
            actorUid = reviewerUid,
            actorRole = "PLATFORM_ADMIN",
            action = "Decided appeal $appealId -> $outcomeCode",
            outcome = "SUCCESS",
            targetResourceId = appealId,
            metadata = mapOf("notes" to decisionNotes)
        )

        Result.success(Unit)
    }

    override suspend fun updateOrgVerificationStatus(
        reviewId: String,
        newStatus: String,
        decisionNotes: String,
        reviewerUid: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (isFirebaseConfigured()) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val updates = mapOf(
                    "status" to newStatus,
                    "decisionNotes" to decisionNotes,
                    "assignedReviewerUid" to reviewerUid,
                    "decidedAt" to System.currentTimeMillis()
                )
                firestore.collection("org_verification_reviews")
                    .document(reviewId)
                    .update(updates)
                    .await()
            } catch (_: Exception) {}
        }

        SecurityAuditLogger.logEvent(
            context = context,
            eventType = "ORGANIZATION_VERIFICATION_DECIDED",
            actorUid = reviewerUid,
            actorRole = "PLATFORM_ADMIN",
            action = "Updated org review $reviewId to $newStatus",
            outcome = "SUCCESS",
            targetResourceId = reviewId
        )

        Result.success(Unit)
    }

    override suspend fun resolveEscalation(
        escalationId: String,
        notes: String,
        ownerUid: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (isFirebaseConfigured()) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val updates = mapOf(
                    "status" to "RESOLVED",
                    "notes" to notes,
                    "assignedOwnerUid" to ownerUid
                )
                firestore.collection("operational_escalations")
                    .document(escalationId)
                    .update(updates)
                    .await()
            } catch (_: Exception) {}
        }

        Result.success(Unit)
    }

    private fun createSampleGovernanceCases(): List<GovernanceCase> {
        val now = System.currentTimeMillis()
        return listOf(
            GovernanceCase(
                caseId = "case_gov_001",
                category = GovernanceCaseCategory.SUSPECTED_FALSE_REPORT,
                status = GovernanceCaseStatus.UNDER_REVIEW,
                priority = CasePriority.HIGH,
                targetResourceType = "REPORT",
                targetResourceId = "case_702",
                reporterUid = "user_resident_12",
                assignedReviewerUid = "coord_01",
                summary = "Report flagged for potential duplicate photo reuse in Sector 68.",
                notes = "Initial AI similarity score 88% matching case_700.",
                createdAt = now - 2 * 3600000L
            ),
            GovernanceCase(
                caseId = "case_gov_002",
                category = GovernanceCaseCategory.TASK_DISPUTE,
                status = GovernanceCaseStatus.OPEN,
                priority = CasePriority.MEDIUM,
                targetResourceType = "TASK",
                targetResourceId = "task_991",
                reporterUid = "worker_field_04",
                summary = "Field worker disputes task rejection regarding drainage clearance.",
                createdAt = now - 12 * 3600000L
            ),
            GovernanceCase(
                caseId = "case_gov_003",
                category = GovernanceCaseCategory.UNSAFE_CLEANUP_ACTIVITY,
                status = GovernanceCaseStatus.ACTION_REQUIRED,
                priority = CasePriority.CRITICAL,
                targetResourceType = "ACTIVITY",
                targetResourceId = "act_404",
                reporterUid = "volunteer_88",
                summary = "Heavy construction debris cleanup drive requested safety gloves & equipment.",
                createdAt = now - 1 * 3600000L
            )
        )
    }

    private fun createSampleAppeals(): List<GovernanceAppeal> {
        val now = System.currentTimeMillis()
        return listOf(
            GovernanceAppeal(
                appealId = "appeal_101",
                originalResourceId = "case_605",
                appellantUid = "resident_mohali_09",
                explanation = "My report for overflowing bin in Sector 70 was incorrectly marked as duplicate.",
                status = "SUBMITTED",
                createdAt = now - 5 * 3600000L
            ),
            GovernanceAppeal(
                appealId = "appeal_102",
                originalResourceId = "task_882",
                appellantUid = "worker_vol_02",
                explanation = "Before/After completion photo clearly shows plastic waste was removed and bagged.",
                status = "UNDER_REVIEW",
                createdAt = now - 24 * 3600000L
            )
        )
    }

    private fun createSampleOrgReviews(): List<OrgVerificationReview> {
        val now = System.currentTimeMillis()
        return listOf(
            OrgVerificationReview(
                reviewId = "org_rev_01",
                orgId = "org_clean_mohali",
                orgName = "Clean Mohali Youth Foundation",
                orgType = "Civic NGO",
                status = "SUBMITTED",
                submittedAt = now - 48 * 3600000L
            ),
            OrgVerificationReview(
                reviewId = "org_rev_02",
                orgId = "org_green_punjab",
                orgName = "Green Punjab Environmental Society",
                orgType = "Environmental Trust",
                status = "UNDER_REVIEW",
                submittedAt = now - 72 * 3600000L
            )
        )
    }

    private fun createSampleEscalations(): List<OperationalEscalationItem> {
        val now = System.currentTimeMillis()
        return listOf(
            OperationalEscalationItem(
                escalationId = "esc_301",
                triggerCode = "SLA_BREACH_OVERDUE",
                priority = CasePriority.HIGH,
                targetResourceId = "task_771",
                status = "OPEN",
                notes = "Field task in Sector 68 exceeded 24h resolution SLA.",
                createdAt = now - 3 * 3600000L
            )
        )
    }
}
