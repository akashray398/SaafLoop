package com.example.saafloop.core.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.saafloop.core.data.DraftRepositoryImpl
import com.example.saafloop.core.data.RemoteCaseRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val draftId = inputData.getString(KEY_DRAFT_ID)
            ?: return@withContext Result.failure()

        val draftRepository = DraftRepositoryImpl(applicationContext)
        val remoteRepository = RemoteCaseRepositoryImpl(applicationContext)

        val draftResult = draftRepository.getDraft(draftId)
        val draft = draftResult.getOrNull()
            ?: return@withContext Result.failure()

        val submitResult = remoteRepository.submitCase(draft)
        if (submitResult.isSuccess) {
            // Delete local draft after confirmed server case creation
            draftRepository.deleteDraft(draftId)
            Result.success()
        } else {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val KEY_DRAFT_ID = "key_draft_id"
        const val WORK_TAG_PREFIX = "upload_draft_"
    }
}
