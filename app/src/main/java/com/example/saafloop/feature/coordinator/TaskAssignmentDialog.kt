package com.example.saafloop.feature.coordinator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.saafloop.core.model.CasePriority
import com.example.saafloop.core.model.CaseReport
import com.example.saafloop.core.model.FieldTask
import com.example.saafloop.core.model.TaskStatus
import java.util.UUID

data class WorkerOption(
    val uid: String,
    val name: String,
    val role: String,
    val distanceText: String,
    val activeTasksCount: Int
)

@Composable
fun TaskAssignmentDialog(
    caseReport: CaseReport,
    onDismiss: () -> Unit,
    onTaskAssigned: (FieldTask) -> Unit
) {
    val sampleWorkers = remember {
        listOf(
            WorkerOption("worker_1", "Rahul Sharma", "Field Operations Lead", "1.2 km away", 2),
            WorkerOption("worker_2", "Aman Verma", "Sanitation Specialist", "2.4 km away", 0),
            WorkerOption("worker_3", "Priya Singh", "Volunteer Team Lead", "0.8 km away", 1)
        )
    }

    var selectedWorker by remember { mutableStateOf(sampleWorkers.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.AssignmentInd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Assign Field Cleanup Task",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Report: #${caseReport.caseId.takeLast(6)} • ${caseReport.category}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Select Assignee (Smart Proximity Suggestions):",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )

                sampleWorkers.forEach { worker ->
                    val isSelected = selectedWorker.uid == worker.uid
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedWorker = worker },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedWorker = worker }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = worker.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${worker.role} • ${worker.distanceText} • ${worker.activeTasksCount} active tasks",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newTask = FieldTask(
                        taskId = "task_${UUID.randomUUID()}",
                        reportId = caseReport.caseId,
                        title = caseReport.category,
                        description = caseReport.description,
                        category = caseReport.category,
                        priority = CasePriority.HIGH,
                        status = TaskStatus.ASSIGNED,
                        latitude = caseReport.latitude,
                        longitude = caseReport.longitude,
                        approximateArea = caseReport.approximateArea,
                        assignedToUid = selectedWorker.uid,
                        assignedToName = selectedWorker.name,
                        assignedToRole = selectedWorker.role,
                        dueAt = System.currentTimeMillis() + (4 * 60 * 60 * 1000) // 4 hours SLA
                    )
                    onTaskAssigned(newTask)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Confirm Assignment", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
