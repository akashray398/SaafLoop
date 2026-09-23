package com.example.saafloop.feature.report.model

import android.net.Uri

enum class ReportFormStage(val stageNumber: Int, val titleRes: Int) {
    PHOTO_INPUT(1, com.example.saafloop.R.string.report_stage_1),
    DETAILS_AND_LOCATION(2, com.example.saafloop.R.string.report_stage_2),
    REVIEW(3, com.example.saafloop.R.string.report_stage_3)
}

data class ReportFormState(
    val currentStage: ReportFormStage = ReportFormStage.PHOTO_INPUT,
    val photoUri: Uri? = null,
    val category: WasteCategory? = null,
    val sizeEstimate: WasteSizeEstimate? = null,
    val description: String = "",
    val accessNote: String = "",
    val latitude: Double = 28.6139,
    val longitude: Double = 77.2090,
    val locationName: String = "Central District",
    val isHazardousSuspected: Boolean = false,
    val validationTriggered: Boolean = false
) {
    val isPhotoValid: Boolean
        get() = photoUri != null

    val isCategoryValid: Boolean
        get() = category != null

    val isLocationValid: Boolean
        get() = latitude != 0.0 && longitude != 0.0

    val isValid: Boolean
        get() = isPhotoValid && isCategoryValid && isLocationValid

    val hasEnteredContent: Boolean
        get() = photoUri != null || category != null || sizeEstimate != null ||
                description.isNotBlank() || accessNote.isNotBlank()
}
