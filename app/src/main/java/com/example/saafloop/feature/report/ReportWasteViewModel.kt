package com.example.saafloop.feature.report

import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saafloop.core.data.DraftRepository
import com.example.saafloop.core.data.DraftRepositoryImpl
import com.example.saafloop.feature.report.model.ReportFormStage
import com.example.saafloop.feature.report.model.ReportFormState
import com.example.saafloop.feature.report.model.WasteCategory
import com.example.saafloop.feature.report.model.WasteSizeEstimate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class ReportWasteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DraftRepository = DraftRepositoryImpl(application)

    private val _formState = MutableStateFlow(ReportFormState())
    val formState: StateFlow<ReportFormState> = _formState.asStateFlow()

    private var activeDraftId: String? = null
    private var tempCameraPhotoUri: Uri? = null

    /** Loads an existing draft from Room if editing a saved draft. */
    fun loadDraft(draftId: String) {
        if (activeDraftId == draftId) return
        activeDraftId = draftId

        viewModelScope.launch {
            val result = repository.getDraft(draftId)
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
            }
        }
    }

    /** Saves current form state locally as a draft to Room and private photo storage. */
    fun saveAsDraft(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.saveDraft(
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
    }

    fun removePhoto() {
        _formState.value = _formState.value.copy(photoUri = null)
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
