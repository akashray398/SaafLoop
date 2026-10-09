package com.example.saafloop.feature.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saafloop.core.data.AuthRepository
import com.example.saafloop.core.data.AuthRepositoryImpl
import com.example.saafloop.core.data.SecurityRepository
import com.example.saafloop.core.data.SecurityRepositoryImpl
import com.example.saafloop.core.data.UserAccessState
import com.example.saafloop.core.model.AbuseReport
import com.example.saafloop.core.model.PrivacySettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SecurityUiState {
    data object Idle : SecurityUiState
    data object Loading : SecurityUiState
    data class Success(val message: String) : SecurityUiState
    data class Error(val message: String) : SecurityUiState
}

class SecurityPrivacyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SecurityRepository = SecurityRepositoryImpl(application)
    private val authRepository: AuthRepository = AuthRepositoryImpl(application)

    private val _uiState = MutableStateFlow<SecurityUiState>(SecurityUiState.Idle)
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    val userAccessState: StateFlow<UserAccessState> = authRepository.userAccessState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserAccessState.Guest
        )

    private val currentUserIdFlow = MutableStateFlow(authRepository.getCurrentUserUid() ?: "GUEST")

    @OptIn(ExperimentalCoroutinesApi::class)
    val privacySettingsState: StateFlow<PrivacySettings> = currentUserIdFlow
        .flatMapLatest { uid ->
            repository.observePrivacySettings(uid)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PrivacySettings()
        )

    fun updatePrivacySettings(settings: PrivacySettings) {
        val uid = authRepository.getCurrentUserUid() ?: "GUEST"
        viewModelScope.launch {
            repository.updatePrivacySettings(uid, settings)
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            _uiState.value = SecurityUiState.Error("Please enter your registered email address")
            return
        }

        _uiState.value = SecurityUiState.Loading
        viewModelScope.launch {
            val result = repository.sendPasswordResetEmail(email)
            result.onSuccess {
                _uiState.value = SecurityUiState.Success("Password reset email sent to $email. Please check your inbox.")
            }.onFailure { e ->
                _uiState.value = SecurityUiState.Error(e.message ?: "Failed to send password reset email")
            }
        }
    }

    fun deleteAccountAndAnonymizeData(onSuccess: () -> Unit) {
        val uid = authRepository.getCurrentUserUid() ?: "GUEST"
        _uiState.value = SecurityUiState.Loading

        viewModelScope.launch {
            val result = repository.deleteAccountAndAnonymizeData(uid)
            result.onSuccess {
                _uiState.value = SecurityUiState.Success("Account deleted and data anonymized.")
                authRepository.signOut()
                onSuccess()
            }.onFailure { e ->
                _uiState.value = SecurityUiState.Error(e.message ?: "Failed to delete account")
            }
        }
    }

    fun submitAbuseReport(
        targetEntityType: String,
        targetEntityId: String,
        reason: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        val uid = authRepository.getCurrentUserUid() ?: "ANONYMOUS"
        val abuseReport = AbuseReport(
            abuseId = "",
            targetEntityType = targetEntityType,
            targetEntityId = targetEntityId,
            reporterUid = uid,
            reason = reason,
            notes = notes
        )

        viewModelScope.launch {
            repository.submitAbuseReport(abuseReport)
            onSuccess()
        }
    }

    fun resetUiState() {
        _uiState.value = SecurityUiState.Idle
    }
}
