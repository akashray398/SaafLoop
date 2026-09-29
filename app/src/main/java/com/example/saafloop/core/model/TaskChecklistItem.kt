package com.example.saafloop.core.model

data class TaskChecklistItem(
    val id: Int,
    val label: String,
    val isCompleted: Boolean = false,
    val isMandatory: Boolean = true
)
