package com.example.saafloop.feature.admin

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saafloop.core.model.GovernanceAppeal
import com.example.saafloop.core.model.GovernanceCase
import com.example.saafloop.core.model.GovernanceCaseStatus
import com.example.saafloop.core.model.OperationalEscalationItem
import com.example.saafloop.core.model.OrgVerificationReview
import com.example.saafloop.core.model.SecurityAuditEvent
import com.example.saafloop.feature.admin.components.GovernanceCaseDetailSheet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGovernanceDashboardScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminGovernanceViewModel = viewModel()
) {
    val cases by viewModel.governanceCasesState.collectAsState()
    val appeals by viewModel.appealsState.collectAsState()
    val orgReviews by viewModel.orgReviewsState.collectAsState()
    val escalations by viewModel.escalationsState.collectAsState()
    val auditLogs by viewModel.auditLogsState.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0=Cases, 1=Appeals, 2=Org Reviews, 3=Escalations, 4=Audit Log
    var activeCaseForReview by remember { mutableStateOf<GovernanceCase?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Admin Governance & Moderation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Trust, Safety & Dispute Resolution",
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
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // KPI Overview Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminKpiBox(count = cases.size.toString(), label = "Open Cases", color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                AdminKpiBox(count = appeals.size.toString(), label = "Appeals", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                AdminKpiBox(count = orgReviews.size.toString(), label = "Org Reviews", color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.weight(1f))
                AdminKpiBox(count = escalations.size.toString(), label = "Escalations", color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
            }

            // Tab Navigation
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("Cases (${cases.size})") })
                Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("Appeals (${appeals.size})") })
                Tab(selected = selectedTabIndex == 2, onClick = { selectedTabIndex = 2 }, text = { Text("Orgs (${orgReviews.size})") })
                Tab(selected = selectedTabIndex == 3, onClick = { selectedTabIndex = 3 }, text = { Text("Audit (${auditLogs.size})") })
            }

            // Tab Content List
            when (selectedTabIndex) {
                0 -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(cases, key = { it.caseId }) { item ->
                        GovernanceCaseCard(caseItem = item, onClick = { activeCaseForReview = item })
                    }
                }

                1 -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(appeals, key = { it.appealId }) { appeal ->
                        GovernanceAppealCard(
                            appeal = appeal,
                            onResolve = { outcome -> viewModel.resolveAppeal(appeal.appealId, outcome, "Reviewed by Platform Admin") }
                        )
                    }
                }

                2 -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(orgReviews, key = { it.reviewId }) { review ->
                        OrgReviewCard(
                            review = review,
                            onApprove = { viewModel.updateOrgVerification(review.reviewId, "VERIFIED", "Verification docs approved.") },
                            onReject = { viewModel.updateOrgVerification(review.reviewId, "REJECTED", "Documentation insufficient.") }
                        )
                    }
                }

                3 -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(auditLogs, key = { it.eventId }) { log ->
                        AuditLogCard(log = log)
                    }
                }
            }
        }
    }

    activeCaseForReview?.let { caseItem ->
        GovernanceCaseDetailSheet(
            caseItem = caseItem,
            onDismiss = { activeCaseForReview = null },
            onResolveCase = { caseId, targetStatus, decisionReason ->
                viewModel.resolveCase(caseId, targetStatus, decisionReason)
            }
        )
    }
}

@Composable
private fun AdminKpiBox(count: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = count, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun GovernanceCaseCard(caseItem: GovernanceCase, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Governance Case #${caseItem.caseId}" },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = caseItem.category.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(text = caseItem.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Status: ${caseItem.status.label}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun GovernanceAppealCard(appeal: GovernanceAppeal, onResolve: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Appeal for Resource #${appeal.originalResourceId.takeLast(6)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            Text(text = appeal.explanation, style = MaterialTheme.typography.bodySmall)

            if (appeal.status == "SUBMITTED" || appeal.status == "UNDER_REVIEW") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onResolve("OVERTURNED") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                        Text("Overturn (Approve)")
                    }
                    OutlinedButton(onClick = { onResolve("UPHELD") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                        Text("Upheld (Reject)")
                    }
                }
            } else {
                Text(text = "Outcome: ${appeal.status}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun OrgReviewCard(review: OrgVerificationReview, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CorporateFare, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = review.orgName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Text(text = "Type: ${review.orgType} • Status: ${review.status}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (review.status == "SUBMITTED" || review.status == "UNDER_REVIEW") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onApprove, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                        Text("Approve & Verify")
                    }
                    OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                        Text("Reject Docs")
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditLogCard(log: SecurityAuditEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = log.eventType, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(text = "${log.action} (${log.actorRole})", style = MaterialTheme.typography.bodySmall)
            }
            Text(text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.timestamp)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
