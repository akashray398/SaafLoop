package com.example.saafloop.feature.analytics

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saafloop.core.model.AnalyticsTimeRange
import com.example.saafloop.core.model.DataQualityMetrics
import com.example.saafloop.core.model.OperationalAnalyticsData
import com.example.saafloop.core.model.SectorAnalyticsItem
import com.example.saafloop.feature.analytics.components.CategoryDistributionProgressChart
import com.example.saafloop.feature.analytics.components.DailyTrendBarChart
import com.example.saafloop.feature.analytics.components.SlaPerformanceCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationalAnalyticsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OperationalAnalyticsViewModel = viewModel()
) {
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()
    val selectedSector by viewModel.selectedSector.collectAsState()
    val analyticsData by viewModel.operationalAnalyticsState.collectAsState()
    val dataQuality by viewModel.dataQualityState.collectAsState()

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Operational Analytics & Trends",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Platform Performance & Triage SLA",
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Time Range Filters
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AnalyticsTimeRange.entries.toTypedArray()) { range ->
                    FilterChip(
                        selected = selectedTimeRange == range,
                        onClick = { viewModel.setTimeRange(range) },
                        label = { Text(text = range.label) }
                    )
                }
            }

            // High-Level KPI Volume Grid
            Text(
                text = "Report Volume & Triage KPI Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiCard(count = analyticsData.totalSubmittedVolume.toString(), label = "Total Volume", color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                KpiCard(count = analyticsData.uniqueIssueCount.toString(), label = "Unique Issues", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                KpiCard(count = analyticsData.resolvedCount.toString(), label = "Resolved", color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                KpiCard(count = analyticsData.duplicateMergedCount.toString(), label = "Duplicates", color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.weight(1f))
            }

            // Daily Trend Bar Chart
            DailyTrendBarChart(trendPoints = analyticsData.dailyTrendData)

            // Category Breakdown Chart
            CategoryDistributionProgressChart(categoryDistribution = analyticsData.categoryDistribution)

            // SLA & Response Speed Card
            SlaPerformanceCard(analyticsData = analyticsData)

            // Sector Performance Table
            SectorPerformanceTable(sectors = analyticsData.sectorPerformance)

            // Data Quality Audit Indicator Card
            DataQualityCard(metrics = dataQuality)

            // Refresh Time Footer
            Text(
                text = "Last synchronized: ${SimpleDateFormat("h:mm a, MMM d", Locale.getDefault()).format(Date(analyticsData.lastRefreshedAt))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun KpiCard(count: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = count, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectorPerformanceTable(sectors: List<SectorAnalyticsItem>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Sector Performance and Hotspot Table" },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sector Performance & Hotspots",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            sectors.forEach { sector ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (sector.isHotspot) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = sector.sectorName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            if (sector.isHotspot) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(imageVector = Icons.Default.Repeat, contentDescription = "Repeat Hotspot", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                            }
                        }
                        Text(text = "${sector.activeTasks} active • ${sector.resolvedCount} resolved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Text(text = "${sector.totalReports} total", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun DataQualityCard(metrics: DataQualityMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = Color(0xFF1B5E20), modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = "Data Integrity Score: ${(metrics.dataQualityScore * 100).toInt()}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                Text(text = metrics.auditStatusMessage, style = MaterialTheme.typography.bodySmall, color = Color(0xFF1B5E20))
            }
        }
    }
}
