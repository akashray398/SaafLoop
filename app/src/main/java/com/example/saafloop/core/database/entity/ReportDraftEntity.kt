package com.example.saafloop.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "report_drafts",
    indices = [
        Index(value = ["updatedAt"]),
        Index(value = ["status"])
    ]
)
data class ReportDraftEntity(
    @PrimaryKey
    val draftId: String = UUID.randomUUID().toString(),
    val category: String? = null,
    val sizeEstimate: String? = null,
    val description: String = "",
    val accessNote: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationName: String = "",
    val photoPath: String? = null,
    val isHazardousSuspected: Boolean = false,
    val status: String = "INCOMPLETE",
    val idempotencyKey: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
