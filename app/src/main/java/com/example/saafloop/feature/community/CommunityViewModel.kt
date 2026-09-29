package com.example.saafloop.feature.community

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saafloop.core.data.AuthRepository
import com.example.saafloop.core.data.AuthRepositoryImpl
import com.example.saafloop.core.data.CommunityRepository
import com.example.saafloop.core.data.CommunityRepositoryImpl
import com.example.saafloop.core.model.ActivityParticipant
import com.example.saafloop.core.model.CommunityActivity
import com.example.saafloop.core.model.CommunityBadge
import com.example.saafloop.core.model.OrganizationProfile
import com.example.saafloop.core.model.ParticipantStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CommunityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CommunityRepository = CommunityRepositoryImpl(application)
    private val authRepository: AuthRepository = AuthRepositoryImpl(application)

    private val _selectedActivityId = MutableStateFlow<String?>(null)
    val selectedActivityId: StateFlow<String?> = _selectedActivityId.asStateFlow()

    val nearbyActivitiesState: StateFlow<List<CommunityActivity>> = repository.observeNearbyActivities()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val organizationsState: StateFlow<List<OrganizationProfile>> = repository.observeOrganizations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val userBadgesState: StateFlow<List<CommunityBadge>> = repository.observeUserBadges(
        authRepository.getCurrentUserUid() ?: ""
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val userContributionsState: StateFlow<List<ActivityParticipant>> = repository.observeUserContributions(
        authRepository.getCurrentUserUid() ?: ""
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeActivityDetailState: StateFlow<CommunityActivity?> = _selectedActivityId
        .flatMapLatest { id ->
            if (id.isNullOrBlank()) flowOf(null)
            else repository.observeActivityDetails(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeActivityParticipantsState: StateFlow<List<ActivityParticipant>> = _selectedActivityId
        .flatMapLatest { id ->
            if (id.isNullOrBlank()) flowOf(emptyList())
            else repository.observeActivityParticipants(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun selectActivity(activityId: String) {
        _selectedActivityId.value = activityId
    }

    fun joinActivity(
        activityId: String,
        onSuccess: (ParticipantStatus) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = authRepository.getCurrentUserUid() ?: "GUEST_VOLUNTEER"
        viewModelScope.launch {
            val result = repository.joinActivity(activityId, uid, "Community Volunteer")
            result.onSuccess { status -> onSuccess(status) }.onFailure { onError(it.message ?: "Failed to join drive") }
        }
    }

    fun leaveActivity(
        activityId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = authRepository.getCurrentUserUid() ?: "GUEST_VOLUNTEER"
        viewModelScope.launch {
            val result = repository.leaveActivity(activityId, uid)
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Failed to leave drive") }
        }
    }

    fun createActivity(
        activity: CommunityActivity,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.createActivity(activity)
            result.onSuccess { id -> onSuccess(id) }.onFailure { onError(it.message ?: "Failed to submit activity proposal") }
        }
    }

    fun approveActivity(
        activityId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = authRepository.getCurrentUserUid() ?: "COORDINATOR_LOCAL"
        viewModelScope.launch {
            val result = repository.approveActivity(activityId, uid)
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Failed to approve activity") }
        }
    }

    fun checkInParticipant(
        activityId: String,
        checkInToken: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = authRepository.getCurrentUserUid() ?: "GUEST_VOLUNTEER"
        viewModelScope.launch {
            val result = repository.checkInParticipant(activityId, uid, checkInToken)
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Failed to check in") }
        }
    }
}
