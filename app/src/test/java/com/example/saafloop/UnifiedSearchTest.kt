package com.example.saafloop

import android.content.Context
import android.content.ContextWrapper
import com.example.saafloop.core.data.UnifiedSearchRepositoryImpl
import com.example.saafloop.core.model.SearchFilterOptions
import com.example.saafloop.core.model.SearchResourceType
import com.example.saafloop.core.model.SearchSortOption
import com.example.saafloop.core.model.UnifiedSearchQuery
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UnifiedSearchTest {

    private lateinit var fakeContext: Context
    private lateinit var repository: UnifiedSearchRepositoryImpl

    @Before
    fun setUp() {
        fakeContext = SearchTestContext()
        repository = UnifiedSearchRepositoryImpl(fakeContext)
    }

    @Test
    fun executeSearch_returnsFilteredResultsMatchingText() = runBlocking {
        val query = UnifiedSearchQuery(
            queryText = "Sector 68",
            filters = SearchFilterOptions(selectedResourceType = SearchResourceType.ALL)
        )

        val results = repository.executeSearch(query).first()

        assertNotNull(results)
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.areaName.contains("Sector 68") })
    }

    @Test
    fun executeSearch_filtersByResourceTypeAndDistance() = runBlocking {
        val query = UnifiedSearchQuery(
            queryText = "",
            filters = SearchFilterOptions(
                selectedResourceType = SearchResourceType.REPORT,
                maxDistanceMeters = 1000f
            ),
            userLat = 30.7046,
            userLng = 76.7178
        )

        val results = repository.executeSearch(query).first()

        assertNotNull(results)
        assertTrue(results.all { it.type == SearchResourceType.REPORT })
        assertTrue(results.all { (it.distanceMeters ?: 0f) <= 1000f })
    }

    @Test
    fun executeSearch_sortsByDistanceNearestFirst() = runBlocking {
        val query = UnifiedSearchQuery(
            queryText = "",
            filters = SearchFilterOptions(
                selectedResourceType = SearchResourceType.ALL,
                sortOption = SearchSortOption.DISTANCE
            ),
            userLat = 30.7046,
            userLng = 76.7178
        )

        val results = repository.executeSearch(query).first()

        assertTrue(results.isNotEmpty())
        if (results.size > 1) {
            val firstDist = results[0].distanceMeters ?: Float.MAX_VALUE
            val secondDist = results[1].distanceMeters ?: Float.MAX_VALUE
            assertTrue(firstDist <= secondDist)
        }
    }

    @Test
    fun getRecentSearchTerms_returnsRecentSearchList() = runBlocking {
        val terms = repository.getRecentSearchTerms().first()

        assertNotNull(terms)
        assertTrue(terms.isNotEmpty())
    }
}

private class SearchTestContext : ContextWrapper(null) {
    override fun getApplicationContext(): Context = this
}
