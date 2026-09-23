package com.example.saafloop.feature.activity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saafloop.core.data.DraftRepository
import com.example.saafloop.core.data.DraftRepositoryImpl
import com.example.saafloop.core.database.entity.ReportDraftEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DraftRepository = DraftRepositoryImpl(application)

    val draftsState: StateFlow<List<ReportDraftEntity>> = repository.observeDrafts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun deleteDraft(draftId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteDraft(draftId)
            onComplete()
        }
    }
}
