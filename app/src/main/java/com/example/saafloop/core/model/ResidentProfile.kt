package com.example.saafloop.core.model

data class ResidentProfile(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
