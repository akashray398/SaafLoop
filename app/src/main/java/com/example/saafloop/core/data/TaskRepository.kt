package com.example.saafloop.core.data

import android.content.Context
import com.example.saafloop.core.model.AuditEvent
import com.example.saafloop.core.model.CasePriority
import com.example.saafloop.core.model.CaseStatus
import com.example.saafloop.core.model.FieldTask
import com.example.saafloop.core.model.TaskComment
import com.example.saafloop.core.model.TaskStatus
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

interface TaskRepository {
    fun isFirebaseConfigured(): Boolean
    fun observeAssignedTasks(workerUid: String): Flow<List<FieldTask>>
    fun observeCoordinatorTasks(): Flow<List<FieldTask>>
    fun observeTaskDetails(taskId: String): Flow<FieldTask?>
    fun observeTaskComments(taskId: String): Flow<List<TaskComment>>
    suspend fun createAndAssignTask(task: FieldTask): Result<String>
    suspend fun acceptTask(taskId: String, workerUid: String, workerName: String): Result<Unit>
    suspend fun declineTask(taskId: String, reason: String, notes: String, workerUid: String): Result<Unit>
    suspend fun startTask(taskId: String, workerUid: String): Result<Unit>
    suspend fun updateTaskChecklist(taskId: String, checklistJson: String): Result<Unit>
    suspend fun submitTaskCompletion(taskId: String, afterPhotoPath: String?, notes: String, workerUid: String): Result<Unit>
    suspend fun approveTaskCompletion(taskId: String, reportId: String, coordinatorUid: String, coordinatorName: String): Result<Unit>
    suspend fun requestTaskRework(taskId: String, reason: String, notes: String, coordinatorUid: String): Result<Unit>
    suspend fun addTaskComment(taskId: String, text: String, authorUid: String, authorName: String, authorRole: String): Result<Unit>
}

class TaskRepositoryImpl(private val context: Context) : TaskRepository {

    override fun isFirebaseConfigured(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty() && FirebaseFirestore.getInstance().app != null
        } catch (_: Exception) {
            false
        }
    }

    override fun observeAssignedTasks(workerUid: String): Flow<List<FieldTask>> = callbackFlow {
        if (!isFirebaseConfigured()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val query = if (workerUid.isBlank()) {
            firestore.collection("tasks").orderBy("createdAt", Query.Direction.DESCENDING).limit(50)
        } else {
            firestore.collection("tasks").whereEqualTo("assignedToUid", workerUid)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }

            val tasks = snapshot.documents.mapNotNull { doc ->
                try {
                    FieldTask(
                        taskId = doc.getString("taskId") ?: doc.id,
                        reportId = doc.getString("reportId") ?: "",
                        title = doc.getString("title") ?: "Field Cleanup Task",
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: "GARBAGE",
                        priority = try { CasePriority.valueOf(doc.getString("priority") ?: "MEDIUM") } catch (_: Exception) { CasePriority.MEDIUM },
                        status = try { TaskStatus.valueOf(doc.getString("status") ?: "UNASSIGNED") } catch (_: Exception) { TaskStatus.UNASSIGNED },
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        approximateArea = doc.getString("approximateArea") ?: "",
                        assignedToUid = doc.getString("assignedToUid"),
                        assignedToName = doc.getString("assignedToName"),
                        assignedToRole = doc.getString("assignedToRole"),
                        assignedByUid = doc.getString("assignedByUid"),
                        assignedByName = doc.getString("assignedByName"),
                        teamId = doc.getString("teamId"),
                        dueAt = doc.getLong("dueAt"),
                        checklistJson = doc.getString("checklistJson"),
                        beforePhotoPath = doc.getString("beforePhotoPath"),
                        afterPhotoPath = doc.getString("afterPhotoPath"),
                        completionNotes = doc.getString("completionNotes"),
                        rejectionReason = doc.getString("rejectionReason"),
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                    )
                } catch (_: Exception) {
                    null
                }
            }

            trySend(tasks)
        }

        awaitClose { listener.remove() }
    }

    override fun observeCoordinatorTasks(): Flow<List<FieldTask>> = callbackFlow {
        if (!isFirebaseConfigured()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collection("tasks")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val tasks = snapshot.documents.mapNotNull { doc ->
                    try {
                        FieldTask(
                            taskId = doc.getString("taskId") ?: doc.id,
                            reportId = doc.getString("reportId") ?: "",
                            title = doc.getString("title") ?: "Field Cleanup Task",
                            description = doc.getString("description") ?: "",
                            category = doc.getString("category") ?: "GARBAGE",
                            priority = try { CasePriority.valueOf(doc.getString("priority") ?: "MEDIUM") } catch (_: Exception) { CasePriority.MEDIUM },
                            status = try { TaskStatus.valueOf(doc.getString("status") ?: "UNASSIGNED") } catch (_: Exception) { TaskStatus.UNASSIGNED },
                            latitude = doc.getDouble("latitude") ?: 0.0,
                            longitude = doc.getDouble("longitude") ?: 0.0,
                            approximateArea = doc.getString("approximateArea") ?: "",
                            assignedToUid = doc.getString("assignedToUid"),
                            assignedToName = doc.getString("assignedToName"),
                            assignedToRole = doc.getString("assignedToRole"),
                            assignedByUid = doc.getString("assignedByUid"),
                            assignedByName = doc.getString("assignedByName"),
                            teamId = doc.getString("teamId"),
                            dueAt = doc.getLong("dueAt"),
                            checklistJson = doc.getString("checklistJson"),
                            beforePhotoPath = doc.getString("beforePhotoPath"),
                            afterPhotoPath = doc.getString("afterPhotoPath"),
                            completionNotes = doc.getString("completionNotes"),
                            rejectionReason = doc.getString("rejectionReason"),
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                            updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                        )
                    } catch (_: Exception) {
                        null
                    }
                }

                trySend(tasks)
            }

        awaitClose { listener.remove() }
    }

    override fun observeTaskDetails(taskId: String): Flow<FieldTask?> = callbackFlow {
        if (!isFirebaseConfigured() || taskId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collection("tasks").document(taskId)
            .addSnapshotListener { doc, error ->
                if (error != null || doc == null || !doc.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val task = try {
                    FieldTask(
                        taskId = doc.getString("taskId") ?: doc.id,
                        reportId = doc.getString("reportId") ?: "",
                        title = doc.getString("title") ?: "Field Cleanup Task",
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: "GARBAGE",
                        priority = try { CasePriority.valueOf(doc.getString("priority") ?: "MEDIUM") } catch (_: Exception) { CasePriority.MEDIUM },
                        status = try { TaskStatus.valueOf(doc.getString("status") ?: "UNASSIGNED") } catch (_: Exception) { TaskStatus.UNASSIGNED },
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        approximateArea = doc.getString("approximateArea") ?: "",
                        assignedToUid = doc.getString("assignedToUid"),
                        assignedToName = doc.getString("assignedToName"),
                        assignedToRole = doc.getString("assignedToRole"),
                        assignedByUid = doc.getString("assignedByUid"),
                        assignedByName = doc.getString("assignedByName"),
                        teamId = doc.getString("teamId"),
                        dueAt = doc.getLong("dueAt"),
                        checklistJson = doc.getString("checklistJson"),
                        beforePhotoPath = doc.getString("beforePhotoPath"),
                        afterPhotoPath = doc.getString("afterPhotoPath"),
                        completionNotes = doc.getString("completionNotes"),
                        rejectionReason = doc.getString("rejectionReason"),
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                    )
                } catch (_: Exception) {
                    null
                }

                trySend(task)
            }

        awaitClose { listener.remove() }
    }

    override fun observeTaskComments(taskId: String): Flow<List<TaskComment>> = callbackFlow {
        if (!isFirebaseConfigured() || taskId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val firestore = FirebaseFirestore.getInstance()
        val listener = firestore.collection("tasks")
            .document(taskId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val comments = snapshot.documents.mapNotNull { doc ->
                    try {
                        TaskComment(
                            commentId = doc.getString("commentId") ?: doc.id,
                            taskId = doc.getString("taskId") ?: taskId,
                            authorUid = doc.getString("authorUid") ?: "",
                            authorName = doc.getString("authorName") ?: "User",
                            authorRole = doc.getString("authorRole") ?: "FIELD_WORKER",
                            text = doc.getString("text") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        )
                    } catch (_: Exception) {
                        null
                    }
                }

                trySend(comments)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun createAndAssignTask(task: FieldTask): Result<String> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.failure(IllegalStateException("Firebase unconfigured"))

        try {
            val firestore = FirebaseFirestore.getInstance()
            val taskRef = firestore.collection("tasks").document(task.taskId)

            val taskData = hashMapOf(
                "taskId" to task.taskId,
                "reportId" to task.reportId,
                "title" to task.title,
                "description" to task.description,
                "category" to task.category,
                "priority" to task.priority.name,
                "status" to task.status.name,
                "latitude" to task.latitude,
                "longitude" to task.longitude,
                "approximateArea" to task.approximateArea,
                "assignedToUid" to task.assignedToUid,
                "assignedToName" to task.assignedToName,
                "assignedToRole" to task.assignedToRole,
                "assignedByUid" to task.assignedByUid,
                "assignedByName" to task.assignedByName,
                "teamId" to task.teamId,
                "dueAt" to task.dueAt,
                "checklistJson" to task.checklistJson,
                "beforePhotoPath" to task.beforePhotoPath,
                "afterPhotoPath" to null,
                "completionNotes" to null,
                "createdAt" to task.createdAt,
                "updatedAt" to task.updatedAt
            )

            taskRef.set(taskData).await()

            // Also update parent report status to ASSIGNED
            firestore.collection("cases").document(task.reportId).update(
                mapOf(
                    "status" to CaseStatus.ASSIGNED.name,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            Result.success(task.taskId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptTask(taskId: String, workerUid: String, workerName: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.failure(IllegalStateException("Firebase unconfigured"))

        try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("tasks").document(taskId).update(
                mapOf(
                    "status" to TaskStatus.ACCEPTED.name,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun declineTask(taskId: String, reason: String, notes: String, workerUid: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.failure(IllegalStateException("Firebase unconfigured"))

        try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("tasks").document(taskId).update(
                mapOf(
                    "status" to TaskStatus.DECLINED.name,
                    "rejectionReason" to "$reason: $notes",
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startTask(taskId: String, workerUid: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.failure(IllegalStateException("Firebase unconfigured"))

        try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("tasks").document(taskId).update(
                mapOf(
                    "status" to TaskStatus.IN_PROGRESS.name,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTaskChecklist(taskId: String, checklistJson: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.failure(IllegalStateException("Firebase unconfigured"))

        try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("tasks").document(taskId).update(
                mapOf(
                    "checklistJson" to checklistJson,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitTaskCompletion(
        taskId: String,
        afterPhotoPath: String?,
        notes: String,
        workerUid: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.failure(IllegalStateException("Firebase unconfigured"))

        try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("tasks").document(taskId).update(
                mapOf(
                    "status" to TaskStatus.COMPLETION_SUBMITTED.name,
                    "afterPhotoPath" to afterPhotoPath,
                    "completionNotes" to notes,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun approveTaskCompletion(
        taskId: String,
        reportId: String,
        coordinatorUid: String,
        coordinatorName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.failure(IllegalStateException("Firebase unconfigured"))

        try {
            val firestore = FirebaseFirestore.getInstance()

            // Update Task to COMPLETED
            firestore.collection("tasks").document(taskId).update(
                mapOf(
                    "status" to TaskStatus.COMPLETED.name,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            // Update parent report to VERIFIED_CLEAN / RESOLVED
            if (reportId.isNotBlank()) {
                firestore.collection("cases").document(reportId).update(
                    mapOf(
                        "status" to CaseStatus.VERIFIED_CLEAN.name,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestTaskRework(
        taskId: String,
        reason: String,
        notes: String,
        coordinatorUid: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured()) return@withContext Result.failure(IllegalStateException("Firebase unconfigured"))

        try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("tasks").document(taskId).update(
                mapOf(
                    "status" to TaskStatus.REWORK_REQUESTED.name,
                    "rejectionReason" to "$reason: $notes",
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addTaskComment(
        taskId: String,
        text: String,
        authorUid: String,
        authorName: String,
        authorRole: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseConfigured() || text.isBlank()) return@withContext Result.failure(IllegalArgumentException("Invalid text"))

        try {
            val firestore = FirebaseFirestore.getInstance()
            val commentRef = firestore.collection("tasks").document(taskId).collection("comments").document()

            val commentData = hashMapOf(
                "commentId" to commentRef.id,
                "taskId" to taskId,
                "authorUid" to authorUid,
                "authorName" to authorName,
                "authorRole" to authorRole,
                "text" to text,
                "timestamp" to System.currentTimeMillis()
            )

            commentRef.set(commentData).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
