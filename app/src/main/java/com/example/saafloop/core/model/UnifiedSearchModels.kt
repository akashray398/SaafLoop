package com.example.saafloop.core.model

/**
 * Resource type categories for unified civic search.
 */
enum class SearchResourceType(val label: String) {
    ALL("All Resources"),
    REPORT("Waste Reports"),
    ACTIVITY("Cleanup Drives"),
    TASK("Field Tasks"),
    ORGANIZATION("Organizations"),
    ANNOUNCEMENT("Civic Notices")
}

/**
 * Sorting options for search results.
 */
enum class SearchSortOption(val label: String) {
    RELEVANCE("Relevance"),
    DISTANCE("Nearest First"),
    MOST_RECENT("Most Recent"),
    PRIORITY("Highest Priority")
}

/**
 * Filter parameters for refining search queries.
 */
data class SearchFilterOptions(
    val selectedResourceType: SearchResourceType = SearchResourceType.ALL,
    val statusFilter: String = "ALL", // "ALL", "OPEN", "RESOLVED", "VERIFIED", "IN_PROGRESS"
    val categoryFilter: String = "ALL", // Waste or Activity category
    val maxDistanceMeters: Float = 5000f, // 5 km default radius
    val sortOption: SearchSortOption = SearchSortOption.RELEVANCE,
    val onlyAssignedToMe: Boolean = false
)

/**
 * Encapsulates a complete search request.
 */
data class UnifiedSearchQuery(
    val queryText: String = "",
    val filters: SearchFilterOptions = SearchFilterOptions(),
    val userLat: Double? = null,
    val userLng: Double? = null
)

/**
 * Unified search result item wrapping heterogeneous civic entities.
 */
data class UnifiedSearchResultItem(
    val id: String,
    val type: SearchResourceType,
    val title: String,
    val subtitle: String,
    val category: String,
    val status: String,
    val areaName: String,
    val distanceMeters: Float? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val navigationRoute: String
)
