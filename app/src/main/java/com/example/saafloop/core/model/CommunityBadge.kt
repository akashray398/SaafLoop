package com.example.saafloop.core.model

data class CommunityBadge(
    val badgeId: String,
    val title: String,
    val description: String,
    val requiredActivitiesCount: Int = 1,
    val earnedAt: Long? = null
)
