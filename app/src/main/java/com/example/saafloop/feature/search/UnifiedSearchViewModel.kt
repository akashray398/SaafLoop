package com.example.saafloop.feature.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saafloop.core.data.UnifiedSearchRepository
import com.example.saafloop.core.data.UnifiedSearchRepositoryImpl
import com.example.saafloop.core.domain.ExecuteUnifiedSearchUseCase
import com.example.saafloop.core.model.SearchFilterOptions
import com.example.saafloop.core.model.UnifiedSearchQuery
import com.example.saafloop.core.model.UnifiedSearchResultItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UnifiedSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UnifiedSearchRepository = UnifiedSearchRepositoryImpl(application)
    private val useCase = ExecuteUnifiedSearchUseCase(repository)

    private val _queryText = MutableStateFlow("")
    val queryText: StateFlow<String> = _queryText.asStateFlow()

    private val _filters = MutableStateFlow(SearchFilterOptions())
    val filters: StateFlow<SearchFilterOptions> = _filters.asStateFlow()

    val recentSearchTermsState: StateFlow<List<String>> = useCase.getRecentSearchTerms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResultsState: StateFlow<List<UnifiedSearchResultItem>> = combine(
        _queryText.debounce(300L),
        _filters
    ) { text, filterOptions ->
        UnifiedSearchQuery(
            queryText = text,
            filters = filterOptions,
            userLat = 30.7046, // Default user locality latitude
            userLng = 76.7178  // Default user locality longitude
        )
    }.flatMapLatest { searchQuery ->
        useCase.executeSearch(searchQuery)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun onQueryChanged(newText: String) {
        _queryText.value = newText
    }

    fun applyFilters(newFilters: SearchFilterOptions) {
        _filters.value = newFilters
    }

    fun saveRecentSearch(term: String) {
        viewModelScope.launch {
            useCase.saveSearchTerm(term)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            useCase.clearSearchHistory()
        }
    }
}
