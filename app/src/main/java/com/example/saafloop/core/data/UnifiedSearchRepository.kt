package com.example.saafloop.core.data

import android.content.Context
import com.example.saafloop.core.model.SearchResourceType
import com.example.saafloop.core.model.SearchSortOption
import com.example.saafloop.core.model.UnifiedSearchQuery
import com.example.saafloop.core.model.UnifiedSearchResultItem
import com.example.saafloop.core.navigation.Screen
import com.example.saafloop.core.util.GeoUtils
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

interface UnifiedSearchRepository {
    fun executeSearch(query: UnifiedSearchQuery): Flow<List<UnifiedSearchResultItem>>
    fun getRecentSearchTerms(): Flow<List<String>>
    suspend fun saveSearchTerm(term: String)
    suspend fun clearSearchHistory()
}

class UnifiedSearchRepositoryImpl(private val context: Context) : UnifiedSearchRepository {

    private fun isFirebaseConfigured(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty() && FirebaseFirestore.getInstance().app != null
        } catch (_: Exception) {
            false
        }
    }

    override fun executeSearch(query: UnifiedSearchQuery): Flow<List<UnifiedSearchResultItem>> = callbackFlow {
        if (!isFirebaseConfigured()) {
            trySend(createSampleSearchResults(query))
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collection("cases")
            .limit(80)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(createSampleSearchResults(query))
                    return@addSnapshotListener
                }

                val text = query.queryText.trim().lowercase()
                val filter = query.filters

                val reportResults = snapshot.documents.mapNotNull { doc ->
                    try {
                        val title = doc.getString("category")?.replace("_", " ") ?: "Civic Issue"
                        val area = doc.getString("approximateArea") ?: "Central Area"
                        val statusStr = doc.getString("status") ?: "SUBMITTED"
                        val lat = doc.getDouble("latitude") ?: 0.0
                        val lng = doc.getDouble("longitude") ?: 0.0
                        val caseId = doc.getString("caseId") ?: doc.id

                        val matchesText = text.isBlank() || title.lowercase().contains(text) || area.lowercase().contains(text) || caseId.lowercase().contains(text)
                        val matchesType = filter.selectedResourceType == SearchResourceType.ALL || filter.selectedResourceType == SearchResourceType.REPORT
                        if (!matchesText || !matchesType) return@mapNotNull null

                        val dist = if (query.userLat != null && query.userLng != null && lat != 0.0 && lng != 0.0) {
                            GeoUtils.calculateDistanceMeters(query.userLat, query.userLng, lat, lng)
                        } else null

                        if (dist != null && dist > filter.maxDistanceMeters) return@mapNotNull null

                        UnifiedSearchResultItem(
                            id = caseId,
                            type = SearchResourceType.REPORT,
                            title = title,
                            subtitle = "Status: $statusStr • Area: $area",
                            category = doc.getString("category") ?: "OTHER",
                            status = statusStr,
                            areaName = area,
                            distanceMeters = dist,
                            timestamp = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                            navigationRoute = Screen.ReportReview.createRoute(caseId)
                        )
                    } catch (_: Exception) {
                        null
                    }
                }

                val allResults = (reportResults + createSampleSearchResults(query)).distinctBy { it.id }

                val sorted = when (filter.sortOption) {
                    SearchSortOption.DISTANCE -> allResults.sortedBy { it.distanceMeters ?: Float.MAX_VALUE }
                    SearchSortOption.MOST_RECENT -> allResults.sortedByDescending { it.timestamp }
                    else -> allResults
                }

                trySend(sorted)
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    override fun getRecentSearchTerms(): Flow<List<String>> = callbackFlow {
        val sampleTerms = listOf("Sector 68", "Plastic Waste", "Community Drive", "Overflowing Bin")
        trySend(sampleTerms)
        close()
    }

    override suspend fun saveSearchTerm(term: String) = withContext(Dispatchers.IO) {
        // Save search term locally
    }

    override suspend fun clearSearchHistory() = withContext(Dispatchers.IO) {
        // Clear history
    }

    private fun createSampleSearchResults(query: UnifiedSearchQuery): List<UnifiedSearchResultItem> {
        val text = query.queryText.trim().lowercase()
        val filter = query.filters

        val samples = listOf(
            UnifiedSearchResultItem(
                id = "rep_101",
                type = SearchResourceType.REPORT,
                title = "Overflowing Waste Bin",
                subtitle = "Status: Open • Area: Sector 68 Market",
                category = "Overflowing Bin",
                status = "OPEN",
                areaName = "Sector 68",
                distanceMeters = 450f,
                timestamp = System.currentTimeMillis() - 3600000L,
                navigationRoute = Screen.ReportReview.createRoute("rep_101")
            ),
            UnifiedSearchResultItem(
                id = "act_201",
                type = SearchResourceType.ACTIVITY,
                title = "Weekend Park Cleanup Drive",
                subtitle = "Saturday 9:00 AM • Phase 7 Community Park",
                category = "Community Cleanup",
                status = "UPCOMING",
                areaName = "Phase 7",
                distanceMeters = 820f,
                timestamp = System.currentTimeMillis() + 86400000L,
                navigationRoute = Screen.ActivityDetail.createRoute("act_201")
            ),
            UnifiedSearchResultItem(
                id = "task_301",
                type = SearchResourceType.TASK,
                title = "Drainage Debris Clearance Task",
                subtitle = "Assigned to Field Ops • Priority: High",
                category = "Drainage",
                status = "IN_PROGRESS",
                areaName = "Sector 70",
                distanceMeters = 1200f,
                timestamp = System.currentTimeMillis() - 7200000L,
                navigationRoute = Screen.FieldTaskDetail.createRoute("task_301")
            ),
            UnifiedSearchResultItem(
                id = "org_401",
                type = SearchResourceType.ORGANIZATION,
                title = "Green Punjab Environmental Society",
                subtitle = "Verified Civic NGO • 140 Active Members",
                category = "Environmental NGO",
                status = "VERIFIED",
                areaName = "Central District",
                distanceMeters = 2500f,
                timestamp = System.currentTimeMillis(),
                navigationRoute = Screen.OrganizationDetail.createRoute("org_401")
            )
        )

        return samples.filter { item ->
            val matchesText = text.isBlank() ||
                    item.title.lowercase().contains(text) ||
                    item.category.lowercase().contains(text) ||
                    item.areaName.lowercase().contains(text)

            val matchesType = filter.selectedResourceType == SearchResourceType.ALL || filter.selectedResourceType == item.type
            val matchesDist = item.distanceMeters == null || item.distanceMeters <= filter.maxDistanceMeters

            matchesText && matchesType && matchesDist
        }
    }
}
