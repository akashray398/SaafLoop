package com.example.saafloop.core.domain

import android.net.Uri
import com.example.saafloop.core.data.CivicAIRepository
import com.example.saafloop.core.model.ReportAIAnalysis

class AnalyzeCivicReportUseCase(
    private val repository: CivicAIRepository
) {
    suspend operator fun invoke(
        photoUri: Uri?,
        photoPath: String?,
        userCategory: String?,
        userDescription: String,
        locationName: String,
        latitude: Double,
        longitude: Double
    ): Result<ReportAIAnalysis> {
        return repository.analyzeReportImageAndContext(
            photoUri = photoUri,
            photoPath = photoPath,
            userCategory = userCategory,
            userDescription = userDescription,
            locationName = locationName,
            latitude = latitude,
            longitude = longitude
        )
    }
}
