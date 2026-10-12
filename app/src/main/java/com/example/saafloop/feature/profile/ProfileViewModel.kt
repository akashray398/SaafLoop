package com.example.saafloop.feature.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saafloop.core.data.UserPreferencesRepository
import com.example.saafloop.core.util.LocaleManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepository = UserPreferencesRepository(application)

    val displayNameState: StateFlow<String> = prefsRepository.displayName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "Civic Resident"
        )

    val emailState: StateFlow<String> = prefsRepository.email
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    val phoneState: StateFlow<String> = prefsRepository.phone
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    val profilePhotoPathState: StateFlow<String?> = prefsRepository.profilePhotoPath
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val selectedLanguageState: StateFlow<String> = prefsRepository.selectedLanguage
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "en"
        )

    fun updateProfileDetails(name: String, email: String, phone: String) {
        viewModelScope.launch {
            prefsRepository.updateProfileDetails(name, email, phone)
        }
    }

    fun saveProfilePhotoUri(uri: Uri) {
        viewModelScope.launch {
            val savedPath = saveImageToAppStorage(uri)
            if (savedPath != null) {
                prefsRepository.setProfilePhotoPath(savedPath)
            }
        }
    }

    fun removeProfilePhoto() {
        viewModelScope.launch {
            prefsRepository.setProfilePhotoPath(null)
        }
    }

    fun setSelectedLanguage(languageCode: String) {
        viewModelScope.launch {
            prefsRepository.setSelectedLanguage(languageCode)
            LocaleManager.applyLanguage(getApplication(), languageCode)
        }
    }

    private suspend fun saveImageToAppStorage(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val context = getApplication<Application>().applicationContext
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val outputFile = File(context.filesDir, "profile_avatar.jpg")

            FileOutputStream(outputFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()

            if (outputFile.exists() && outputFile.length() > 0) {
                outputFile.absolutePath
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
