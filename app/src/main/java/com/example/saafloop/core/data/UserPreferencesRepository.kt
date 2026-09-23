package com.example.saafloop.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "saafloop_user_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_GUEST_SESSION_ACTIVE = booleanPreferencesKey("guest_session_active")
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
        if (isGuest) {
            UserAccessState.Guest
        } else {
            UserAccessState.SignedOut
        }
    }

    /** Persists onboarding completion or skip preference. */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    /** Starts or ends a guest session locally. */
    suspend fun setGuestSessionActive(active: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_GUEST_SESSION_ACTIVE] = active
        }
    }

    /** Exits guest mode without clearing the onboarding completion flag. */
    suspend fun exitGuestMode() {
        context.dataStore.edit { preferences ->
            preferences[KEY_GUEST_SESSION_ACTIVE] = false
        }
    }
}
