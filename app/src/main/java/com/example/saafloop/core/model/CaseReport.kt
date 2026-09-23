package com.example.saafloop.core.model

data class CaseReport(
    val caseId: String,
    val authorUid: String,
    val category: String,
    val sizeEstimate: String? = null,
    val description: String = "",
    val accessNote: String = "",
    val approximateArea: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: CaseStatus = CaseStatus.SUBMITTED,
    val assignedOrgId: String? = null,
    val isHazardousSuspected: Boolean = false,
    val photoStoragePath: String? = null,
    val idempotencyKey: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
