package com.example.saafloop.core.model

data class OrganizationProfile(
    val orgId: String,
    val name: String,
    val type: String = "NGO",
    val description: String,
    val verificationStatus: OrgVerificationStatus = OrgVerificationStatus.VERIFIED,
    val activeMembersCount: Int = 12,
    val completedDrivesCount: Int = 8,
    val logoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
