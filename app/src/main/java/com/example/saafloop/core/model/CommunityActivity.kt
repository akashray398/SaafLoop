package com.example.saafloop.core.model

data class CommunityActivity(
    val activityId: String,
    val title: String,
    val category: ActivityCategory = ActivityCategory.CLEANUP_DRIVE,
    val description: String,
    val approximateArea: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val startDate: Long,
    val startTime: String,
    val expectedDurationHours: Int = 2,
    val maxParticipants: Int = 20,
    val currentParticipantsCount: Int = 0,
    val organizerUid: String,
    val organizerName: String,
    val organizerType: String = "NGO",
    val status: ActivityStatus = ActivityStatus.PUBLISHED,
    val requiredEquipment: String? = null,
    val safetyInstructions: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
