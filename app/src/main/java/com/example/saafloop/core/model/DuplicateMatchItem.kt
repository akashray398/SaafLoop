package com.example.saafloop.core.model

data class DuplicateMatchItem(
    val caseReport: CaseReport,
    val distanceMeters: Int,
    val matchReason: String,
    val confidenceScore: Float
)
