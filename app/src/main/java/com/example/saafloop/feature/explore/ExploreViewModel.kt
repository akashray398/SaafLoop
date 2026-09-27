package com.example.saafloop.feature.explore

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saafloop.core.data.RemoteCaseRepository
import com.example.saafloop.core.data.RemoteCaseRepositoryImpl
import com.example.saafloop.core.model.CaseReport
import com.example.saafloop.feature.explore.model.LocationUnavailableReason
import com.example.saafloop.feature.explore.model.MapCameraRegion
import com.example.saafloop.feature.explore.model.MapUiState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExploreViewModel(application: Application) : AndroidViewModel(application) {

    private val remoteCaseRepository: RemoteCaseRepository = RemoteCaseRepositoryImpl(application)

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

    val publicCasesState: StateFlow<List<CaseReport>> = remoteCaseRepository
        .observePublicCases()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
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

    /**
     * Searches any city, state, or district name (e.g. "Bihar", "Patna", "Delhi", "Mumbai")
     * using Android's Geocoder service to update the map view position.
     */
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
                                zoom = 10f,
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
                            zoom = 10f,
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
                            val userRegion = MapCameraRegion(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                zoom = 15f,
                                regionName = "Your Approximate Location"
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

    private fun getCurrentRegion(): MapCameraRegion {
        return when (val state = _mapUiState.value) {
            is MapUiState.Ready -> state.cameraRegion
            is MapUiState.LocationUnavailable -> state.cameraRegion
            is MapUiState.MapProviderUnavailable -> state.cameraRegion
            else -> defaultRegion
        }
    }
}
