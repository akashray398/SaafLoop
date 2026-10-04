package com.example.saafloop.feature.explore

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saafloop.core.data.AuthRepository
import com.example.saafloop.core.data.AuthRepositoryImpl
import com.example.saafloop.core.data.CommunityRepository
import com.example.saafloop.core.data.CommunityRepositoryImpl
import com.example.saafloop.core.data.RemoteCaseRepository
import com.example.saafloop.core.data.RemoteCaseRepositoryImpl
import com.example.saafloop.core.data.TaskRepository
import com.example.saafloop.core.data.TaskRepositoryImpl
import com.example.saafloop.core.data.UserAccessState
import com.example.saafloop.core.model.CasePriority
import com.example.saafloop.core.model.CaseReport
import com.example.saafloop.core.model.CaseStatus
import com.example.saafloop.core.model.CivicMapMarker
import com.example.saafloop.core.model.CivicMapType
import com.example.saafloop.core.model.CivicMapViewMode
import com.example.saafloop.core.model.CommunityActivity
import com.example.saafloop.core.model.FieldTask
import com.example.saafloop.core.model.GeoAnalyticsState
import com.example.saafloop.core.model.LocationConfirmationState
import com.example.saafloop.core.model.MapFilterState
import com.example.saafloop.core.model.SectorOverview
import com.example.saafloop.core.model.TimeFilterRange
import com.example.saafloop.core.util.GeoUtils
import com.example.saafloop.feature.explore.model.LocationUnavailableReason
import com.example.saafloop.feature.explore.model.MapCameraRegion
import com.example.saafloop.feature.explore.model.MapUiState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExploreViewModel(application: Application) : AndroidViewModel(application) {

    private val remoteCaseRepository: RemoteCaseRepository = RemoteCaseRepositoryImpl(application)
    private val taskRepository: TaskRepository = TaskRepositoryImpl(application)
    private val communityRepository: CommunityRepository = CommunityRepositoryImpl(application)
    private val authRepository: AuthRepository = AuthRepositoryImpl(application)

    private val defaultRegion = MapCameraRegion(
        latitude = 28.6139,
        longitude = 77.2090,
        zoom = 13.5f,
        regionName = "Central District"
    )

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val _mapUiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val mapUiState: StateFlow<MapUiState> = _mapUiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _viewMode = MutableStateFlow(CivicMapViewMode.MAP)
    val viewMode: StateFlow<CivicMapViewMode> = _viewMode.asStateFlow()

    private val _filterState = MutableStateFlow(MapFilterState())
    val filterState: StateFlow<MapFilterState> = _filterState.asStateFlow()

    private val _selectedMarker = MutableStateFlow<CivicMapMarker?>(null)
    val selectedMarker: StateFlow<CivicMapMarker?> = _selectedMarker.asStateFlow()

    private val _locationConfirmationState = MutableStateFlow<LocationConfirmationState?>(null)
    val locationConfirmationState: StateFlow<LocationConfirmationState?> = _locationConfirmationState.asStateFlow()

    private val _isAnalyticsMode = MutableStateFlow(false)
    val isAnalyticsMode: StateFlow<Boolean> = _isAnalyticsMode.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val _userLocation = MutableStateFlow<Location?>(null)

    val publicCasesState: StateFlow<List<CaseReport>> = remoteCaseRepository
        .observePublicCases()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val tasksState: StateFlow<List<FieldTask>> = taskRepository
        .observeCoordinatorTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val activitiesState: StateFlow<List<CommunityActivity>> = communityRepository
        .observeNearbyActivities()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val userAccessState: StateFlow<UserAccessState> = authRepository
        .userAccessState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserAccessState.Guest
        )

    /**
     * Unfiltered domain markers converted from reports, tasks, and activities.
     */
    private val unfilteredMarkers: Flow<List<CivicMapMarker>> = combine(
        publicCasesState,
        tasksState,
        activitiesState
    ) { cases, tasks, activities ->
        val markers = mutableListOf<CivicMapMarker>()

        // 1. Reports
        cases.forEach { case ->
            if (case.latitude != 0.0 || case.longitude != 0.0) {
                val isResolved = case.status == CaseStatus.VERIFIED_CLEAN || case.status == CaseStatus.RESOLVED
                val markerType = if (isResolved) CivicMapType.RESOLVED_ISSUE else CivicMapType.REPORT

                val publicStatusLabel = when (case.status) {
                    CaseStatus.SUBMITTED, CaseStatus.UNDER_REVIEW -> "Reported"
                    CaseStatus.VERIFIED, CaseStatus.ASSIGNED, CaseStatus.IN_PROGRESS -> "Being Addressed"
                    CaseStatus.AWAITING_VERIFICATION -> "Under Verification"
                    CaseStatus.VERIFIED_CLEAN, CaseStatus.RESOLVED -> "Resolved"
                    else -> "Reported"
                }

                markers.add(
                    CivicMapMarker(
                        id = "report_${case.caseId}",
                        type = markerType,
                        latitude = case.latitude,
                        longitude = case.longitude,
                        title = case.category,
                        category = case.category,
                        areaName = if (case.approximateArea.isNotBlank()) case.approximateArea else "Sector Area",
                        statusLabel = publicStatusLabel,
                        rawStatus = case.status.name,
                        priority = CasePriority.MEDIUM,
                        timestamp = case.createdAt,
                        originalEntityId = case.caseId,
                        photoPath = case.photoStoragePath,
                        description = case.description
                    )
                )
            }
        }

        // 2. Field Tasks
        tasks.forEach { task ->
            if (task.latitude != 0.0 || task.longitude != 0.0) {
                markers.add(
                    CivicMapMarker(
                        id = "task_${task.taskId}",
                        type = CivicMapType.FIELD_TASK,
                        latitude = task.latitude,
                        longitude = task.longitude,
                        title = task.title,
                        category = task.category,
                        areaName = if (task.approximateArea.isNotBlank()) task.approximateArea else "Task Sector",
                        statusLabel = task.status.label,
                        rawStatus = task.status.name,
                        priority = task.priority,
                        timestamp = task.createdAt,
                        originalEntityId = task.taskId,
                        photoPath = task.beforePhotoPath,
                        description = task.description,
                        organizerOrAssignee = task.assignedToName
                    )
                )
            }
        }

        // 3. Community Activities
        activities.forEach { act ->
            if (act.latitude != 0.0 || act.longitude != 0.0) {
                markers.add(
                    CivicMapMarker(
                        id = "activity_${act.activityId}",
                        type = CivicMapType.COMMUNITY_ACTIVITY,
                        latitude = act.latitude,
                        longitude = act.longitude,
                        title = act.title,
                        category = act.category.label,
                        areaName = if (act.approximateArea.isNotBlank()) act.approximateArea else "Activity Spot",
                        statusLabel = act.status.label,
                        rawStatus = act.status.name,
                        priority = null,
                        timestamp = act.startDate,
                        originalEntityId = act.activityId,
                        description = act.description,
                        organizerOrAssignee = act.organizerName
                    )
                )
            }
        }

        markers
    }

    /**
     * Filtered markers with user distance calculations applied.
     */
    val allRawMarkers: StateFlow<List<CivicMapMarker>> = combine(
        unfilteredMarkers,
        _filterState,
        _searchQuery,
        _userLocation
    ) { rawMarkers: List<CivicMapMarker>, filter: MapFilterState, query: String, userLoc: Location? ->
        val updatedDistances = rawMarkers.map { m ->
            val dist = userLoc?.let { loc ->
                GeoUtils.calculateDistanceMeters(loc.latitude, loc.longitude, m.latitude, m.longitude)
            }
            m.copy(distanceMeters = dist)
        }

        val filtered = updatedDistances.filter { m ->
            val matchesType = filter.selectedTypes.contains(m.type)

            val matchesStatus = filter.selectedStatuses.isEmpty() || filter.selectedStatuses.any {
                m.statusLabel.contains(it, ignoreCase = true)
            }

            val matchesCategory = filter.selectedCategories.isEmpty() || filter.selectedCategories.any {
                m.category.contains(it, ignoreCase = true)
            }

            val matchesPriority = filter.selectedPriorities.isEmpty() || (m.priority != null && filter.selectedPriorities.contains(m.priority))

            val matchesSearch = query.isBlank() ||
                    m.title.contains(query, ignoreCase = true) ||
                    m.areaName.contains(query, ignoreCase = true) ||
                    m.category.contains(query, ignoreCase = true)

            val matchesTime = when (filter.selectedTimeRange) {
                TimeFilterRange.ALL_TIME -> true
                TimeFilterRange.TODAY -> (System.currentTimeMillis() - m.timestamp) <= 24 * 60 * 60 * 1000L
                TimeFilterRange.LAST_7_DAYS -> (System.currentTimeMillis() - m.timestamp) <= 7 * 24 * 60 * 60 * 1000L
                TimeFilterRange.LAST_30_DAYS -> (System.currentTimeMillis() - m.timestamp) <= 30 * 24 * 60 * 60 * 1000L
                TimeFilterRange.LAST_3_MONTHS -> (System.currentTimeMillis() - m.timestamp) <= 90 * 24 * 60 * 60 * 1000L
            }

            matchesType && matchesStatus && matchesCategory && matchesPriority && matchesSearch && matchesTime
        }

        filtered.sortedBy { it.distanceMeters ?: Float.MAX_VALUE }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    /**
     * Map markers stream (clustered if zoom level < 14f).
     */
    val mapDisplayMarkers: StateFlow<List<CivicMapMarker>> = combine(
        allRawMarkers,
        _mapUiState
    ) { rawMarkers, uiState ->
        val cameraZoom = (uiState as? MapUiState.Ready)?.cameraRegion?.zoom ?: 13.5f
        if (cameraZoom < 14f) {
            GeoUtils.clusterMarkers(rawMarkers, clusterRadiusMeters = 250.0)
        } else {
            rawMarkers
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    /**
     * Nearby duplicate or related candidates (within 500m) for the currently selected marker.
     */
    val nearbyMarkersForSelected: StateFlow<List<CivicMapMarker>> = combine(
        _selectedMarker,
        allRawMarkers
    ) { selected, all ->
        if (selected == null) emptyList()
        else GeoUtils.findNearbyMarkers(
            targetLat = selected.latitude,
            targetLng = selected.longitude,
            allMarkers = all,
            radiusMeters = 500f,
            excludeId = selected.id
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    /**
     * Geo Analytics overview state for coordinators.
     */
    val geoAnalyticsState: StateFlow<GeoAnalyticsState> = combine(
        allRawMarkers,
        _isAnalyticsMode
    ) { markers, isAnalytics ->
        if (!isAnalytics) {
            GeoAnalyticsState()
        } else {
            val openIssues = markers.count { it.type == CivicMapType.REPORT }
            val activeTasks = markers.count { it.type == CivicMapType.FIELD_TASK }
            val resolved = markers.count { it.type == CivicMapType.RESOLVED_ISSUE }

            val sectorGroup = markers.groupBy { it.areaName }
            val summaries = sectorGroup.map { (area, areaMarkers) ->
                val areaOpen = areaMarkers.count { it.type == CivicMapType.REPORT }
                val areaTasks = areaMarkers.count { it.type == CivicMapType.FIELD_TASK }
                val areaResolved = areaMarkers.count { it.type == CivicMapType.RESOLVED_ISSUE }
                val isRepeat = areaOpen >= 3

                SectorOverview(
                    sectorName = area,
                    openIssues = areaOpen,
                    activeTasks = areaTasks,
                    resolvedThisMonth = areaResolved,
                    isRepeatProblemArea = isRepeat,
                    recurringReportsCount = if (isRepeat) areaOpen else 0
                )
            }

            val repeatCount = summaries.count { it.isRepeatProblemArea }

            GeoAnalyticsState(
                totalOpenIssues = openIssues,
                totalActiveTasks = activeTasks,
                totalResolved = resolved,
                repeatProblemAreasCount = repeatCount,
                sectorSummaries = summaries
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GeoAnalyticsState()
    )

    init {
        checkMapProviderAvailability()
    }

    fun checkMapProviderAvailability() {
        _mapUiState.value = MapUiState.Ready(
            cameraRegion = defaultRegion,
            isMapsSdkAvailable = true
        )
    }

    fun setViewMode(mode: CivicMapViewMode) {
        _viewMode.value = mode
    }

    fun toggleAnalyticsMode() {
        _isAnalyticsMode.value = !_isAnalyticsMode.value
    }

    fun selectMarker(marker: CivicMapMarker?) {
        _selectedMarker.value = marker
        if (marker != null) {
            updateCameraRegion(
                MapCameraRegion(
                    latitude = marker.latitude,
                    longitude = marker.longitude,
                    zoom = 15.5f,
                    regionName = marker.areaName
                )
            )
        }
    }

    fun dismissSelectedMarker() {
        _selectedMarker.value = null
    }

    fun updateFilters(newFilters: MapFilterState) {
        _filterState.value = newFilters
    }

    fun clearFilters() {
        _filterState.value = MapFilterState()
    }

    fun prepareReportAtLocation(lat: Double, lng: Double, areaName: String, accuracyMeters: Float = 15f) {
        val isLow = accuracyMeters > 40f
        _locationConfirmationState.value = LocationConfirmationState(
            latitude = lat,
            longitude = lng,
            areaName = areaName,
            estimatedAccuracyMeters = accuracyMeters,
            isLowAccuracy = isLow
        )
    }

    fun dismissLocationConfirmation() {
        _locationConfirmationState.value = null
    }

    fun searchLocation(context: Context, query: String) {
        _searchQuery.value = query
        if (query.isBlank()) return

        when {
            query.equals("north", ignoreCase = true) || query.equals("north zone", ignoreCase = true) -> {
                updateCameraRegion(MapCameraRegion(28.6500, 77.2100, 14f, "North Zone"))
                return
            }
            query.equals("south", ignoreCase = true) || query.equals("south zone", ignoreCase = true) -> {
                updateCameraRegion(MapCameraRegion(28.5500, 77.2100, 14f, "South Zone"))
                return
            }
            query.equals("east", ignoreCase = true) || query.equals("east zone", ignoreCase = true) -> {
                updateCameraRegion(MapCameraRegion(28.6100, 77.2800, 14f, "East Zone"))
                return
            }
            query.equals("west", ignoreCase = true) || query.equals("west zone", ignoreCase = true) -> {
                updateCameraRegion(MapCameraRegion(28.6100, 77.1200, 14f, "West Zone"))
                return
            }
            query.equals("central", ignoreCase = true) -> {
                updateCameraRegion(MapCameraRegion(28.6139, 77.2090, 14f, "Central District"))
                return
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocationName(query, 1) { addresses ->
                        val addr = addresses.firstOrNull()
                        if (addr != null) {
                            val newRegion = MapCameraRegion(
                                latitude = addr.latitude,
                                longitude = addr.longitude,
                                zoom = 12f,
                                regionName = addr.locality ?: addr.adminArea ?: query
                            )
                            updateCameraRegion(newRegion)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(query, 1)
                    val addr = addresses?.firstOrNull()
                    if (addr != null) {
                        val newRegion = MapCameraRegion(
                            latitude = addr.latitude,
                            longitude = addr.longitude,
                            zoom = 12f,
                            regionName = addr.locality ?: addr.adminArea ?: query
                        )
                        updateCameraRegion(newRegion)
                    }
                }
            } catch (_: Exception) {
                // Safe fallback
            }
        }
    }

    fun updateCameraRegion(newRegion: MapCameraRegion) {
        val currentState = _mapUiState.value
        if (currentState is MapUiState.Ready) {
            _mapUiState.value = currentState.copy(cameraRegion = newRegion)
        } else if (currentState is MapUiState.LocationUnavailable) {
            _mapUiState.value = MapUiState.Ready(cameraRegion = newRegion)
        } else if (currentState is MapUiState.MapProviderUnavailable) {
            _mapUiState.value = currentState.copy(cameraRegion = newRegion)
        }
    }

    fun onRequestLocation(context: Context) {
        val hasCoarse = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasFine = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCoarse && !hasFine) {
            val currentRegion = getCurrentRegion()
            _mapUiState.value = MapUiState.LocationUnavailable(
                cameraRegion = currentRegion,
                reason = LocationUnavailableReason.PERMISSION_DENIED
            )
            return
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled) {
            val currentRegion = getCurrentRegion()
            _mapUiState.value = MapUiState.LocationUnavailable(
                cameraRegion = currentRegion,
                reason = LocationUnavailableReason.SERVICES_DISABLED
            )
            return
        }

        fetchUserLocation()
    }

    private fun fetchUserLocation() {
        val context = getApplication<Application>()
        val hasCoarse = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasFine = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCoarse && !hasFine) return

        val cancellationTokenSource = CancellationTokenSource()
        viewModelScope.launch {
            try {
                val priority = if (hasFine) Priority.PRIORITY_BALANCED_POWER_ACCURACY else Priority.PRIORITY_LOW_POWER
                fusedLocationClient.getCurrentLocation(priority, cancellationTokenSource.token)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            _userLocation.value = location
                            val userRegion = MapCameraRegion(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                zoom = 15f,
                                regionName = "Your Location"
                            )
                            val currentState = _mapUiState.value
                            val isSdkAvailable = (currentState as? MapUiState.Ready)?.isMapsSdkAvailable ?: true
                            _mapUiState.value = MapUiState.Ready(
                                cameraRegion = userRegion,
                                isLocationPermissionGranted = true,
                                isUserLocationActive = true,
                                isMapsSdkAvailable = isSdkAvailable
                            )
                        } else {
                            val currentRegion = getCurrentRegion()
                            _mapUiState.value = MapUiState.LocationUnavailable(
                                cameraRegion = currentRegion,
                                reason = LocationUnavailableReason.LOCATION_TIMEOUT
                            )
                        }
                    }
                    .addOnFailureListener {
                        val currentRegion = getCurrentRegion()
                        _mapUiState.value = MapUiState.LocationUnavailable(
                            cameraRegion = currentRegion,
                            reason = LocationUnavailableReason.UNAVAILABLE
                        )
                    }
            } catch (_: SecurityException) {
                val currentRegion = getCurrentRegion()
                _mapUiState.value = MapUiState.LocationUnavailable(
                    cameraRegion = currentRegion,
                    reason = LocationUnavailableReason.PERMISSION_DENIED
                )
            }
        }
    }

    fun dismissLocationError() {
        val currentRegion = getCurrentRegion()
        _mapUiState.value = MapUiState.Ready(
            cameraRegion = currentRegion,
            isLocationPermissionGranted = false,
            isUserLocationActive = false
        )
    }

    fun refreshData() {
        _lastSyncTimestamp.value = System.currentTimeMillis()
    }

    private fun getCurrentRegion(): MapCameraRegion {
        return when (val state = _mapUiState.value) {
            is MapUiState.Ready -> state.cameraRegion
            is MapUiState.LocationUnavailable -> state.cameraRegion
            is MapUiState.MapProviderUnavailable -> state.cameraRegion
            else -> defaultRegion
        }
    }
}
