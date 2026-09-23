package com.example.saafloop.feature.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saafloop.core.data.AuthRepository
import com.example.saafloop.core.data.AuthRepositoryImpl
import com.example.saafloop.core.data.UserAccessState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val message: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository: AuthRepository = AuthRepositoryImpl(application)

    val userAccessState: StateFlow<UserAccessState> = authRepository.userAccessState
        .let { flow ->
            val stateFlow = MutableStateFlow<UserAccessState>(UserAccessState.SignedOut)
            viewModelScope.launch {
                flow.collect { stateFlow.value = it }
            }
            stateFlow.asStateFlow()
        }

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun isFirebaseConfigured(): Boolean = authRepository.isFirebaseConfigured()

    fun signInWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and password cannot be empty")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = authRepository.signInWithEmail(email, password)
            result.onSuccess { profile ->
                _uiState.value = AuthUiState.Success("Signed in as ${profile.displayName ?: profile.email}")
            }.onFailure { e ->
                _uiState.value = AuthUiState.Error(e.message ?: "Sign in failed")
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, displayName: String) {
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            _uiState.value = AuthUiState.Error("All fields are required")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = authRepository.signUpWithEmail(email, password, displayName)
            result.onSuccess { profile ->
                _uiState.value = AuthUiState.Success("Account created for ${profile.displayName}")
            }.onFailure { e ->
                _uiState.value = AuthUiState.Error(e.message ?: "Sign up failed")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun resetUiState() {
        _uiState.value = AuthUiState.Idle
    }
}
