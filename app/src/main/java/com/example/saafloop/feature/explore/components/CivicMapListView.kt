package com.example.saafloop.feature.explore.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.saafloop.core.model.CasePriority
import com.example.saafloop.core.model.CivicMapMarker
import com.example.saafloop.core.model.CivicMapType
import com.example.saafloop.core.util.GeoUtils

@Composable
fun CivicMapListView(
    markers: List<CivicMapMarker>,
    onSelectMarker: (CivicMapMarker) -> Unit,
    onOpenEntityDetail: (CivicMapMarker) -> Unit,
    onSwitchToMapMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (markers.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "No civic items found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Try adjusting your filters or search query to find nearby reports and activities.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(markers, key = { it.id }) { marker ->
            CivicListItemCard(
                marker = marker,
                onSelectMarker = onSelectMarker,
                onOpenEntityDetail = onOpenEntityDetail,
                onSwitchToMapMode = onSwitchToMapMode
            )
        }
    }
}

@Composable
private fun CivicListItemCard(
    marker: CivicMapMarker,
    onSelectMarker: (CivicMapMarker) -> Unit,
    onOpenEntityDetail: (CivicMapMarker) -> Unit,
    onSwitchToMapMode: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onSelectMarker(marker)
            }
            .semantics {
                contentDescription = "${marker.title}, ${marker.areaName}, ${marker.statusLabel}, ${GeoUtils.formatDistance(marker.distanceMeters)}"
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TypeTag(type = marker.type)

                Text(
                    text = GeoUtils.formatDistance(marker.distanceMeters),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = marker.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = marker.areaName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusTag(statusLabel = marker.statusLabel)

                marker.priority?.let { priority ->
                    PriorityBadge(priority = priority)
                }
            }

            if (marker.description.isNotBlank()) {
                Text(
                    text = marker.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onOpenEntityDetail(marker) },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "View Details",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        onSelectMarker(marker)
                        onSwitchToMapMode()
                    },
                    modifier = Modifier.height(38.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Locate on Map",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Map", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun TypeTag(type: CivicMapType) {
    val (bgColor, textColor, icon) = when (type) {
        CivicMapType.REPORT -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, Icons.Default.ReportProblem)
        CivicMapType.FIELD_TASK -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Icons.Default.Build)
        CivicMapType.COMMUNITY_ACTIVITY -> Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, Icons.Default.Event)
        CivicMapType.RESOLVED_ISSUE -> Triple(Color(0xFFE8F5E9), Color(0xFF1B5E20), Icons.Default.CheckCircle)
        CivicMapType.CLUSTER -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Icons.Default.LocationOn)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = type.label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
private fun StatusTag(statusLabel: String) {
    val chipColor = when {
        statusLabel.contains("Resolved", ignoreCase = true) || statusLabel.contains("Clean", ignoreCase = true) -> Color(0xFFE8F5E9)
        statusLabel.contains("Addressed", ignoreCase = true) || statusLabel.contains("Progress", ignoreCase = true) -> Color(0xFFE3F2FD)
        else -> Color(0xFFFFF3E0)
    }

    val textColor = when {
        statusLabel.contains("Resolved", ignoreCase = true) || statusLabel.contains("Clean", ignoreCase = true) -> Color(0xFF1B5E20)
        statusLabel.contains("Addressed", ignoreCase = true) || statusLabel.contains("Progress", ignoreCase = true) -> Color(0xFF0D47A1)
        else -> Color(0xFFE65100)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(chipColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = statusLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = textColor)
    }
}

@Composable
private fun PriorityBadge(priority: CasePriority) {
    val color = when (priority) {
        CasePriority.CRITICAL, CasePriority.HIGH -> Color(0xFFD32F2F)
        CasePriority.MEDIUM -> Color(0xFFED6C02)
        CasePriority.LOW -> Color(0xFF2E7D32)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, color, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = priority.label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
    }
}
