package com.example.saafloop.feature.report

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.saafloop.R
import com.example.saafloop.core.model.CaseStatus
import com.example.saafloop.core.model.DuplicateMatchItem
import com.example.saafloop.feature.auth.AuthDialog
import com.example.saafloop.feature.report.model.ReportFormStage
import com.example.saafloop.feature.report.model.ReportFormState
import com.example.saafloop.feature.report.model.SubmissionState
import com.example.saafloop.feature.report.model.WasteCategory
import com.example.saafloop.feature.report.model.WasteSizeEstimate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportWasteScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    draftId: String? = null,
    viewModel: ReportWasteViewModel = viewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val submissionState by viewModel.submissionState.collectAsState()
    val duplicateMatches by viewModel.duplicateMatchesState.collectAsState()

    var showLeaveConfirmDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var confirmedServerCaseId by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(draftId) {
        if (!draftId.isNullOrBlank()) {
            viewModel.loadDraft(draftId)
        }
    }

    val handleBackPress = {
        if (formState.hasEnteredContent) {
            showLeaveConfirmDialog = true
        } else {
            onBackClick()
        }
    }

    BackHandler(onBack = handleBackPress)

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val uri = viewModel.getTempCameraPhotoUri()
            if (uri != null) {
                viewModel.setPhotoUri(uri)
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setPhotoUri(uri)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.report_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBackPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveAsDraft { _, message ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.draft_action_save_draft),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            StageStepperHeader(
                currentStage = formState.currentStage,
                onStageClick = { stage ->
                    if (stage.ordinal < formState.currentStage.ordinal) {
                        viewModel.goToStage(stage)
                    }
                }
            )

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                when (formState.currentStage) {
                    ReportFormStage.PHOTO_INPUT -> {
                        PhotoInputStageContent(
                            formState = formState,
                            onTakePhoto = {
                                val uri = viewModel.createTempPhotoUri()
                                takePictureLauncher.launch(uri)
                            },
                            onPickPhoto = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onRemovePhoto = { viewModel.removePhoto() }
                        )
                    }

                    ReportFormStage.DETAILS_AND_LOCATION -> {
                        DetailsAndLocationStageContent(
                            formState = formState,
                            onCategorySelected = { viewModel.setCategory(it) },
                            onSizeSelected = { viewModel.setSizeEstimate(it) },
                            onDescriptionChanged = { viewModel.setDescription(it) },
                            onAccessNoteChanged = { viewModel.setAccessNote(it) },
                            onHazardousToggled = { viewModel.setHazardousSuspected(it) },
                            onAppendSpeech = { viewModel.appendSpeechText(it) },
                            onLocationUpdated = { lat, lng, name ->
                                viewModel.updateLocation(lat, lng, name)
                            }
                        )
                    }

                    ReportFormStage.REVIEW -> {
                        ReviewStageContent(
                            formState = formState,
                            submissionState = submissionState,
                            duplicateMatches = duplicateMatches,
                            onSubmitReport = { previousCaseId ->
                                viewModel.submitReport(
                                    previousCaseId = previousCaseId,
                                    onRequireAuth = { showAuthDialog = true },
                                    onSuccessConfirmed = { caseId ->
                                        confirmedServerCaseId = caseId
                                    }
                                )
                            },
                            onConfirmExistingCase = { caseId ->
                                viewModel.confirmExistingCase(
                                    caseId = caseId,
                                    onRequireAuth = { showAuthDialog = true },
                                    onSuccessConfirmed = { confirmedCaseId ->
                                        confirmedServerCaseId = confirmedCaseId
                                    }
                                )
                            }
                        )
                    }
                }
            }

            // Bottom Navigation Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (formState.currentStage != ReportFormStage.PHOTO_INPUT) {
                    OutlinedButton(
                        onClick = { viewModel.prevStage() },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = stringResource(R.string.report_prev_stage))
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                if (formState.currentStage != ReportFormStage.REVIEW) {
                    Button(
                        onClick = { viewModel.nextStage() },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.report_next_stage),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Leave Confirmation Dialog
    if (showLeaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.report_leave_confirm_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.report_leave_confirm_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveConfirmDialog = false
                        onBackClick()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.report_leave_confirm_exit),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirmDialog = false }) {
                    Text(text = stringResource(R.string.report_leave_confirm_keep))
                }
            }
        )
    }

    // Auth Dialog for Guests attempting to Submit
    if (showAuthDialog) {
        AuthDialog(
            onDismiss = { showAuthDialog = false },
            onAuthSuccess = {
                showAuthDialog = false
                viewModel.submitReport(
                    onRequireAuth = { showAuthDialog = true },
                    onSuccessConfirmed = { caseId ->
                        confirmedServerCaseId = caseId
                    }
                )
            }
        )
    }

    // Server Confirmed Submission Success Alert
    confirmedServerCaseId?.let { serverCaseId ->
        AlertDialog(
            onDismissRequest = {
                confirmedServerCaseId = null
                onBackClick()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32)
                )
            },
            title = {
                Text(
                    text = "Report Confirmed & Submitted",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Server Case ID: #$serverCaseId\n\nYour report is confirmed by the backend and is now under review by municipal coordinators. You can track status updates in Activity.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmedServerCaseId = null
                        onBackClick()
                    }
                ) {
                    Text("View in Activity", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun StageStepperHeader(
    currentStage: ReportFormStage,
    onStageClick: (ReportFormStage) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ReportFormStage.entries.forEach { stage ->
            val isActive = currentStage == stage
            val isCompleted = stage.ordinal < currentStage.ordinal

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = isCompleted) { onStageClick(stage) }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isActive -> MaterialTheme.colorScheme.primary
                                isCompleted -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.outlineVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Text(
                            text = "${stage.stageNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(stage.titleRes),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun PhotoInputStageContent(
    formState: ReportFormState,
    onTakePhoto: () -> Unit,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.report_photo_section_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.report_photo_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )

        if (formState.photoUri == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        2.dp,
                        if (formState.validationTriggered) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Attach photo of waste location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onTakePhoto,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = stringResource(R.string.report_take_photo))
                        }

                        OutlinedButton(
                            onClick = onPickPhoto,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = stringResource(R.string.report_choose_gallery))
                        }
                    }
                }
            }

            if (formState.validationTriggered && !formState.isPhotoValid) {
                Text(
                    text = stringResource(R.string.report_photo_required_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = formState.photoUri,
                            contentDescription = "Selected waste photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = onPickPhoto,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = stringResource(R.string.report_change_photo))
                        }

                        TextButton(
                            onClick = onRemovePhoto,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = stringResource(R.string.report_remove_photo))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailsAndLocationStageContent(
    formState: ReportFormState,
    onCategorySelected: (WasteCategory) -> Unit,
    onSizeSelected: (WasteSizeEstimate?) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onAccessNoteChanged: (String) -> Unit,
    onHazardousToggled: (Boolean) -> Unit,
    onAppendSpeech: (String) -> Unit,
    onLocationUpdated: (Double, Double, String) -> Unit
) {
    val context = LocalContext.current

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = matches?.firstOrNull()
            if (!text.isNullOrBlank()) {
                onAppendSpeech(text)
            }
        }
    }

    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe the waste site…")
        }
    }

    val isSpeechAvailable = remember {
        speechIntent.resolveActivity(context.packageManager) != null
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.report_category_section_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.report_category_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            WasteCategory.entries.forEach { category ->
                val isSelected = formState.category == category
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onCategorySelected(category) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(category.titleRes),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = category.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (formState.validationTriggered && !formState.isCategoryValid) {
                Text(
                    text = stringResource(R.string.report_category_required_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.report_hazardous_warning_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.report_hazardous_warning_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onHazardousToggled(!formState.isHazardousSuspected) }
                ) {
                    Checkbox(
                        checked = formState.isHazardousSuspected,
                        onCheckedChange = { onHazardousToggled(it) }
                    )
                    Text(
                        text = "Suspected medical / chemical / sharp / toxic waste",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "${stringResource(R.string.report_size_title)} ${stringResource(R.string.report_size_subtitle)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WasteSizeEstimate.entries.forEach { size ->
                    val isSelected = formState.sizeEstimate == size
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) onSizeSelected(null) else onSizeSelected(size)
                        },
                        label = {
                            Text(text = stringResource(size.labelRes))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.report_description_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = formState.description,
                onValueChange = onDescriptionChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.report_description_hint)) },
                trailingIcon = {
                    if (isSpeechAvailable) {
                        IconButton(onClick = { speechLauncher.launch(speechIntent) }) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = stringResource(R.string.report_voice_input),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )
            Text(
                text = "${formState.description.length}/250 characters",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.End)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.report_access_note_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = formState.accessNote,
                onValueChange = onAccessNoteChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.report_access_note_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.report_location_section_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.report_location_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    com.example.saafloop.core.ui.MapLibreMapView(
                        latitude = formState.latitude,
                        longitude = formState.longitude,
                        zoom = 14.0,
                        modifier = Modifier.fillMaxSize()
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PinDrop,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = formState.locationName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    onLocationUpdated(28.6139, 77.2090, "Central District")
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = "Central Zone", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    onLocationUpdated(28.6500, 77.2100, "North Zone")
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = "North Zone", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewStageContent(
    formState: ReportFormState,
    submissionState: SubmissionState,
    duplicateMatches: List<DuplicateMatchItem>,
    onSubmitReport: (String?) -> Unit,
    onConfirmExistingCase: (String) -> Unit
) {
    var userConsentChecked by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.report_review_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Photo Preview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (formState.photoUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = formState.photoUri,
                            contentDescription = "Report Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Text(
                        text = "No photo attached",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formState.category?.let { stringResource(it.titleRes) } ?: "Category Missing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (formState.sizeEstimate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Estimated Volume: ${stringResource(formState.sizeEstimate.labelRes)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                if (formState.isHazardousSuspected) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "⚠ Suspected Hazardous / Qualified Handling Required",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Location: ${formState.locationName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (formState.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Description: ${formState.description}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (formState.accessNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Access Note: ${formState.accessNote}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Section 8: Duplicate Detection Matches Suggestions Card
        if (duplicateMatches.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nearby Active Reports Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Text(
                        text = "We found existing reports nearby. You can confirm an existing report to add your voice, or report a new occurrence if waste has returned.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )

                    duplicateMatches.forEach { match ->
                        val isCleanedSite = match.caseReport.status == CaseStatus.VERIFIED_CLEAN
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = match.matchReason,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${match.caseReport.category} • ${match.caseReport.approximateArea}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isCleanedSite) {
                                        Button(
                                            onClick = { onSubmitReport(match.caseReport.caseId) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Waste Has Returned", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Button(
                                            onClick = { onConfirmExistingCase(match.caseReport.caseId) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Looks Like Same Waste", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Privacy & Map Sharing Notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "This report will be shared with authorised coordinators. A privacy-safe summary location will appear on the local community map.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Mandatory User Consent Checkbox
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { userConsentChecked = !userConsentChecked }
                .padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = userConsentChecked,
                onCheckedChange = { userConsentChecked = it }
            )
            Text(
                text = "I confirm these report details are accurate and understand that hazardous waste must not be handled by volunteers.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Active Submission Progress State Status Banner
        when (submissionState) {
            is SubmissionState.UploadingPhoto -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Uploading photo…", style = MaterialTheme.typography.bodyMedium)
                }
            }

            is SubmissionState.FinalisingCase -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Finalising case registration…", style = MaterialTheme.typography.bodyMedium)
                }
            }

            is SubmissionState.QueuedForUpload -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Queued for upload - Has not reached SaafLoop yet. WorkManager will retry when online.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            is SubmissionState.NeedsAttention -> {
                Text(
                    text = submissionState.errorReason,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            else -> {}
        }

        // Primary Action: "Submit as New Independent Report"
        Button(
            onClick = { onSubmitReport(null) },
            enabled = userConsentChecked && formState.isValid && submissionState !is SubmissionState.UploadingPhoto && submissionState !is SubmissionState.FinalisingCase,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (duplicateMatches.isNotEmpty()) "Submit as New Separate Report" else "Submit Report",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
