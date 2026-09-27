package com.example.saafloop.feature.activity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saafloop.core.data.AuthRepository
import com.example.saafloop.core.data.AuthRepositoryImpl
import com.example.saafloop.core.data.DraftRepository
import com.example.saafloop.core.data.DraftRepositoryImpl
import com.example.saafloop.core.data.RemoteCaseRepository
import com.example.saafloop.core.data.RemoteCaseRepositoryImpl
import com.example.saafloop.core.database.entity.ReportDraftEntity
import com.example.saafloop.core.model.CaseReport
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val draftRepository: DraftRepository = DraftRepositoryImpl(application)
    private val authRepository: AuthRepository = AuthRepositoryImpl(application)
    private val remoteCaseRepository: RemoteCaseRepository = RemoteCaseRepositoryImpl(application)

    val draftsState: StateFlow<List<ReportDraftEntity>> = draftRepository.observeDrafts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val submittedCasesState: StateFlow<List<CaseReport>> = remoteCaseRepository
        .observeUserCases(authRepository.getCurrentUserUid() ?: "")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun deleteDraft(draftId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            draftRepository.deleteDraft(draftId)
            onComplete()
        }
    }
}
