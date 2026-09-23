package com.example.saafloop.core.model

data class Organisation(
    val orgId: String,
    val displayName: String,
    val serviceArea: String,
    val verificationStatus: String = "UNVERIFIED"
)
