package com.example.saafloop.core.data

import android.content.Context
import android.net.Uri
import com.example.saafloop.core.database.entity.ReportDraftEntity
import com.example.saafloop.core.model.CaseReport
import com.example.saafloop.core.model.CaseStatus
import com.example.saafloop.core.util.PhotoMetadataUtils
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

interface RemoteCaseRepository {
    fun isFirebaseConfigured(): Boolean
    suspend fun submitCase(draft: ReportDraftEntity, previousCaseId: String? = null): Result<String>
    suspend fun confirmExistingCase(caseId: String, draft: ReportDraftEntity): Result<String>
    fun observeUserCases(uid: String): Flow<List<CaseReport>>
    fun observePublicCases(): Flow<List<CaseReport>>
}

class RemoteCaseRepositoryImpl(private val context: Context) : RemoteCaseRepository {

    override fun isFirebaseConfigured(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty() && FirebaseFirestore.getInstance().app != null
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun submitCase(
        draft: ReportDraftEntity,
        previousCaseId: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) {
            return@withContext Result.failure(
                IllegalStateException("Firebase backend is unconfigured in local environment. Please see firebase_setup_guide.artifact.md")
            )
        }

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
            ?: return@withContext Result.failure(IllegalStateException("Must be signed in to submit a public report"))

        try {
            val firestore = FirebaseFirestore.getInstance()
            val storage = FirebaseStorage.getInstance()

            // 1. Idempotency Check: Prevent duplicate case creation on network retry or timeout
            if (draft.idempotencyKey.isNotBlank()) {
                val existingQuery = firestore.collection("cases")
                    .whereEqualTo("authorUid", currentUser.uid)
                    .whereEqualTo("idempotencyKey", draft.idempotencyKey)
                    .get()
                    .await()

                if (!existingQuery.isEmpty) {
                    val existingDoc = existingQuery.documents.first()
                    val existingCaseId = existingDoc.getString("caseId") ?: existingDoc.id
                    return@withContext Result.success(existingCaseId)
                }
            }

            val caseId = "case_${UUID.randomUUID()}"
            var remoteStoragePath: String? = null

            // 2. Upload photo if present (with EXIF stripping)
            val photoPath = draft.photoPath
            if (!photoPath.isNullOrBlank()) {
                val rawFile = File(photoPath)
                if (rawFile.exists()) {
                    val stripResult = PhotoMetadataUtils.stripExifAndCompress(context, rawFile)
                    val cleanFile = stripResult.getOrDefault(rawFile)

                    val storageRef = storage.reference.child("cases/$caseId/photo.jpg")
                    storageRef.putFile(Uri.fromFile(cleanFile)).await()
                    remoteStoragePath = storageRef.path
                }
            }

            // 3. Write case document to Firestore /cases/{caseId}
            val initialTimeline = listOf(
                hashMapOf(
                    "status" to CaseStatus.SUBMITTED.name,
                    "message" to if (!previousCaseId.isNullOrBlank()) "New occurrence reported at previously cleaned site" else "Report submitted by resident",
                    "timestamp" to System.currentTimeMillis()
                )
            )

            val caseData = hashMapOf(
                "caseId" to caseId,
                "authorUid" to currentUser.uid,
                "category" to (draft.category ?: "OTHER_UNSURE"),
                "sizeEstimate" to (draft.sizeEstimate ?: "SMALL"),
                "description" to draft.description,
                "accessNote" to draft.accessNote,
                "approximateArea" to draft.locationName,
                "latitude" to draft.latitude,
                "longitude" to draft.longitude,
                "status" to CaseStatus.SUBMITTED.name,
                "assignedOrgId" to null,
                "isHazardousSuspected" to draft.isHazardousSuspected,
                "photoStoragePath" to remoteStoragePath,
                "idempotencyKey" to draft.idempotencyKey,
                "previousCaseId" to previousCaseId,
                "confirmationsCount" to 1,
                "timeline" to initialTimeline,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection("cases")
                .document(caseId)
                .set(caseData)
                .await()

            Result.success(caseId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun confirmExistingCase(
        caseId: String,
        draft: ReportDraftEntity
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) {
            return@withContext Result.failure(
                IllegalStateException("Firebase backend is unconfigured in local environment")
            )
        }

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
            ?: return@withContext Result.failure(IllegalStateException("Must be signed in to confirm an existing case"))

        try {
            val firestore = FirebaseFirestore.getInstance()
            val confirmationId = "conf_${currentUser.uid}_$caseId"

            val confRef = firestore.collection("cases")
                .document(caseId)
                .collection("confirmations")
                .document(confirmationId)

            val existingConf = confRef.get().await()
            if (!existingConf.exists()) {
                val confData = hashMapOf(
                    "confirmationId" to confirmationId,
                    "caseId" to caseId,
                    "authorUid" to currentUser.uid,
                    "idempotencyKey" to draft.idempotencyKey,
                    "createdAt" to System.currentTimeMillis()
                )
                confRef.set(confData).await()

                // Increment confirmations count on parent case
                firestore.collection("cases")
                    .document(caseId)
                    .update("confirmationsCount", FieldValue.increment(1))
                    .await()
            }

            Result.success(caseId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeUserCases(uid: String): Flow<List<CaseReport>> = callbackFlow {
        if (!isFirebaseConfigured() || uid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collection("cases")
            .whereEqualTo("authorUid", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val cases = snapshot.documents.mapNotNull { doc ->
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
                }
                trySend(cases)
            }

        awaitClose { listener.remove() }
    }

    override fun observePublicCases(): Flow<List<CaseReport>> = callbackFlow {
        if (!isFirebaseConfigured()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collection("cases")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val cases = snapshot.documents.mapNotNull { doc ->
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
                }
                trySend(cases)
            }

        awaitClose { listener.remove() }
    }
}
