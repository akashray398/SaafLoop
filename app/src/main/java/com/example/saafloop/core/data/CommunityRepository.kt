package com.example.saafloop.core.data

import android.content.Context
import com.example.saafloop.core.model.ActivityCategory
import com.example.saafloop.core.model.ActivityParticipant
import com.example.saafloop.core.model.ActivityStatus
import com.example.saafloop.core.model.CommunityActivity
import com.example.saafloop.core.model.CommunityBadge
import com.example.saafloop.core.model.OrgVerificationStatus
import com.example.saafloop.core.model.OrganizationProfile
import com.example.saafloop.core.model.ParticipantStatus
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

interface CommunityRepository {
    fun isFirebaseConfigured(): Boolean
    fun observeNearbyActivities(): Flow<List<CommunityActivity>>
    fun observeActivityDetails(activityId: String): Flow<CommunityActivity?>
    fun observeActivityParticipants(activityId: String): Flow<List<ActivityParticipant>>
    fun observeUserContributions(userUid: String): Flow<List<ActivityParticipant>>
    fun observeUserBadges(userUid: String): Flow<List<CommunityBadge>>
    fun observeOrganizations(): Flow<List<OrganizationProfile>>
    suspend fun createActivity(activity: CommunityActivity): Result<String>
    suspend fun approveActivity(activityId: String, coordinatorUid: String): Result<Unit>
    suspend fun joinActivity(activityId: String, userUid: String, userName: String): Result<ParticipantStatus>
    suspend fun leaveActivity(activityId: String, userUid: String): Result<Unit>
    suspend fun checkInParticipant(activityId: String, userUid: String, checkInToken: String): Result<Unit>
}

class CommunityRepositoryImpl(private val context: Context) : CommunityRepository {

    override fun isFirebaseConfigured(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty() && FirebaseFirestore.getInstance().app != null
        } catch (_: Exception) {
            false
        }
    }

    override fun observeNearbyActivities(): Flow<List<CommunityActivity>> = callbackFlow {
        if (!isFirebaseConfigured()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collection("community_activities")
            .orderBy("startDate", Query.Direction.ASCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val activities = snapshot.documents.mapNotNull { doc ->
                    try {
                        CommunityActivity(
                            activityId = doc.getString("activityId") ?: doc.id,
                            title = doc.getString("title") ?: "Community Drive",
                            category = try { ActivityCategory.valueOf(doc.getString("category") ?: "CLEANUP_DRIVE") } catch (_: Exception) { ActivityCategory.CLEANUP_DRIVE },
                            description = doc.getString("description") ?: "",
                            approximateArea = doc.getString("approximateArea") ?: "",
                            latitude = doc.getDouble("latitude") ?: 0.0,
                            longitude = doc.getDouble("longitude") ?: 0.0,
                            startDate = doc.getLong("startDate") ?: System.currentTimeMillis(),
                            startTime = doc.getString("startTime") ?: "8:00 AM",
                            expectedDurationHours = doc.getLong("expectedDurationHours")?.toInt() ?: 2,
                            maxParticipants = doc.getLong("maxParticipants")?.toInt() ?: 20,
                            currentParticipantsCount = doc.getLong("currentParticipantsCount")?.toInt() ?: 0,
                            organizerUid = doc.getString("organizerUid") ?: "",
                            organizerName = doc.getString("organizerName") ?: "SaafLoop Community",
                            organizerType = doc.getString("organizerType") ?: "NGO",
                            status = try { ActivityStatus.valueOf(doc.getString("status") ?: "PUBLISHED") } catch (_: Exception) { ActivityStatus.PUBLISHED },
                            requiredEquipment = doc.getString("requiredEquipment"),
                            safetyInstructions = doc.getString("safetyInstructions"),
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                            updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                        )
                    } catch (_: Exception) {
                        null
                    }
                }

                trySend(activities)
            }

        awaitClose { listener.remove() }
    }

    override fun observeActivityDetails(activityId: String): Flow<CommunityActivity?> = callbackFlow {
        if (!isFirebaseConfigured() || activityId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collection("community_activities").document(activityId)
            .addSnapshotListener { doc, error ->
                if (error != null || doc == null || !doc.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val activity = try {
                    CommunityActivity(
                        activityId = doc.getString("activityId") ?: doc.id,
                        title = doc.getString("title") ?: "Community Drive",
                        category = try { ActivityCategory.valueOf(doc.getString("category") ?: "CLEANUP_DRIVE") } catch (_: Exception) { ActivityCategory.CLEANUP_DRIVE },
                        description = doc.getString("description") ?: "",
                        approximateArea = doc.getString("approximateArea") ?: "",
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        startDate = doc.getLong("startDate") ?: System.currentTimeMillis(),
                        startTime = doc.getString("startTime") ?: "8:00 AM",
                        expectedDurationHours = doc.getLong("expectedDurationHours")?.toInt() ?: 2,
                        maxParticipants = doc.getLong("maxParticipants")?.toInt() ?: 20,
                        currentParticipantsCount = doc.getLong("currentParticipantsCount")?.toInt() ?: 0,
                        organizerUid = doc.getString("organizerUid") ?: "",
                        organizerName = doc.getString("organizerName") ?: "SaafLoop Community",
                        organizerType = doc.getString("organizerType") ?: "NGO",
                        status = try { ActivityStatus.valueOf(doc.getString("status") ?: "PUBLISHED") } catch (_: Exception) { ActivityStatus.PUBLISHED },
                        requiredEquipment = doc.getString("requiredEquipment"),
                        safetyInstructions = doc.getString("safetyInstructions"),
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                    )
                } catch (_: Exception) {
                    null
                }

                trySend(activity)
            }

        awaitClose { listener.remove() }
    }

    override fun observeActivityParticipants(activityId: String): Flow<List<ActivityParticipant>> = callbackFlow {
        if (!isFirebaseConfigured() || activityId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collection("community_activities")
            .document(activityId)
            .collection("participants")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val participants = snapshot.documents.mapNotNull { doc ->
                    try {
                        ActivityParticipant(
                            participantId = doc.getString("participantId") ?: doc.id,
                            activityId = doc.getString("activityId") ?: activityId,
                            userUid = doc.getString("userUid") ?: "",
                            userName = doc.getString("userName") ?: "Volunteer",
                            status = try { ParticipantStatus.valueOf(doc.getString("status") ?: "JOINED") } catch (_: Exception) { ParticipantStatus.JOINED },
                            checkInTime = doc.getLong("checkInTime"),
                            checkInToken = doc.getString("checkInToken"),
                            joinedAt = doc.getLong("joinedAt") ?: System.currentTimeMillis()
                        )
                    } catch (_: Exception) {
                        null
                    }
                }

                trySend(participants)
            }

        awaitClose { listener.remove() }
    }

    override fun observeUserContributions(userUid: String): Flow<List<ActivityParticipant>> = callbackFlow {
        if (!isFirebaseConfigured() || userUid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collectionGroup("participants")
            .whereEqualTo("userUid", userUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        ActivityParticipant(
                            participantId = doc.getString("participantId") ?: doc.id,
                            activityId = doc.getString("activityId") ?: "",
                            userUid = doc.getString("userUid") ?: userUid,
                            userName = doc.getString("userName") ?: "Volunteer",
                            status = try { ParticipantStatus.valueOf(doc.getString("status") ?: "JOINED") } catch (_: Exception) { ParticipantStatus.JOINED },
                            checkInTime = doc.getLong("checkInTime"),
                            checkInToken = doc.getString("checkInToken"),
                            joinedAt = doc.getLong("joinedAt") ?: System.currentTimeMillis()
                        )
                    } catch (_: Exception) {
                        null
                    }
                }

                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    override fun observeUserBadges(userUid: String): Flow<List<CommunityBadge>> = callbackFlow {
        val badges = listOf(
            CommunityBadge("b_1", "Community Contributor", "Participated in 1 verified cleanup drive", 1, System.currentTimeMillis()),
            CommunityBadge("b_2", "Cleanup Champion", "Participated in 5 verified cleanup drives", 5, null),
            CommunityBadge("b_3", "Neighborhood Helper", "Supported 3 local reports with evidence", 3, System.currentTimeMillis()),
            CommunityBadge("b_4", "Verified Volunteer", "Completed verified volunteer orientation", 1, System.currentTimeMillis())
        )
        trySend(badges)
        close()
    }

    override fun observeOrganizations(): Flow<List<OrganizationProfile>> = callbackFlow {
        if (!isFirebaseConfigured()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collection("organizations")
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val orgs = snapshot.documents.mapNotNull { doc ->
                    try {
                        OrganizationProfile(
                            orgId = doc.getString("orgId") ?: doc.id,
                            name = doc.getString("name") ?: "Community Organization",
                            type = doc.getString("type") ?: "NGO",
                            description = doc.getString("description") ?: "",
                            verificationStatus = try { OrgVerificationStatus.valueOf(doc.getString("verificationStatus") ?: "VERIFIED") } catch (_: Exception) { OrgVerificationStatus.VERIFIED },
                            activeMembersCount = doc.getLong("activeMembersCount")?.toInt() ?: 12,
                            completedDrivesCount = doc.getLong("completedDrivesCount")?.toInt() ?: 6,
                            logoUrl = doc.getString("logoUrl"),
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    } catch (_: Exception) {
                        null
                    }
                }

                trySend(orgs)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun createActivity(activity: CommunityActivity): Result<String> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.failure(IllegalStateException("Firebase unconfigured"))

        try {
            val firestore = FirebaseFirestore.getInstance()
            val docRef = firestore.collection("community_activities").document(activity.activityId)

            val data = hashMapOf(
                "activityId" to activity.activityId,
                "title" to activity.title,
                "category" to activity.category.name,
                "description" to activity.description,
                "approximateArea" to activity.approximateArea,
                "latitude" to activity.latitude,
                "longitude" to activity.longitude,
                "startDate" to activity.startDate,
                "startTime" to activity.startTime,
                "expectedDurationHours" to activity.expectedDurationHours,
                "maxParticipants" to activity.maxParticipants,
                "currentParticipantsCount" to 0,
                "organizerUid" to activity.organizerUid,
                "organizerName" to activity.organizerName,
                "organizerType" to activity.organizerType,
                "status" to ActivityStatus.PENDING_APPROVAL.name,
                "requiredEquipment" to activity.requiredEquipment,
                "safetyInstructions" to activity.safetyInstructions,
                "createdAt" to activity.createdAt,
                "updatedAt" to activity.updatedAt
            )

            docRef.set(data).await()
            Result.success(activity.activityId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun approveActivity(activityId: String, coordinatorUid: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.failure(IllegalStateException("Firebase unconfigured"))

        try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("community_activities").document(activityId).update(
                mapOf(
                    "status" to ActivityStatus.PUBLISHED.name,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinActivity(
        activityId: String,
        userUid: String,
        userName: String
    ): Result<ParticipantStatus> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.failure(IllegalStateException("Firebase unconfigured"))

        try {
            val firestore = FirebaseFirestore.getInstance()
            val actRef = firestore.collection("community_activities").document(activityId)
            val partRef = actRef.collection("participants").document("part_${userUid}_$activityId")

            var finalStatus = ParticipantStatus.JOINED

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(actRef)
                val maxPart = snapshot.getLong("maxParticipants")?.toInt() ?: 20
                val currentPart = snapshot.getLong("currentParticipantsCount")?.toInt() ?: 0

                finalStatus = if (currentPart >= maxPart) ParticipantStatus.WAITLISTED else ParticipantStatus.JOINED

                val partData = hashMapOf(
                    "participantId" to "part_${userUid}_$activityId",
                    "activityId" to activityId,
                    "userUid" to userUid,
                    "userName" to userName,
                    "status" to finalStatus.name,
                    "joinedAt" to System.currentTimeMillis()
                )

                transaction.set(partRef, partData)

                if (finalStatus == ParticipantStatus.JOINED) {
                    transaction.update(actRef, "currentParticipantsCount", FieldValue.increment(1))
                }
            }.await()

            Result.success(finalStatus)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveActivity(activityId: String, userUid: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.failure(IllegalStateException("Firebase unconfigured"))

        try {
            val firestore = FirebaseFirestore.getInstance()
            val actRef = firestore.collection("community_activities").document(activityId)
            val partRef = actRef.collection("participants").document("part_${userUid}_$activityId")

            firestore.runTransaction { transaction ->
                val partSnap = transaction.get(partRef)
                if (partSnap.exists()) {
                    val statusStr = partSnap.getString("status")
                    transaction.delete(partRef)
                    if (statusStr == ParticipantStatus.JOINED.name) {
                        transaction.update(actRef, "currentParticipantsCount", FieldValue.increment(-1))
                    }
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkInParticipant(
        activityId: String,
        userUid: String,
        checkInToken: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.failure(IllegalStateException("Firebase unconfigured"))

        try {
            val firestore = FirebaseFirestore.getInstance()
            val partRef = firestore.collection("community_activities")
                .document(activityId)
                .collection("participants")
                .document("part_${userUid}_$activityId")

            partRef.update(
                mapOf(
                    "status" to ParticipantStatus.ATTENDED.name,
                    "checkInTime" to System.currentTimeMillis(),
                    "checkInToken" to checkInToken
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
