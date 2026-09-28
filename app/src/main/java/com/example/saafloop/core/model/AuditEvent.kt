package com.example.saafloop.core.model

import java.util.UUID

data class AuditEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val caseId: String,
    val actorUid: String,
    val actorName: String,
    val actorRole: String,
    val action: String,
    val previousStatus: String? = null,
    val newStatus: String? = null,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
