package com.example.saafloop.feature.fieldops

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saafloop.core.data.AuthRepository
import com.example.saafloop.core.data.AuthRepositoryImpl
import com.example.saafloop.core.data.TaskRepository
import com.example.saafloop.core.data.TaskRepositoryImpl
import com.example.saafloop.core.model.FieldTask
import com.example.saafloop.core.model.TaskChecklistItem
import com.example.saafloop.core.model.TaskComment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository = TaskRepositoryImpl(application)
    private val authRepository: AuthRepository = AuthRepositoryImpl(application)

    private val _selectedTaskId = MutableStateFlow<String?>(null)
    val selectedTaskId: StateFlow<String?> = _selectedTaskId.asStateFlow()

    val assignedTasksState: StateFlow<List<FieldTask>> = repository.observeAssignedTasks(
        authRepository.getCurrentUserUid() ?: ""
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeTaskDetailState: StateFlow<FieldTask?> = _selectedTaskId
        .flatMapLatest { taskId ->
            if (taskId.isNullOrBlank()) flowOf(null)
            else repository.observeTaskDetails(taskId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeTaskCommentsState: StateFlow<List<TaskComment>> = _selectedTaskId
        .flatMapLatest { taskId ->
            if (taskId.isNullOrBlank()) flowOf(emptyList())
            else repository.observeTaskComments(taskId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun selectTask(taskId: String) {
        _selectedTaskId.value = taskId
    }

    fun acceptTask(
        taskId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = authRepository.getCurrentUserUid() ?: "WORKER_LOCAL"
        viewModelScope.launch {
            val result = repository.acceptTask(taskId, uid, "Field Worker")
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Failed to accept task") }
        }
    }

    fun declineTask(
        taskId: String,
        reason: String,
        notes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = authRepository.getCurrentUserUid() ?: "WORKER_LOCAL"
        viewModelScope.launch {
            val result = repository.declineTask(taskId, reason, notes, uid)
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Failed to decline task") }
        }
    }

    fun startTask(
        taskId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = authRepository.getCurrentUserUid() ?: "WORKER_LOCAL"
        viewModelScope.launch {
            val result = repository.startTask(taskId, uid)
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Failed to start task") }
        }
    }

    fun submitTaskCompletion(
        taskId: String,
        afterPhotoPath: String?,
        notes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = authRepository.getCurrentUserUid() ?: "WORKER_LOCAL"
        viewModelScope.launch {
            val result = repository.submitTaskCompletion(taskId, afterPhotoPath, notes, uid)
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Failed to submit completion") }
        }
    }

    fun approveTaskCompletion(
        taskId: String,
        reportId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = authRepository.getCurrentUserUid() ?: "COORDINATOR_LOCAL"
        viewModelScope.launch {
            val result = repository.approveTaskCompletion(taskId, reportId, uid, "Municipal Coordinator")
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Failed to approve completion") }
        }
    }

    fun addComment(
        taskId: String,
        text: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = authRepository.getCurrentUserUid() ?: "WORKER_LOCAL"
        viewModelScope.launch {
            val result = repository.addTaskComment(
                taskId = taskId,
                text = text,
                authorUid = uid,
                authorName = "Field Worker",
                authorRole = "FIELD_WORKER"
            )
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Failed to post comment") }
        }
    }
}
