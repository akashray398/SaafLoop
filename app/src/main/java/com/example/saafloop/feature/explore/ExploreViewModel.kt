package com.example.saafloop.feature.explore

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saafloop.BuildConfig
import com.example.saafloop.feature.explore.model.LocationUnavailableReason
import com.example.saafloop.feature.explore.model.MapCameraRegion
import com.example.saafloop.feature.explore.model.MapUiState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreViewModel(application: Application) : AndroidViewModel(application) {

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

    init {
        checkMapProviderAvailability()
    }

    fun checkMapProviderAvailability() {
        val apiKey: String = BuildConfig.MAPS_API_KEY
        val isKeyConfigured = apiKey.isNotBlank() &&
                !apiKey.equals("DEFAULT_MAPS_KEY_MISSING", ignoreCase = true) &&
                !apiKey.contains("YOUR_GOOGLE_MAPS_API_KEY", ignoreCase = true)

        _mapUiState.value = MapUiState.Ready(
            cameraRegion = defaultRegion,
            isMapsSdkAvailable = isKeyConfigured
        )
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            val newRegion = when {
                query.contains("north", ignoreCase = true) -> MapCameraRegion(28.6500, 77.2100, 14f, "North Zone")
                query.contains("south", ignoreCase = true) -> MapCameraRegion(28.5500, 77.2100, 14f, "South Zone")
                query.contains("east", ignoreCase = true) -> MapCameraRegion(28.6100, 77.2800, 14f, "East Zone")
                query.contains("west", ignoreCase = true) -> MapCameraRegion(28.6100, 77.1200, 14f, "West Zone")
                else -> MapCameraRegion(28.6139, 77.2090, 14f, query.trim())
            }
            updateCameraRegion(newRegion)
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
