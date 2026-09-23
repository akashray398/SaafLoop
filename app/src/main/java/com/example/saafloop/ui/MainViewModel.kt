package com.example.saafloop.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saafloop.core.data.UserAccessState
import com.example.saafloop.core.data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Success(
        val isOnboardingCompleted: Boolean,
        val userAccessState: UserAccessState
    ) : MainUiState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserPreferencesRepository(application)

    val uiState: StateFlow<MainUiState> = combine(
        repository.isOnboardingCompleted,
        repository.userAccessState
    ) { isOnboardingCompleted, userAccessState ->
        MainUiState.Success(
            isOnboardingCompleted = isOnboardingCompleted,
            userAccessState = userAccessState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState.Loading
    )

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingCompleted(completed = true)
        }
    }

    fun startGuestSession() {
        viewModelScope.launch {
            repository.setGuestSessionActive(active = true)
        }
    }

    fun exitGuestMode() {
        viewModelScope.launch {
            repository.exitGuestMode()
        }
    }
}
