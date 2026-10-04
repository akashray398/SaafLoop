package com.example.saafloop.feature.coordinator

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saafloop.core.data.TaskRepository
import com.example.saafloop.core.data.TaskRepositoryImpl
import com.example.saafloop.core.model.AuditEvent
import com.example.saafloop.core.model.CasePriority
import com.example.saafloop.core.model.CaseReport
import com.example.saafloop.core.model.CaseStatus
import com.example.saafloop.core.model.IssueSeverity
import com.example.saafloop.core.ui.MapLibreMapView
import com.example.saafloop.core.util.PriorityCalculator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportReviewScreen(
    caseId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    coordinatorViewModel: CoordinatorViewModel = viewModel()
) {
    val reportDetail by coordinatorViewModel.activeReportDetailState.collectAsState()
    val auditHistory by coordinatorViewModel.activeReportAuditHistoryState.collectAsState()
    val priorityResult by coordinatorViewModel.activeReportPriorityResultState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(caseId) {
        coordinatorViewModel.selectCaseForReview(caseId)
    }

    var selectedSeverity by remember { mutableStateOf(IssueSeverity.MODERATE) }
    var selectedPriority by remember { mutableStateOf(CasePriority.HIGH) }

    var showVerifyDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var showRequestInfoDialog by remember { mutableStateOf(false) }
    var showAssignTaskDialog by remember { mutableStateOf(false) }

    var verificationNotes by remember { mutableStateOf("") }
    var rejectionReason by remember { mutableStateOf("Spam or Invalid Information") }
    var rejectionNotes by remember { mutableStateOf("") }

    var requestInfoReason by remember { mutableStateOf("Location details unclear") }
    var requestInfoMessage by remember { mutableStateOf("") }

    val report = reportDetail

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Report #SL-${caseId.takeLast(6)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = report?.category ?: "Review Report",
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
        if (report == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Loading report details…", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            val priorityResult = remember(report, selectedSeverity) {
                PriorityCalculator.calculatePriority(report, selectedSeverity)
            }

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
                    // 1. Status Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when (report.status) {
                                CaseStatus.VERIFIED -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                                CaseStatus.UNDER_REVIEW -> MaterialTheme.colorScheme.secondaryContainer
                                CaseStatus.NEEDS_INFORMATION -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.primaryContainer
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
                                    text = "Current Status: ${report.status.label}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = report.status.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // 2. Location & Map Verification
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
                                    text = "Location: ${report.approximateArea}",
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
                                    latitude = report.latitude,
                                    longitude = report.longitude,
                                    zoom = 14.5,
                                     publicCases = listOf(report),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    // 2.5 AI Triage & Priority Insights Card
                    com.example.saafloop.feature.coordinator.components.AICoordinatorInsightsCard(
                        priorityResult = priorityResult,
                        duplicateMatches = emptyList(),
                        onOverridePriority = { /* override */ }
                    )

                    // 3. Issue Classification & Priority Calculator
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
                                text = "Issue Severity & Priority Calculator",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Select Severity Level:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                IssueSeverity.entries.forEach { severity ->
                                    val isSelected = selectedSeverity == severity
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedSeverity = severity },
                                        label = { Text(severity.label, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Calculated Priority: ${priorityResult.priority.label}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    priorityResult.reasons.forEach { reason ->
                                        Text(
                                            text = "• $reason",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Audit Trail Timeline
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Audit History & Activity Log",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (auditHistory.isEmpty()) {
                                Text(
                                    text = "No audit events recorded yet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            } else {
                                auditHistory.forEach { audit ->
                                    AuditItemRow(audit = audit)
                                }
                            }
                        }
                    }
                }

                // 5. Sticky Coordinator Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showRejectDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showRequestInfoDialog = true },
                        modifier = Modifier
                            .weight(1.1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Request Info", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (report.status == CaseStatus.VERIFIED) {
                                showAssignTaskDialog = true
                            } else {
                                showVerifyDialog = true
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (report.status == CaseStatus.VERIFIED) MaterialTheme.colorScheme.primary else Color(0xFF2E7D32)
                        )
                    ) {
                        Icon(
                            imageVector = if (report.status == CaseStatus.VERIFIED) Icons.Default.AssignmentInd else Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (report.status == CaseStatus.VERIFIED) "Assign Task" else "Verify",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Assign Task Dialog
    if (showAssignTaskDialog && report != null) {
        val taskRepo = remember { TaskRepositoryImpl(coordinatorViewModel.getApplication()) }
        TaskAssignmentDialog(
            caseReport = report,
            onDismiss = { showAssignTaskDialog = false },
            onTaskAssigned = { newTask ->
                coroutineScope.launch {
                    val result = taskRepo.createAndAssignTask(newTask)
                    result.onSuccess {
                        showAssignTaskDialog = false
                        snackbarHostState.showSnackbar("Task assigned to ${newTask.assignedToName}!")
                    }.onFailure { err ->
                        showAssignTaskDialog = false
                        snackbarHostState.showSnackbar(err.message ?: "Failed to assign task")
                    }
                }
            }
        )
    }

    // Verify Report Confirmation Modal
    if (showVerifyDialog && report != null) {
        AlertDialog(
            onDismissRequest = { showVerifyDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32)
                )
            },
            title = {
                Text(
                    text = "Verify Report #SL-${caseId.takeLast(6)}?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "This report will become a verified actionable case ready for team assignment.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = verificationNotes,
                        onValueChange = { verificationNotes = it },
                        placeholder = { Text("Add verification notes for cleanup team…") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coordinatorViewModel.verifyReport(
                            caseId = caseId,
                            category = report.category,
                            severity = selectedSeverity.name,
                            priority = selectedPriority.name,
                            notes = verificationNotes,
                            onSuccess = {
                                showVerifyDialog = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Report verified successfully!")
                                }
                            },
                            onError = { err ->
                                showVerifyDialog = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(err)
                                }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Confirm Verification", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showVerifyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reject Report Modal
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(text = "Reject Report", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Select Rejection Reason:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)

                    listOf(
                        "Spam or Invalid Information",
                        "Insufficient Evidence",
                        "Outside Service Area",
                        "Issue Already Resolved"
                    ).forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { rejectionReason = reason }
                        ) {
                            RadioButton(
                                selected = rejectionReason == reason,
                                onClick = { rejectionReason = reason }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = reason, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    OutlinedTextField(
                        value = rejectionNotes,
                        onValueChange = { rejectionNotes = it },
                        placeholder = { Text("Additional notes…") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coordinatorViewModel.rejectReport(
                            caseId = caseId,
                            reason = rejectionReason,
                            notes = rejectionNotes,
                            onSuccess = {
                                showRejectDialog = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Report rejected")
                                }
                            },
                            onError = { err ->
                                showRejectDialog = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(err)
                                }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Rejection", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Request Additional Info Modal
    if (showRequestInfoDialog) {
        AlertDialog(
            onDismissRequest = { showRequestInfoDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
            },
            title = {
                Text(text = "Request Additional Information", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Reason for Request:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)

                    listOf(
                        "Location details unclear",
                        "Image quality insufficient",
                        "Description incomplete"
                    ).forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { requestInfoReason = reason }
                        ) {
                            RadioButton(
                                selected = requestInfoReason == reason,
                                onClick = { requestInfoReason = reason }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = reason, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    OutlinedTextField(
                        value = requestInfoMessage,
                        onValueChange = { requestInfoMessage = it },
                        placeholder = { Text("Message to reporter…") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coordinatorViewModel.requestMoreInfo(
                            caseId = caseId,
                            reason = requestInfoReason,
                            message = requestInfoMessage,
                            onSuccess = {
                                showRequestInfoDialog = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Information request sent to reporter")
                                }
                            },
                            onError = { err ->
                                showRequestInfoDialog = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(err)
                                }
                            }
                        )
                    }
                ) {
                    Text("Send Request", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestInfoDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AuditItemRow(audit: AuditEvent) {
    val formattedDate = remember(audit.timestamp) {
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        sdf.format(Date(audit.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${audit.action} by ${audit.actorName}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            if (!audit.notes.isNullOrBlank()) {
                Text(
                    text = audit.notes,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
