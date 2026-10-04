package com.example.saafloop.feature.explore.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.saafloop.R
import com.example.saafloop.core.model.CasePriority
import com.example.saafloop.core.model.CivicMapType
import com.example.saafloop.core.model.MapFilterState
import com.example.saafloop.core.model.TimeFilterRange

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MapFilterBottomSheet(
    filterState: MapFilterState,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onApplyFilters: (MapFilterState) -> Unit,
    onClearFilters: () -> Unit
) {
    var tempState by remember(filterState) { mutableStateOf(filterState) }
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Map Filters",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        tempState = MapFilterState()
                        onClearFilters()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Reset")
                }
            }

            // 1. Entity Types Filter
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Map Entities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                val availableTypes = listOf(
                    CivicMapType.REPORT,
                    CivicMapType.FIELD_TASK,
                    CivicMapType.COMMUNITY_ACTIVITY,
                    CivicMapType.RESOLVED_ISSUE
                )

                availableTypes.forEach { type ->
                    val isChecked = tempState.selectedTypes.contains(type)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                val newSet = tempState.selectedTypes.toMutableSet()
                                if (checked) newSet.add(type) else newSet.remove(type)
                                tempState = tempState.copy(selectedTypes = newSet)
                            }
                        )
                        Text(
                            text = type.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 2. Statuses Filter
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                val availableStatuses = listOf("Reported", "Verified", "Being Addressed", "Resolved")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableStatuses.forEach { status ->
                        val selected = tempState.selectedStatuses.contains(status)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                val newSet = tempState.selectedStatuses.toMutableSet()
                                if (selected) newSet.remove(status) else newSet.add(status)
                                tempState = tempState.copy(selectedStatuses = newSet)
                            },
                            label = { Text(text = status) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // 3. Waste Categories Filter
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Issue Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                val categories = listOf(
                    "Garbage Accumulation",
                    "Overflowing Bin",
                    "Illegal Dumping",
                    "Plastic Waste",
                    "Construction Waste",
                    "Drainage",
                    "Road Cleanliness",
                    "Public Sanitation",
                    "Other"
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val selected = tempState.selectedCategories.contains(cat)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                val newSet = tempState.selectedCategories.toMutableSet()
                                if (selected) newSet.remove(cat) else newSet.add(cat)
                                tempState = tempState.copy(selectedCategories = newSet)
                            },
                            label = { Text(text = cat) }
                        )
                    }
                }
            }

            // 4. Time Range Filter
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Timeframe",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeFilterRange.entries.forEach { timeRange ->
                        val selected = tempState.selectedTimeRange == timeRange
                        FilterChip(
                            selected = selected,
                            onClick = {
                                tempState = tempState.copy(selectedTimeRange = timeRange)
                            },
                            label = { Text(text = timeRange.label) }
                        )
                    }
                }
            }

            // 5. Priorities Filter
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Priority Level",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CasePriority.entries.forEach { priority ->
                        val selected = tempState.selectedPriorities.contains(priority)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                val newSet = tempState.selectedPriorities.toMutableSet()
                                if (selected) newSet.remove(priority) else newSet.add(priority)
                                tempState = tempState.copy(selectedPriorities = newSet)
                            },
                            label = { Text(text = "${priority.label} Priority") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Cancel")
                }

                Button(
                    onClick = {
                        onApplyFilters(tempState)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(text = "Apply Filters", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
