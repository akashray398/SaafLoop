package com.example.saafloop.feature.notification

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saafloop.core.model.NotificationItem
import com.example.saafloop.core.model.NotificationPriority
import com.example.saafloop.core.model.NotificationType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    onBackClick: () -> Unit,
    onOpenPreferences: () -> Unit,
    onNavigateToDeepLink: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationCenterViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val notifications by viewModel.notificationsState.collectAsState()
    val unreadCount by viewModel.unreadCountState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Notification Center",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (unreadCount > 0) "$unreadCount unread updates" else "All caught up",
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
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = { viewModel.markAllAsRead() }) {
                            Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Read All", fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(onClick = onOpenPreferences) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Notification Settings"
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Filter Tabs Row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(NotificationTabFilter.entries.toTypedArray()) { tab ->
                    val isSelected = selectedTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(tab) },
                        label = { Text(text = tab.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Notification List
            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "You're all caught up!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "No new notifications in this category.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notifications, key = { it.notificationId }) { item ->
                        NotificationItemCard(
                            item = item,
                            onClick = {
                                viewModel.markAsRead(item.notificationId)
                                if (item.deepLink.isNotBlank()) {
                                    onNavigateToDeepLink(item.deepLink)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItemCard(
    item: NotificationItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "${item.title}, ${item.body}, ${if (item.isRead) "Read" else "Unread"}"
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isRead)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isRead) 1.dp else 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Unread Indicator Dot + Type Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getNotificationBgColor(item.type)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getNotificationIcon(item.type),
                    contentDescription = null,
                    tint = getNotificationIconColor(item.type),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (item.isRead) FontWeight.SemiBold else FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = formatRelativeTime(item.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.priority == NotificationPriority.HIGH || item.priority == NotificationPriority.CRITICAL) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFD32F2F).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${item.priority.label} Alert",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            if (!item.isRead) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
private fun getNotificationIcon(type: NotificationType) = when (type) {
    NotificationType.REPORT -> Icons.Default.ReportProblem
    NotificationType.TASK -> Icons.Default.Build
    NotificationType.COMMUNITY -> Icons.Default.Event
    NotificationType.ORGANIZATION -> Icons.Default.CheckCircle
    NotificationType.AI -> Icons.Default.Analytics
    NotificationType.SYSTEM -> Icons.Default.Info
}

@Composable
private fun getNotificationBgColor(type: NotificationType) = when (type) {
    NotificationType.REPORT -> MaterialTheme.colorScheme.errorContainer
    NotificationType.TASK -> MaterialTheme.colorScheme.primaryContainer
    NotificationType.COMMUNITY -> MaterialTheme.colorScheme.secondaryContainer
    NotificationType.ORGANIZATION -> Color(0xFFE8F5E9)
    NotificationType.AI -> MaterialTheme.colorScheme.tertiaryContainer
    NotificationType.SYSTEM -> MaterialTheme.colorScheme.surfaceVariant
}

@Composable
private fun getNotificationIconColor(type: NotificationType) = when (type) {
    NotificationType.REPORT -> MaterialTheme.colorScheme.onErrorContainer
    NotificationType.TASK -> MaterialTheme.colorScheme.onPrimaryContainer
    NotificationType.COMMUNITY -> MaterialTheme.colorScheme.onSecondaryContainer
    NotificationType.ORGANIZATION -> Color(0xFF1B5E20)
    NotificationType.AI -> MaterialTheme.colorScheme.onTertiaryContainer
    NotificationType.SYSTEM -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
