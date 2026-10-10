package com.example.saafloop.core.domain

import com.example.saafloop.core.data.UnifiedSearchRepository
import com.example.saafloop.core.model.UnifiedSearchQuery
import com.example.saafloop.core.model.UnifiedSearchResultItem
import kotlinx.coroutines.flow.Flow

class ExecuteUnifiedSearchUseCase(
    private val repository: UnifiedSearchRepository
) {
    fun executeSearch(query: UnifiedSearchQuery): Flow<List<UnifiedSearchResultItem>> {
        return repository.executeSearch(query)
    }

    fun getRecentSearchTerms(): Flow<List<String>> {
        return repository.getRecentSearchTerms()
    }

    suspend fun saveSearchTerm(term: String) {
        repository.saveSearchTerm(term)
    }

    suspend fun clearSearchHistory() {
        repository.clearSearchHistory()
    }
}
