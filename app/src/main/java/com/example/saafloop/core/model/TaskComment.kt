package com.example.saafloop.core.model

import java.util.UUID

data class TaskComment(
    val commentId: String = UUID.randomUUID().toString(),
    val taskId: String,
    val authorUid: String,
    val authorName: String,
    val authorRole: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
