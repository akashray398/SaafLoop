package com.example.saafloop.feature.fieldops

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.saafloop.core.model.TaskChecklistItem
import com.example.saafloop.core.model.TaskComment
import com.example.saafloop.core.model.TaskStatus
import com.example.saafloop.core.ui.MapLibreMapView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldTaskDetailScreen(
    taskId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    taskViewModel: TaskViewModel = viewModel()
) {
    val taskDetail by taskViewModel.activeTaskDetailState.collectAsState()
    val comments by taskViewModel.activeTaskCommentsState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(taskId) {
        taskViewModel.selectTask(taskId)
    }

    var showDeclineDialog by remember { mutableStateOf(false) }
    var declineReason by remember { mutableStateOf("Too far away") }
    var declineNotes by remember { mutableStateOf("") }

    var afterPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var completionNotes by remember { mutableStateOf("") }
    var showSubmitConfirmation by remember { mutableStateOf(false) }

    var commentText by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            afterPhotoUri = uri
        }
    }

    val checklistItems = remember {
        mutableStateListOf(
            TaskChecklistItem(1, "Locate reported waste site"),
            TaskChecklistItem(2, "Assess waste volume & safety"),
            TaskChecklistItem(3, "Collect & bag waste"),
            TaskChecklistItem(4, "Clean surrounding area"),
            TaskChecklistItem(5, "Dispose at designated facility"),
            TaskChecklistItem(6, "Capture completion photo proof")
        )
    }

    val task = taskDetail

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Task #TASK-${taskId.takeLast(6)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = task?.title ?: "Field Cleanup Task",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (task == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Loading field task details…", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Task Status Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when (task.status) {
                                TaskStatus.COMPLETED -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                                TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondaryContainer
                                TaskStatus.ACCEPTED -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Task Status: ${task.status.label}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = task.status.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // 2. Location & Map Navigation
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PinDrop,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Location: ${task.approximateArea}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                MapLibreMapView(
                                    latitude = task.latitude,
                                    longitude = task.longitude,
                                    zoom = 14.5,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    // 3. Field Action Checklist
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val completedCount = checklistItems.count { it.isCompleted }
                            Text(
                                text = "Field Action Checklist ($completedCount / ${checklistItems.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            checklistItems.forEachIndexed { index, item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            checklistItems[index] = item.copy(isCompleted = !item.isCompleted)
                                        }
                                ) {
                                    Checkbox(
                                        checked = item.isCompleted,
                                        onCheckedChange = { checked ->
                                            checklistItems[index] = item.copy(isCompleted = checked)
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (item.isCompleted) FontWeight.Bold else FontWeight.Normal,
                                        color = if (item.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // 4. Evidence Photo Proof (After Cleanup Photo)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Completion Photo Proof (After Cleanup)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (afterPhotoUri == null) {
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Attach After Photo Evidence")
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    AsyncImage(
                                        model = afterPhotoUri,
                                        contentDescription = "After Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = completionNotes,
                                onValueChange = { completionNotes = it },
                                placeholder = { Text("Worker completion notes…") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // 5. Task Communication Comment Thread
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Field Task Updates & Discussion (${comments.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            comments.forEach { comment ->
                                CommentItemRow(comment = comment)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = commentText,
                                    onValueChange = { commentText = it },
                                    placeholder = { Text("Post field note…") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        if (commentText.isNotBlank()) {
                                            taskViewModel.addComment(
                                                taskId = taskId,
                                                text = commentText,
                                                onSuccess = { commentText = "" },
                                                onError = { err ->
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(err)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. Sticky Action Footer based on Task Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (task.status) {
                        TaskStatus.ASSIGNED -> {
                            OutlinedButton(
                                onClick = { showDeclineDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Decline Task", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    taskViewModel.acceptTask(
                                        taskId = taskId,
                                        onSuccess = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Task accepted!")
                                            }
                                        },
                                        onError = { err ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(err)
                                            }
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Accept Task", fontWeight = FontWeight.Bold)
                            }
                        }

                        TaskStatus.ACCEPTED -> {
                            Button(
                                onClick = {
                                    taskViewModel.startTask(
                                        taskId = taskId,
                                        onSuccess = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Task started! You are now in progress.")
                                            }
                                        },
                                        onError = { err ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(err)
                                            }
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsRun,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Task Work", fontWeight = FontWeight.Bold)
                            }
                        }

                        TaskStatus.IN_PROGRESS, TaskStatus.REWORK_REQUESTED -> {
                            Button(
                                onClick = {
                                    taskViewModel.submitTaskCompletion(
                                        taskId = taskId,
                                        afterPhotoPath = afterPhotoUri?.toString(),
                                        notes = completionNotes,
                                        onSuccess = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Completion submitted for coordinator review!")
                                            }
                                        },
                                        onError = { err ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(err)
                                            }
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E7D32)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Submit Task Completion", fontWeight = FontWeight.Bold)
                            }
                        }

                        else -> {
                            Text(
                                text = "Task Status: ${task.status.label}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Decline Task Modal
    if (showDeclineDialog) {
        AlertDialog(
            onDismissRequest = { showDeclineDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Decline Task Assignment", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select reason:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)

                    listOf(
                        "Too far away",
                        "Not available",
                        "Wrong task category",
                        "Already occupied"
                    ).forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { declineReason = reason }
                        ) {
                            RadioButton(
                                selected = declineReason == reason,
                                onClick = { declineReason = reason }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = reason, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    OutlinedTextField(
                        value = declineNotes,
                        onValueChange = { declineNotes = it },
                        placeholder = { Text("Additional notes…") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        taskViewModel.declineTask(
                            taskId = taskId,
                            reason = declineReason,
                            notes = declineNotes,
                            onSuccess = {
                                showDeclineDialog = false
                                onBackClick()
                            },
                            onError = { err ->
                                showDeclineDialog = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(err)
                                }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Decline", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeclineDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CommentItemRow(comment: TaskComment) {
    val formattedDate = remember(comment.timestamp) {
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        sdf.format(Date(comment.timestamp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${comment.authorName} (${comment.authorRole})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
