package com.example.saafloop.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "saafloop_user_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_GUEST_SESSION_ACTIVE = booleanPreferencesKey("guest_session_active")
        private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_PHONE = stringPreferencesKey("phone")
        private val KEY_PROFILE_PHOTO_PATH = stringPreferencesKey("profile_photo_path")
        private val KEY_SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
    }

    /** Stream indicating whether the first-launch onboarding has been finished or skipped. */
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    /** Stream indicating whether a local guest session is currently active. */
    val isGuestSessionActive: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_GUEST_SESSION_ACTIVE] ?: false
    }

    /** Stream mapping current local state to [UserAccessState]. */
    val userAccessState: Flow<UserAccessState> = context.dataStore.data.map { preferences ->
        val isGuest = preferences[KEY_GUEST_SESSION_ACTIVE] ?: false
        val name = preferences[KEY_DISPLAY_NAME] ?: "Guest Resident"
        if (isGuest) {
            UserAccessState.Guest
        } else {
            UserAccessState.AuthenticatedResident(userId = "user_resident", displayName = name)
        }
    }

    val displayName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_DISPLAY_NAME] ?: "Civic Resident"
    }

    val email: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_EMAIL] ?: ""
    }

    val phone: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_PHONE] ?: ""
    }

    val profilePhotoPath: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_PROFILE_PHOTO_PATH]
    }

    val selectedLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_LANGUAGE] ?: "en"
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setGuestSessionActive(active: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_GUEST_SESSION_ACTIVE] = active
        }
    }

    suspend fun exitGuestMode() {
        context.dataStore.edit { preferences ->
            preferences[KEY_GUEST_SESSION_ACTIVE] = false
        }
    }

    suspend fun updateProfileDetails(name: String, emailStr: String, phoneStr: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DISPLAY_NAME] = name
            preferences[KEY_EMAIL] = emailStr
            preferences[KEY_PHONE] = phoneStr
        }
    }

    suspend fun setProfilePhotoPath(photoPath: String?) {
        context.dataStore.edit { preferences ->
            if (photoPath != null) {
                preferences[KEY_PROFILE_PHOTO_PATH] = photoPath
            } else {
                preferences.remove(KEY_PROFILE_PHOTO_PATH)
            }
        }
    }

    suspend fun setSelectedLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_LANGUAGE] = languageCode
        }
    }
}
