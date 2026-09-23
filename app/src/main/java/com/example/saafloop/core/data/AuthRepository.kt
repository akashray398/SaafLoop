package com.example.saafloop.core.data

import android.content.Context
import com.example.saafloop.core.model.ResidentProfile
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    val userAccessState: Flow<UserAccessState>
    fun isFirebaseConfigured(): Boolean
    suspend fun signInWithEmail(email: String, password: String): Result<ResidentProfile>
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<ResidentProfile>
    suspend fun signOut(): Result<Unit>
    fun getCurrentUserUid(): String?
}

class AuthRepositoryImpl(private val context: Context) : AuthRepository {

    private val prefsRepository = UserPreferencesRepository(context)

    override fun isFirebaseConfigured(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty() && FirebaseAuth.getInstance().app != null
        } catch (_: Exception) {
            false
        }
    }

    private val firebaseAuthStateFlow: Flow<UserAccessState?> = callbackFlow {
        if (!isFirebaseConfigured()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                trySend(
                    UserAccessState.AuthenticatedResident(
                        userId = user.uid,
                        displayName = user.displayName ?: user.email ?: "Resident"
                    )
                )
            } else {
                trySend(null)
            }
        }

        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override val userAccessState: Flow<UserAccessState> = combine(
        firebaseAuthStateFlow,
        prefsRepository.isGuestSessionActive
    ) { authState, isGuest ->
        when {
            authState != null -> authState
            isGuest -> UserAccessState.Guest
            else -> UserAccessState.SignedOut
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<ResidentProfile> {
        if (!isFirebaseConfigured()) {
            return Result.failure(IllegalStateException("Firebase Auth is unconfigured in local environment. Please follow firebase_setup_guide.artifact.md"))
        }

        return try {
            val auth = FirebaseAuth.getInstance()
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: return Result.failure(IllegalStateException("Sign in failed: Null user"))

            // Disable guest mode upon successful sign-in
            prefsRepository.setGuestSessionActive(false)

            Result.success(
                ResidentProfile(
                    uid = user.uid,
                    email = user.email,
                    displayName = user.displayName ?: user.email
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<ResidentProfile> {
        if (!isFirebaseConfigured()) {
            return Result.failure(IllegalStateException("Firebase Auth is unconfigured in local environment. Please follow firebase_setup_guide.artifact.md"))
        }

        return try {
            val auth = FirebaseAuth.getInstance()
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: return Result.failure(IllegalStateException("Sign up failed: Null user"))

            // Update user display name in Firebase Auth
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()

            // Save resident profile to Firestore /users/{uid}
            val firestore = FirebaseFirestore.getInstance()
            val residentProfile = ResidentProfile(
                uid = user.uid,
                email = user.email,
                displayName = displayName
            )

            firestore.collection("users")
                .document(user.uid)
                .set(residentProfile)
                .await()

            // Disable guest mode upon successful sign-up
            prefsRepository.setGuestSessionActive(false)

            Result.success(residentProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            if (isFirebaseConfigured()) {
                FirebaseAuth.getInstance().signOut()
            }
            prefsRepository.exitGuestMode()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUserUid(): String? {
        return if (isFirebaseConfigured()) FirebaseAuth.getInstance().currentUser?.uid else null
    }
}
