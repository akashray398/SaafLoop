package com.example.saafloop.feature.report.model

sealed interface SubmissionState {
    data object Draft : SubmissionState
    data object ReadyToSubmit : SubmissionState
    data object QueuedForUpload : SubmissionState
    data object UploadingPhoto : SubmissionState
    data object FinalisingCase : SubmissionState
    data class SubmittedConfirmed(val serverCaseId: String) : SubmissionState
    data class NeedsAttention(val errorReason: String) : SubmissionState
}
