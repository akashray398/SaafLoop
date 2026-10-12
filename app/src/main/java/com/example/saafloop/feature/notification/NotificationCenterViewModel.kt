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
    ARCHIVED("Archived"),
    SYSTEM("System"),
    AI("AI Insights")
}

class NotificationCenterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NotificationRepository = NotificationRepositoryImpl(application)
    private val authRepository: AuthRepository = AuthRepositoryImpl(application)

    private val _selectedTab = MutableStateFlow(NotificationTabFilter.ALL)
    val selectedTab: StateFlow<NotificationTabFilter> = _selectedTab.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedNotificationIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedNotificationIds: StateFlow<Set<String>> = _selectedNotificationIds.asStateFlow()

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
                NotificationTabFilter.ALL -> list.filter { !it.isArchived }
                NotificationTabFilter.UNREAD -> list.filter { !it.isRead && !it.isArchived }
                NotificationTabFilter.REPORTS -> list.filter { it.type == NotificationType.REPORT && !it.isArchived }
                NotificationTabFilter.TASKS -> list.filter { it.type == NotificationType.TASK && !it.isArchived }
                NotificationTabFilter.COMMUNITY -> list.filter { it.type == NotificationType.COMMUNITY && !it.isArchived }
                NotificationTabFilter.ARCHIVED -> list.filter { it.isArchived }
                NotificationTabFilter.SYSTEM -> list.filter { (it.type == NotificationType.SYSTEM || it.type == NotificationType.ORGANIZATION) && !it.isArchived }
                NotificationTabFilter.AI -> list.filter { it.type == NotificationType.AI && !it.isArchived }
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

    fun toggleSelectionMode() {
        val newMode = !_isSelectionMode.value
        _isSelectionMode.value = newMode
        if (!newMode) {
            _selectedNotificationIds.value = emptySet()
        }
    }

    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedNotificationIds.value = emptySet()
    }

    fun toggleNotificationSelection(id: String) {
        val current = _selectedNotificationIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedNotificationIds.value = current
        if (current.isNotEmpty() && !_isSelectionMode.value) {
            _isSelectionMode.value = true
        }
    }

    fun selectAllNotifications() {
        val allIds = notificationsState.value.map { it.notificationId }.toSet()
        _selectedNotificationIds.value = allIds
        if (allIds.isNotEmpty()) {
            _isSelectionMode.value = true
        }
    }

    fun deselectAllNotifications() {
        _selectedNotificationIds.value = emptySet()
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

    fun toggleReadStatus(notificationId: String) {
        viewModelScope.launch {
            repository.toggleReadStatus(notificationId)
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            repository.deleteNotification(notificationId)
        }
    }

    fun deleteSelectedNotifications() {
        val ids = _selectedNotificationIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.deleteNotifications(ids)
            exitSelectionMode()
        }
    }

    fun archiveNotification(notificationId: String) {
        viewModelScope.launch {
            repository.archiveNotification(notificationId)
        }
    }

    fun archiveSelectedNotifications() {
        val ids = _selectedNotificationIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.archiveNotifications(ids)
            exitSelectionMode()
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
