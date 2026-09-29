package com.example.saafloop.core.model

data class FieldTask(
    val taskId: String,
    val reportId: String,
    val title: String,
    val description: String,
    val category: String,
    val priority: CasePriority = CasePriority.MEDIUM,
    val status: TaskStatus = TaskStatus.UNASSIGNED,
    val latitude: Double,
    val longitude: Double,
    val approximateArea: String,
    val assignedToUid: String? = null,
    val assignedToName: String? = null,
    val assignedToRole: String? = null,
    val assignedByUid: String? = null,
    val assignedByName: String? = null,
    val teamId: String? = null,
    val dueAt: Long? = null,
    val checklistJson: String? = null,
    val beforePhotoPath: String? = null,
    val afterPhotoPath: String? = null,
    val completionNotes: String? = null,
    val rejectionReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
