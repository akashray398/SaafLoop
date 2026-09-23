package com.example.saafloop.core.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class DraftPhotoStorage(private val context: Context) {

    private val photosDir: File
        get() = File(context.filesDir, "draft_photos").apply {
            if (!exists()) mkdirs()
        }

    suspend fun savePhotoForDraft(sourceUri: Uri, draftId: String): String? = withContext(Dispatchers.IO) {
        try {
            // Check if sourceUri is already inside draft_photos (already a saved private copy)
            val sourcePath = sourceUri.path
            if (sourcePath != null && sourcePath.contains("draft_photos")) {
                val existingFile = File(sourcePath)
                if (existingFile.exists()) {
                    return@withContext existingFile.absolutePath
                }
            }

            val destinationFile = File(photosDir, "draft_${draftId}_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null

            if (destinationFile.exists() && destinationFile.length() > 0) {
                destinationFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deletePhotoFile(photoPath: String?, otherActivePaths: List<String>) = withContext(Dispatchers.IO) {
        if (photoPath.isNullOrBlank()) return@withContext
        try {
            val file = File(photoPath)
            if (file.exists() && !otherActivePaths.contains(photoPath)) {
                file.delete()
            }
        } catch (_: Exception) {
            // Safe ignore delete failure
        }
    }
}
