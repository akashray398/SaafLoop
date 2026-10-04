package com.example.saafloop.feature.report

import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.saafloop.core.data.AuthRepository
import com.example.saafloop.core.data.AuthRepositoryImpl
import com.example.saafloop.core.data.CivicAIRepository
import com.example.saafloop.core.data.CivicAIRepositoryImpl
import com.example.saafloop.core.data.DraftRepository
import com.example.saafloop.core.data.DraftRepositoryImpl
import com.example.saafloop.core.data.DuplicateDetectionEngine
import com.example.saafloop.core.data.RemoteCaseRepository
import com.example.saafloop.core.data.RemoteCaseRepositoryImpl
import com.example.saafloop.core.domain.AnalyzeCivicReportUseCase
import com.example.saafloop.core.model.CaseReport
import com.example.saafloop.core.model.DuplicateMatchItem
import com.example.saafloop.core.model.ReportAIAnalysis
import com.example.saafloop.core.worker.ReportUploadWorker
import com.example.saafloop.feature.report.model.ReportFormStage
import com.example.saafloop.feature.report.model.ReportFormState
import com.example.saafloop.feature.report.model.SubmissionState
import com.example.saafloop.feature.report.model.WasteCategory
import com.example.saafloop.feature.report.model.WasteSizeEstimate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class ReportWasteViewModel(application: Application) : AndroidViewModel(application) {

    private val draftRepository: DraftRepository = DraftRepositoryImpl(application)
    private val authRepository: AuthRepository = AuthRepositoryImpl(application)
    private val remoteCaseRepository: RemoteCaseRepository = RemoteCaseRepositoryImpl(application)
    private val aiRepository: CivicAIRepository = CivicAIRepositoryImpl(application)
    private val analyzeReportUseCase = AnalyzeCivicReportUseCase(aiRepository)

    private val _formState = MutableStateFlow(ReportFormState())
    val formState: StateFlow<ReportFormState> = _formState.asStateFlow()

    private val _submissionState = MutableStateFlow<SubmissionState>(SubmissionState.Draft)
    val submissionState: StateFlow<SubmissionState> = _submissionState.asStateFlow()

    private val _aiAnalysisState = MutableStateFlow<ReportAIAnalysis?>(null)
    val aiAnalysisState: StateFlow<ReportAIAnalysis?> = _aiAnalysisState.asStateFlow()

    val publicCasesState: StateFlow<List<CaseReport>> = remoteCaseRepository
        .observePublicCases()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val duplicateMatchesState: StateFlow<List<DuplicateMatchItem>> = combine(
        _formState,
        publicCasesState
    ) { form, publicCases ->
        if (form.category != null && (form.latitude != 0.0 || form.longitude != 0.0)) {
            DuplicateDetectionEngine.findNearbyMatches(
                targetLat = form.latitude,
                targetLng = form.longitude,
                targetCategory = form.category.name,
                candidateCases = publicCases
            )
        } else {
            emptyList()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private var activeDraftId: String? = null
    private var tempCameraPhotoUri: Uri? = null

    /** Triggers AI report analysis on the current photo & context. */
    fun triggerAIAnalysis() {
        val current = _formState.value
        viewModelScope.launch {
            val result = analyzeReportUseCase(
                photoUri = current.photoUri,
                photoPath = current.photoUri?.path,
                userCategory = current.category?.name,
                userDescription = current.description,
                locationName = current.locationName,
                latitude = current.latitude,
                longitude = current.longitude
            )
            result.onSuccess { analysis ->
                _aiAnalysisState.value = analysis
                if (current.category == null && !analysis.suggestedCategory.isNullOrBlank()) {
                    val matched = try { WasteCategory.valueOf(analysis.suggestedCategory) } catch (_: Exception) { null }
                    if (matched != null) {
                        setCategory(matched)
                    }
                }
            }
        }
    }

    fun applyAISuggestedCategory(catName: String) {
        val matched = try { WasteCategory.valueOf(catName) } catch (_: Exception) { null }
        if (matched != null) {
            setCategory(matched)
        }
    }

    fun applyAISuggestedDescription(desc: String) {
        setDescription(desc)
    }

    /** Loads an existing draft from Room if editing a saved draft. */
    fun loadDraft(draftId: String) {
        if (activeDraftId == draftId) return
        activeDraftId = draftId

        viewModelScope.launch {
            val result = draftRepository.getDraft(draftId)
            val draft = result.getOrNull()
            if (draft != null) {
                val photoUri = draft.photoPath?.let { path ->
                    val file = File(path)
                    if (file.exists()) Uri.fromFile(file) else null
                }

                val category = draft.category?.let { catName ->
                    try { WasteCategory.valueOf(catName) } catch (_: Exception) { null }
                }

                val sizeEstimate = draft.sizeEstimate?.let { sizeName ->
                    try { WasteSizeEstimate.valueOf(sizeName) } catch (_: Exception) { null }
                }

                _formState.value = ReportFormState(
                    currentStage = ReportFormStage.DETAILS_AND_LOCATION,
                    photoUri = photoUri,
                    category = category,
                    sizeEstimate = sizeEstimate,
                    description = draft.description,
                    accessNote = draft.accessNote,
                    latitude = if (draft.latitude != 0.0) draft.latitude else 28.6139,
                    longitude = if (draft.longitude != 0.0) draft.longitude else 77.2090,
                    locationName = if (draft.locationName.isNotBlank()) draft.locationName else "Central District",
                    isHazardousSuspected = draft.isHazardousSuspected
                )

                triggerAIAnalysis()
            }
        }
    }

    /** Saves current form state locally as a draft to Room and private photo storage. */
    fun saveAsDraft(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = draftRepository.saveDraft(
                formState = _formState.value,
                draftId = activeDraftId
            )
            result.onSuccess { savedId ->
                activeDraftId = savedId
                onResult(true, "Draft saved locally")
            }.onFailure { exception ->
                onResult(false, exception.message ?: "Failed to save draft")
            }
        }
    }

    /** Explicit report submission handler. */
    fun submitReport(
        previousCaseId: String? = null,
        onRequireAuth: () -> Unit,
        onSuccessConfirmed: (String) -> Unit
    ) {
        val currentUserUid = authRepository.getCurrentUserUid()
        if (currentUserUid.isNullOrBlank()) {
            onRequireAuth()
            return
        }

        viewModelScope.launch {
            _submissionState.value = SubmissionState.UploadingPhoto

            val saveResult = draftRepository.saveDraft(formState.value, activeDraftId)
            val savedDraftId = saveResult.getOrNull()
                ?: run {
                    _submissionState.value = SubmissionState.NeedsAttention("Unable to save local draft photo")
                    return@launch
                }
            activeDraftId = savedDraftId

            val draftEntity = draftRepository.getDraft(savedDraftId).getOrNull()
                ?: run {
                    _submissionState.value = SubmissionState.NeedsAttention("Draft not found in database")
                    return@launch
                }

            _submissionState.value = SubmissionState.FinalisingCase

            val submitResult = remoteCaseRepository.submitCase(draftEntity, previousCaseId)
            submitResult.onSuccess { serverCaseId ->
                _submissionState.value = SubmissionState.SubmittedConfirmed(serverCaseId)
                draftRepository.deleteDraft(savedDraftId)
                onSuccessConfirmed(serverCaseId)
            }.onFailure { _ ->
                val workManager = WorkManager.getInstance(getApplication())
                val uploadData = Data.Builder()
                    .putString(ReportUploadWorker.KEY_DRAFT_ID, savedDraftId)
                    .build()

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val uploadRequest = OneTimeWorkRequestBuilder<ReportUploadWorker>()
                    .setInputData(uploadData)
                    .setConstraints(constraints)
                    .addTag("${ReportUploadWorker.WORK_TAG_PREFIX}$savedDraftId")
                    .build()

                workManager.enqueue(uploadRequest)
                _submissionState.value = SubmissionState.QueuedForUpload
            }
        }
    }

    /** Confirms an existing nearby case ("This looks like the same waste"). */
    fun confirmExistingCase(
        caseId: String,
        onRequireAuth: () -> Unit,
        onSuccessConfirmed: (String) -> Unit
    ) {
        val currentUserUid = authRepository.getCurrentUserUid()
        if (currentUserUid.isNullOrBlank()) {
            onRequireAuth()
            return
        }

        viewModelScope.launch {
            _submissionState.value = SubmissionState.FinalisingCase

            val saveResult = draftRepository.saveDraft(formState.value, activeDraftId)
            val savedDraftId = saveResult.getOrNull()
                ?: run {
                    _submissionState.value = SubmissionState.NeedsAttention("Unable to save local draft photo")
                    return@launch
                }
            activeDraftId = savedDraftId

            val draftEntity = draftRepository.getDraft(savedDraftId).getOrNull()
                ?: run {
                    _submissionState.value = SubmissionState.NeedsAttention("Draft not found in database")
                    return@launch
                }

            val confirmResult = remoteCaseRepository.confirmExistingCase(caseId, draftEntity)
            confirmResult.onSuccess { confirmedCaseId ->
                _submissionState.value = SubmissionState.SubmittedConfirmed(confirmedCaseId)
                draftRepository.deleteDraft(savedDraftId)
                onSuccessConfirmed(confirmedCaseId)
            }.onFailure { e ->
                _submissionState.value = SubmissionState.NeedsAttention(e.message ?: "Failed to confirm case")
            }
        }
    }

    /** Creates a temporary content URI using FileProvider for capturing camera photos. */
    fun createTempPhotoUri(): Uri {
        val context = getApplication<Application>()
        val photoFile = File.createTempFile(
            "waste_report_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
        )
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        tempCameraPhotoUri = uri
        return uri
    }

    fun getTempCameraPhotoUri(): Uri? = tempCameraPhotoUri

    fun setPhotoUri(uri: Uri?) {
        _formState.value = _formState.value.copy(
            photoUri = uri,
            validationTriggered = false
        )
        triggerAIAnalysis()
    }

    fun removePhoto() {
        _formState.value = _formState.value.copy(photoUri = null)
        _aiAnalysisState.value = null
    }

    fun setCategory(category: WasteCategory) {
        _formState.value = _formState.value.copy(
            category = category,
            validationTriggered = false
        )
    }

    fun setSizeEstimate(size: WasteSizeEstimate?) {
        _formState.value = _formState.value.copy(sizeEstimate = size)
    }

    fun setDescription(desc: String) {
        val capped = if (desc.length > 250) desc.take(250) else desc
        _formState.value = _formState.value.copy(description = capped)
    }

    fun setAccessNote(note: String) {
        val capped = if (note.length > 150) note.take(150) else note
        _formState.value = _formState.value.copy(accessNote = capped)
    }

    fun setHazardousSuspected(isHazardous: Boolean) {
        _formState.value = _formState.value.copy(isHazardousSuspected = isHazardous)
    }

    fun updateLocation(lat: Double, lng: Double, name: String) {
        _formState.value = _formState.value.copy(
            latitude = lat,
            longitude = lng,
            locationName = name,
            validationTriggered = false
        )
    }

    fun goToStage(stage: ReportFormStage) {
        _formState.value = _formState.value.copy(currentStage = stage)
        triggerAIAnalysis()
    }

    fun nextStage(): Boolean {
        val current = _formState.value
        when (current.currentStage) {
            ReportFormStage.PHOTO_INPUT -> {
                if (!current.isPhotoValid) {
                    _formState.value = current.copy(validationTriggered = true)
                    return false
                }
                _formState.value = current.copy(
                    currentStage = ReportFormStage.DETAILS_AND_LOCATION,
                    validationTriggered = false
                )
                triggerAIAnalysis()
                return true
            }

            ReportFormStage.DETAILS_AND_LOCATION -> {
                if (!current.isCategoryValid || !current.isLocationValid) {
                    _formState.value = current.copy(validationTriggered = true)
                    return false
                }
                _formState.value = current.copy(
                    currentStage = ReportFormStage.REVIEW,
                    validationTriggered = false
                )
                triggerAIAnalysis()
                return true
            }

            ReportFormStage.REVIEW -> {
                return current.isValid
            }
        }
    }

    fun prevStage() {
        val current = _formState.value
        when (current.currentStage) {
            ReportFormStage.DETAILS_AND_LOCATION -> {
                _formState.value = current.copy(currentStage = ReportFormStage.PHOTO_INPUT)
            }

            ReportFormStage.REVIEW -> {
                _formState.value = current.copy(currentStage = ReportFormStage.DETAILS_AND_LOCATION)
            }

            ReportFormStage.PHOTO_INPUT -> {}
        }
    }

    fun appendSpeechText(recognizedText: String) {
        if (recognizedText.isNotBlank()) {
            val currentDesc = _formState.value.description
            val newDesc = if (currentDesc.isBlank()) recognizedText else "$currentDesc $recognizedText"
            setDescription(newDesc)
        }
    }
}
