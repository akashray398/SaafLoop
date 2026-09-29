package com.example.saafloop.core.model

data class ActivityParticipant(
    val participantId: String,
    val activityId: String,
    val userUid: String,
    val userName: String,
    val status: ParticipantStatus = ParticipantStatus.JOINED,
    val checkInTime: Long? = null,
    val checkInToken: String? = null,
    val joinedAt: Long = System.currentTimeMillis()
)
