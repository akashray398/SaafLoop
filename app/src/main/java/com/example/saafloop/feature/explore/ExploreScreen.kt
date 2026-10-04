package com.example.saafloop.feature.explore

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saafloop.R
import com.example.saafloop.core.data.UserAccessState
import com.example.saafloop.core.model.CivicMapMarker
import com.example.saafloop.core.model.CivicMapType
import com.example.saafloop.core.model.CivicMapViewMode
import com.example.saafloop.core.ui.MapLibreMapView
import com.example.saafloop.feature.explore.components.CivicEntityPreviewCard
import com.example.saafloop.feature.explore.components.CivicMapListView
import com.example.saafloop.feature.explore.components.GeoAnalyticsCard
import com.example.saafloop.feature.explore.components.LocationConfirmationDialog
import com.example.saafloop.feature.explore.components.MapFilterBottomSheet
import com.example.saafloop.feature.explore.components.ReportLocationPickerSheet
import com.example.saafloop.feature.explore.model.LocationUnavailableReason
import com.example.saafloop.feature.explore.model.MapCameraRegion
import com.example.saafloop.feature.explore.model.MapUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onReportWasteClick: () -> Unit,
    onReportWasteAtLocation: (Double, Double, String) -> Unit = { _, _, _ -> },
    onOpenReportDetail: (String) -> Unit = {},
    onOpenTaskDetail: (String) -> Unit = {},
    onOpenActivityDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    exploreViewModel: ExploreViewModel = viewModel()
) {
    val context = LocalContext.current
    val mapUiState by exploreViewModel.mapUiState.collectAsState()
    val searchQuery by exploreViewModel.searchQuery.collectAsState()
    val viewMode by exploreViewModel.viewMode.collectAsState()
    val filterState by exploreViewModel.filterState.collectAsState()
    val selectedMarker by exploreViewModel.selectedMarker.collectAsState()
    val mapDisplayMarkers by exploreViewModel.mapDisplayMarkers.collectAsState()
    val allRawMarkers by exploreViewModel.allRawMarkers.collectAsState()
    val nearbyMarkersForSelected by exploreViewModel.nearbyMarkersForSelected.collectAsState()
    val locationConfirmationState by exploreViewModel.locationConfirmationState.collectAsState()
    val geoAnalyticsState by exploreViewModel.geoAnalyticsState.collectAsState()
    val isAnalyticsMode by exploreViewModel.isAnalyticsMode.collectAsState()
    val lastSyncTimestamp by exploreViewModel.lastSyncTimestamp.collectAsState()
    val userAccessState by exploreViewModel.userAccessState.collectAsState()

    var showPermissionInfoDialog by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showLocationPickerSheet by remember { mutableStateOf(false) }

    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            exploreViewModel.onRequestLocation(context)
        } else {
            showPermissionInfoDialog = true
        }
    }

    val currentCameraRegion = when (val state = mapUiState) {
        is MapUiState.Ready -> state.cameraRegion
        is MapUiState.LocationUnavailable -> state.cameraRegion
        is MapUiState.MapProviderUnavailable -> state.cameraRegion
        else -> MapCameraRegion(28.6139, 77.2090, 13.5f, "Central District")
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Search Bar & Preset Zone Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { exploreViewModel.searchLocation(context, it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Search sector, area, landmark" },
                    placeholder = {
                        Text(text = "Search area, sector, ward, or landmark")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { exploreViewModel.searchLocation(context, "") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                val presetZones = listOf("Central", "North Zone", "South Zone", "East Zone", "West Zone")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(presetZones) { zone ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (searchQuery.equals(zone, ignoreCase = true))
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { exploreViewModel.searchLocation(context, zone) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = zone,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (searchQuery.equals(zone, ignoreCase = true))
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. Control Bar: Map/List Mode Toggle + Filter FAB + Analytics Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Map / List Tab
                TabRow(
                    selectedTabIndex = if (viewMode == CivicMapViewMode.MAP) 0 else 1,
                    modifier = Modifier
                        .width(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Tab(
                        selected = viewMode == CivicMapViewMode.MAP,
                        onClick = { exploreViewModel.setViewMode(CivicMapViewMode.MAP) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Map")
                            }
                        }
                    )
                    Tab(
                        selected = viewMode == CivicMapViewMode.LIST,
                        onClick = { exploreViewModel.setViewMode(CivicMapViewMode.LIST) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("List")
                            }
                        }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Filter Button with Badge
                    val activeFilterCount = (if (filterState.selectedCategories.isNotEmpty()) 1 else 0) +
                            (if (filterState.selectedPriorities.isNotEmpty()) 1 else 0) +
                            (if (filterState.selectedTimeRange != com.example.saafloop.core.model.TimeFilterRange.ALL_TIME) 1 else 0)

                    IconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (activeFilterCount > 0) {
                            BadgedBox(badge = { Badge { Text(activeFilterCount.toString()) } }) {
                                Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filters")
                            }
                        } else {
                            Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filters")
                        }
                    }

                    // Analytics Toggle (for Coordinators)
                    if (userAccessState is UserAccessState.AuthenticatedResident) {
                        IconButton(
                            onClick = { exploreViewModel.toggleAnalyticsMode() },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isAnalyticsMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "Geo Analytics",
                                tint = if (isAnalyticsMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Coordinator Analytics Overview Card if active
            if (isAnalyticsMode) {
                GeoAnalyticsCard(analyticsState = geoAnalyticsState)
            }

            // 3. Main Display Area: Map or List View
            if (viewMode == CivicMapViewMode.MAP) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                ) {
                    MapLibreMapView(
                        latitude = currentCameraRegion.latitude,
                        longitude = currentCameraRegion.longitude,
                        zoom = currentCameraRegion.zoom.toDouble(),
                        markers = mapDisplayMarkers,
                        selectedMarkerId = selectedMarker?.id,
                        onMarkerClick = { marker ->
                            exploreViewModel.selectMarker(marker)
                        },
                        onMapClick = { point ->
                            exploreViewModel.prepareReportAtLocation(point.latitude, point.longitude, currentCameraRegion.regionName)
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Map Overlay FABs
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        // "+ Report Issue" FAB
                        Button(
                            onClick = { showLocationPickerSheet = true },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Report Issue", fontWeight = FontWeight.Bold)
                        }

                        // "My Location" FAB
                        FloatingActionButton(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                        android.Manifest.permission.ACCESS_FINE_LOCATION
                                    )
                                )
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .semantics { contentDescription = "Use my current location" },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            } else {
                // List View Mode
                CivicMapListView(
                    markers = allRawMarkers,
                    onSelectMarker = { marker -> exploreViewModel.selectMarker(marker) },
                    onOpenEntityDetail = { marker ->
                        when (marker.type) {
                            CivicMapType.REPORT, CivicMapType.RESOLVED_ISSUE -> onOpenReportDetail(marker.originalEntityId)
                            CivicMapType.FIELD_TASK -> onOpenTaskDetail(marker.originalEntityId)
                            CivicMapType.COMMUNITY_ACTIVITY -> onOpenActivityDetail(marker.originalEntityId)
                            CivicMapType.CLUSTER -> {}
                        }
                    },
                    onSwitchToMapMode = { exploreViewModel.setViewMode(CivicMapViewMode.MAP) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Data Freshness Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${allRawMarkers.size} civic items • Synchronized ${formatTime(lastSyncTimestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                TextButton(onClick = { exploreViewModel.refreshData() }) {
                    Text(text = "Refresh", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Entity Detail Bottom Preview Card
        CivicEntityPreviewCard(
            marker = selectedMarker,
            nearbyMarkers = nearbyMarkersForSelected,
            onDismiss = { exploreViewModel.dismissSelectedMarker() },
            onOpenEntityDetail = { marker ->
                when (marker.type) {
                    CivicMapType.REPORT, CivicMapType.RESOLVED_ISSUE -> onOpenReportDetail(marker.originalEntityId)
                    CivicMapType.FIELD_TASK -> onOpenTaskDetail(marker.originalEntityId)
                    CivicMapType.COMMUNITY_ACTIVITY -> onOpenActivityDetail(marker.originalEntityId)
                    CivicMapType.CLUSTER -> exploreViewModel.setViewMode(CivicMapViewMode.LIST)
                }
            },
            onSelectNearbyMarker = { nearby -> exploreViewModel.selectMarker(nearby) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        MapFilterBottomSheet(
            filterState = filterState,
            sheetState = filterSheetState,
            onDismiss = { showFilterSheet = false },
            onApplyFilters = { newFilters -> exploreViewModel.updateFilters(newFilters) },
            onClearFilters = { exploreViewModel.clearFilters() }
        )
    }

    // Report Location Choice Sheet
    if (showLocationPickerSheet) {
        ReportLocationPickerSheet(
            sheetState = pickerSheetState,
            onDismiss = { showLocationPickerSheet = false },
            onUseCurrentLocation = {
                locationPermissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
                exploreViewModel.prepareReportAtLocation(currentCameraRegion.latitude, currentCameraRegion.longitude, currentCameraRegion.regionName)
            },
            onSelectOnMap = {
                exploreViewModel.prepareReportAtLocation(currentCameraRegion.latitude, currentCameraRegion.longitude, currentCameraRegion.regionName)
            },
            onSearchSector = {
                exploreViewModel.prepareReportAtLocation(currentCameraRegion.latitude, currentCameraRegion.longitude, currentCameraRegion.regionName)
            }
        )
    }

    // Location Confirmation Dialog
    locationConfirmationState?.let { confState ->
        LocationConfirmationDialog(
            confirmationState = confState,
            onDismiss = { exploreViewModel.dismissLocationConfirmation() },
            onAdjustLocation = {
                exploreViewModel.dismissLocationConfirmation()
                exploreViewModel.setViewMode(CivicMapViewMode.MAP)
            },
            onConfirmLocation = {
                exploreViewModel.dismissLocationConfirmation()
                onReportWasteAtLocation(confState.latitude, confState.longitude, confState.areaName)
            }
        )
    }

    // Location Permission Info Dialog
    if (showPermissionInfoDialog || mapUiState is MapUiState.LocationUnavailable) {
        val reason = (mapUiState as? MapUiState.LocationUnavailable)?.reason
        AlertDialog(
            onDismissRequest = {
                showPermissionInfoDialog = false
                exploreViewModel.dismissLocationError()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.LocationOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = if (reason == LocationUnavailableReason.SERVICES_DISABLED)
                        stringResource(R.string.explore_location_disabled_title)
                    else
                        stringResource(R.string.explore_location_permission_needed_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (reason == LocationUnavailableReason.SERVICES_DISABLED)
                        stringResource(R.string.explore_location_disabled_desc)
                    else
                        "SaafLoop uses your location to show relevant civic issues and community activities near you. Your private location is never displayed publicly.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionInfoDialog = false
                        exploreViewModel.dismissLocationError()
                    }
                ) {
                    Text(text = stringResource(R.string.access_dialog_close), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
