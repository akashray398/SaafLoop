package com.example.saafloop.feature.explore.model

data class MapCameraRegion(
    val latitude: Double,
    val longitude: Double,
    val zoom: Float = 14f,
    val regionName: String = "Central Region"
)

sealed interface MapUiState {
    data object Loading : MapUiState

    data class Ready(
        val cameraRegion: MapCameraRegion,
        val isLocationPermissionGranted: Boolean = false,
        val isUserLocationActive: Boolean = false,
        val isMapsSdkAvailable: Boolean = true
    ) : MapUiState

    data class LocationUnavailable(
        val cameraRegion: MapCameraRegion,
        val reason: LocationUnavailableReason
    ) : MapUiState

    data class MapProviderUnavailable(
        val cameraRegion: MapCameraRegion,
        val reason: String
    ) : MapUiState
}

enum class LocationUnavailableReason {
    PERMISSION_DENIED,
    SERVICES_DISABLED,
    LOCATION_TIMEOUT,
    UNAVAILABLE
}
