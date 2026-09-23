package com.example.saafloop.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object PhotoMetadataUtils {

    /**
     * Re-encodes an image to strip embedded EXIF GPS/device metadata and resizes
     * it if necessary prior to Cloud Storage upload.
     */
    suspend fun stripExifAndCompress(
        context: Context,
        inputFile: File,
        maxDimension: Int = 1920,
        quality: Int = 85
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (!inputFile.exists()) {
                return@withContext Result.failure(IllegalArgumentException("Source photo file does not exist"))
            }

            // Decode dimensions first
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(inputFile.absolutePath, options)

            val width = options.outWidth
            val height = options.outHeight

            // Calculate inSampleSize for memory safety
            var sampleSize = 1
            if (width > maxDimension || height > maxDimension) {
                val halfWidth = width / 2
                val halfHeight = height / 2
                while (halfWidth / sampleSize >= maxDimension && halfHeight / sampleSize >= maxDimension) {
                    sampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }

            val bitmap = BitmapFactory.decodeFile(inputFile.absolutePath, decodeOptions)
                ?: return@withContext Result.failure(IllegalStateException("Failed to decode image bitmap"))

            // Strip EXIF metadata by writing fresh JPEG stream without EXIF tags
            val outputFile = File(context.cacheDir, "stripped_upload_${UUID.randomUUID()}.jpg")
            FileOutputStream(outputFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }

            bitmap.recycle()

            if (outputFile.exists() && outputFile.length() > 0) {
                Result.success(outputFile)
            } else {
                Result.failure(IllegalStateException("Failed to write stripped photo file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
