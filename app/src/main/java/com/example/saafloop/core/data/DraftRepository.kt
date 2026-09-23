package com.example.saafloop.core.data

import android.content.Context
import com.example.saafloop.core.database.SaafLoopDatabase
import com.example.saafloop.core.database.entity.ReportDraftEntity
import com.example.saafloop.core.database.model.DraftStatus
import com.example.saafloop.feature.report.model.ReportFormState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Public repository boundary for managing local report drafts and offline upload preparation.
 *
 * NOTE FOR SECTIONS 6-7:
 * [markReadyForSubmission] verifies local data validity without scheduling network calls.
 * [ReportDraftEntity.idempotencyKey] ensures safe reconnect retries without creating duplicate cases.
 */
interface DraftRepository {
    fun observeDrafts(): Flow<List<ReportDraftEntity>>
    suspend fun getDraft(draftId: String): Result<ReportDraftEntity?>
    suspend fun saveDraft(formState: ReportFormState, draftId: String? = null): Result<String>
    suspend fun deleteDraft(draftId: String): Result<Unit>
    suspend fun markReadyForSubmission(draftId: String): Result<Unit>
}

class DraftRepositoryImpl(private val context: Context) : DraftRepository {

    private val db = SaafLoopDatabase.getInstance(context)
    private val dao = db.reportDraftDao()
    private val photoStorage = DraftPhotoStorage(context)

    override fun observeDrafts(): Flow<List<ReportDraftEntity>> {
        return dao.observeAllDrafts()
    }

    override suspend fun getDraft(draftId: String): Result<ReportDraftEntity?> = withContext(Dispatchers.IO) {
        try {
            val draft = dao.getDraftById(draftId)
            Result.success(draft)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveDraft(formState: ReportFormState, draftId: String?): Result<String> = withContext(Dispatchers.IO) {
        try {
            val finalDraftId = draftId ?: UUID.randomUUID().toString()
            val existingDraft = dao.getDraftById(finalDraftId)

            var savedPhotoPath: String? = existingDraft?.photoPath

            // Handle photo save / replacement safely
            val currentPhotoUri = formState.photoUri
            if (currentPhotoUri != null) {
                val newPhotoPath = photoStorage.savePhotoForDraft(currentPhotoUri, finalDraftId)
                if (newPhotoPath == null && currentPhotoUri.scheme != "file") {
                    return@withContext Result.failure(IllegalStateException("Failed to copy photo to app storage"))
                }
                if (newPhotoPath != null) {
                    val oldPhotoPath = existingDraft?.photoPath
                    if (oldPhotoPath != null && oldPhotoPath != newPhotoPath) {
                        val otherPaths = dao.getAllPhotoPaths()
                        photoStorage.deletePhotoFile(oldPhotoPath, otherPaths)
                    }
                    savedPhotoPath = newPhotoPath
                }
            } else {
                val oldPhotoPath = existingDraft?.photoPath
                if (oldPhotoPath != null) {
                    val otherPaths = dao.getAllPhotoPaths()
                    photoStorage.deletePhotoFile(oldPhotoPath, otherPaths)
                    savedPhotoPath = null
                }
            }

            // Status determination: Ready for submission vs Incomplete
            val isReady = formState.isPhotoValid && formState.isCategoryValid && formState.isLocationValid
            val status = if (isReady) DraftStatus.READY_FOR_SUBMISSION.name else DraftStatus.INCOMPLETE.name

            val draftEntity = ReportDraftEntity(
                draftId = finalDraftId,
                category = formState.category?.name,
                sizeEstimate = formState.sizeEstimate?.name,
                description = formState.description,
                accessNote = formState.accessNote,
                latitude = formState.latitude,
                longitude = formState.longitude,
                locationName = formState.locationName,
                photoPath = savedPhotoPath,
                isHazardousSuspected = formState.isHazardousSuspected,
                status = status,
                idempotencyKey = existingDraft?.idempotencyKey ?: UUID.randomUUID().toString(),
                createdAt = existingDraft?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            dao.insertOrUpdateDraft(draftEntity)
            Result.success(finalDraftId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDraft(draftId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val draft = dao.getDraftById(draftId)
            val photoPath = draft?.photoPath

            dao.deleteDraftById(draftId)

            if (photoPath != null) {
                val otherPaths = dao.getAllPhotoPaths()
                photoStorage.deletePhotoFile(photoPath, otherPaths)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markReadyForSubmission(draftId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val draft = dao.getDraftById(draftId) ?: return@withContext Result.failure(IllegalArgumentException("Draft not found"))
            val hasPhoto = !draft.photoPath.isNullOrBlank()
            val hasCategory = !draft.category.isNullOrBlank()
            val hasLocation = draft.latitude != 0.0 || draft.longitude != 0.0

            if (hasPhoto && hasCategory && hasLocation) {
                dao.updateDraftStatus(
                    draftId = draftId,
                    status = DraftStatus.READY_FOR_SUBMISSION.name,
                    updatedAt = System.currentTimeMillis()
                )
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("Draft is incomplete and cannot be marked ready for submission"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
