package com.example.saafloop.feature.notification

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saafloop.core.data.AuthRepository
import com.example.saafloop.core.data.AuthRepositoryImpl
import com.example.saafloop.core.data.NotificationRepository
import com.example.saafloop.core.data.NotificationRepositoryImpl
import com.example.saafloop.core.data.UserAccessState
import com.example.saafloop.core.model.NotificationItem
import com.example.saafloop.core.model.NotificationPreferences
import com.example.saafloop.core.model.NotificationPriority
import com.example.saafloop.core.model.NotificationType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NotificationTabFilter(val label: String) {
    ALL("All"),
    UNREAD("Unread"),
    REPORTS("Reports"),
    TASKS("Tasks"),
    COMMUNITY("Community"),
    SYSTEM("System"),
    AI("AI Insights")
}

class NotificationCenterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NotificationRepository = NotificationRepositoryImpl(application)
    private val authRepository: AuthRepository = AuthRepositoryImpl(application)

    private val _selectedTab = MutableStateFlow(NotificationTabFilter.ALL)
    val selectedTab: StateFlow<NotificationTabFilter> = _selectedTab.asStateFlow()

    val userAccessState: StateFlow<UserAccessState> = authRepository.userAccessState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserAccessState.Guest
        )

    private val currentUserIdFlow = userAccessState.combine(_selectedTab) { access, tab ->
        val uid = authRepository.getCurrentUserUid() ?: "GUEST"
        val role = when (access) {
            is UserAccessState.AuthenticatedResident -> "CITIZEN"
            is UserAccessState.VerifiedOrganisation -> "COORDINATOR"
            else -> "ALL"
        }
        Pair(uid, role)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val notificationsState: StateFlow<List<NotificationItem>> = currentUserIdFlow
        .flatMapLatest { (uid, role) ->
            repository.observeUserNotifications(uid, role)
        }
        .combine(_selectedTab) { list, tab ->
            when (tab) {
                NotificationTabFilter.ALL -> list
                NotificationTabFilter.UNREAD -> list.filter { !it.isRead }
                NotificationTabFilter.REPORTS -> list.filter { it.type == NotificationType.REPORT }
                NotificationTabFilter.TASKS -> list.filter { it.type == NotificationType.TASK }
                NotificationTabFilter.COMMUNITY -> list.filter { it.type == NotificationType.COMMUNITY }
                NotificationTabFilter.SYSTEM -> list.filter { it.type == NotificationType.SYSTEM || it.type == NotificationType.ORGANIZATION }
                NotificationTabFilter.AI -> list.filter { it.type == NotificationType.AI }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val unreadCountState: StateFlow<Int> = currentUserIdFlow
        .flatMapLatest { (uid, role) ->
            repository.observeUnreadCount(uid, role)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    val preferencesState: StateFlow<NotificationPreferences> = repository
        .observeNotificationPreferences(authRepository.getCurrentUserUid() ?: "LOCAL")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NotificationPreferences()
        )

    fun selectTab(tab: NotificationTabFilter) {
        _selectedTab.value = tab
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        val uid = authRepository.getCurrentUserUid() ?: "LOCAL"
        viewModelScope.launch {
            repository.markAllNotificationsAsRead(uid)
        }
    }

    fun updatePreferences(prefs: NotificationPreferences) {
        val uid = authRepository.getCurrentUserUid() ?: "LOCAL"
        viewModelScope.launch {
            repository.updateNotificationPreferences(uid, prefs)
        }
    }

    fun publishAdminAnnouncement(
        title: String,
        body: String,
        targetRole: String,
        priority: NotificationPriority,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.publishAdminAnnouncement(title, body, targetRole, priority)
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Failed to publish announcement") }
        }
    }
}
