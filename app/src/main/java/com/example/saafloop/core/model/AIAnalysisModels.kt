package com.example.saafloop.core.model

/**
 * Lifecycle processing states for AI report analysis jobs.
 */
enum class AIAnalysisStatus(val label: String) {
    NOT_REQUESTED("Not Requested"),
    QUEUED("Queued for Analysis"),
    PROCESSING("Analyzing Image & Context..."),
    COMPLETED("Analysis Completed"),
    PARTIALLY_COMPLETED("Partially Completed"),
    FAILED_RETRYABLE("Temporary Failure (Retryable)"),
    FAILED_PERMANENT("Analysis Unavailable"),
    CANCELLED("Cancelled")
}

/**
 * Image quality and clarity feedback for citizens prior to submission.
 */
enum class ImageQualityAssessment(val label: String, val isAcceptable: Boolean) {
    ACCEPTABLE("Image is clear and well-lit", true),
    BLURRY("Image appears blurry. Retaking may improve cleanup response", false),
    DARK("Lighting is low. Waste details may be obscured", false),
    OBSTRUCTED("Subject is partially obstructed or ambiguous", false),
    UNRECOGNIZED("Image may not show a public cleanliness issue", false)
}

/**
 * Structured, validated response contract for AI civic report analysis.
 *
 * NOTE: All fields are suggestions. Human review by an authorized coordinator
 * is strictly required before changing report status or priority.
 */
data class ReportAIAnalysis(
    val analysisId: String,
    val reportId: String,
    val schemaVersion: Int = 1,
    val status: AIAnalysisStatus = AIAnalysisStatus.COMPLETED,
    val suggestedCategory: String? = null,
    val suggestedSeverity: IssueSeverity? = null,
    val confidence: Float = 0.82f,
    val imageQuality: ImageQualityAssessment = ImageQualityAssessment.ACCEPTABLE,
    val suggestedDescription: String = "",
    val detectedWasteTypes: List<String> = emptyList(),
    val possibleDuplicateIds: List<String> = emptyList(),
    val severityReasoning: List<String> = emptyList(),
    val requiresHumanReview: Boolean = true,
    val warnings: List<String> = emptyList(),
    val modelProvider: String = "SaafLoop Civic AI Pipeline v1.2",
    val modelVersion: String = "civic-vision-hybrid-1",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = System.currentTimeMillis(),
    val errorCode: String? = null
)

/**
 * Aggregated civic intelligence insights for municipal coordinators and administrators.
 */
data class CivicIntelligenceData(
    val topCategories: List<Pair<String, Int>> = emptyList(),
    val backlogCount: Int = 0,
    val reportsApproachingTargetCount: Int = 0,
    val recurringHotspotSectors: List<String> = emptyList(),
    val operationalRecommendations: List<String> = emptyList(),
    val misclassificationRate: Float = 0.04f,
    val lastRefreshedAt: Long = System.currentTimeMillis(),
    val dataQualityMessage: String = "Based on verified civic report data across all sectors in last 30 days."
)
