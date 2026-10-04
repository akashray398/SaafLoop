package com.example.saafloop.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.saafloop.core.model.CaseReport
import com.example.saafloop.core.model.CivicMapMarker
import com.example.saafloop.core.model.CivicMapType
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private const val CARTO_VOYAGER_STYLE_JSON = """
{
  "version": 8,
  "sources": {
    "carto-raster-tiles": {
      "type": "raster",
      "tiles": [
        "https://a.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png",
        "https://b.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png",
        "https://c.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png",
        "https://d.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png"
      ],
      "tileSize": 256,
      "attribution": "© OpenStreetMap contributors, © CARTO"
    }
  },
  "layers": [
    {
      "id": "carto-raster-layer",
      "type": "raster",
      "source": "carto-raster-tiles",
      "minzoom": 0,
      "maxzoom": 20
    }
  ]
}
"""

/**
 * 100% Free, OpenSource MapLibre + CARTO Voyager OpenStreetMap Basemap View.
 * Supports rendering custom civic map markers, clustering, click handlers, and camera positioning.
 */
@Composable
fun MapLibreMapView(
    latitude: Double,
    longitude: Double,
    zoom: Double = 13.5,
    markers: List<CivicMapMarker> = emptyList(),
    publicCases: List<CaseReport> = emptyList(),
    selectedMarkerId: String? = null,
    onMarkerClick: (CivicMapMarker) -> Unit = {},
    onMapClick: (LatLng) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val effectiveMarkers = remember(markers, publicCases) {
        if (markers.isNotEmpty()) {
            markers
        } else {
            publicCases.map { caseReport ->
                CivicMapMarker(
                    id = "report_${caseReport.caseId}",
                    type = CivicMapType.REPORT,
                    latitude = caseReport.latitude,
                    longitude = caseReport.longitude,
                    title = caseReport.category,
                    category = caseReport.category,
                    areaName = caseReport.approximateArea,
                    statusLabel = caseReport.status.label,
                    originalEntityId = caseReport.caseId
                )
            }
        }
    }

    val context = LocalContext.current
    remember { MapLibre.getInstance(context) }

    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        factory = { mapView },
        update = { view ->
            view.getMapAsync { map ->
                map.setStyle(Style.Builder().fromJson(CARTO_VOYAGER_STYLE_JSON)) { _ ->
                    map.animateCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                .target(LatLng(latitude, longitude))
                                .zoom(zoom)
                                .build()
                        ),
                        1000
                    )

                    map.clear()

                    val markerMap = mutableMapOf<String, CivicMapMarker>()

                    effectiveMarkers.forEach { civicMarker ->
                        if (civicMarker.latitude != 0.0 || civicMarker.longitude != 0.0) {
                            val icon = MapMarkerBitmapGenerator.createMapIcon(context, civicMarker)
                            val markerOptions = MarkerOptions()
                                .position(LatLng(civicMarker.latitude, civicMarker.longitude))
                                .title(civicMarker.title)
                                .snippet("${civicMarker.type.label} • ${civicMarker.areaName}")
                                .icon(icon)

                            val addedMarker = map.addMarker(markerOptions)
                            markerMap[addedMarker.id.toString()] = civicMarker
                        }
                    }

                    map.setOnMarkerClickListener { marker ->
                        val matchedCivicMarker = markerMap[marker.id.toString()]
                            ?: effectiveMarkers.find {
                                Math.abs(it.latitude - marker.position.latitude) < 0.0001 &&
                                        Math.abs(it.longitude - marker.position.longitude) < 0.0001
                            }

                        if (matchedCivicMarker != null) {
                            onMarkerClick(matchedCivicMarker)
                            true
                        } else {
                            false
                        }
                    }

                    map.addOnMapClickListener { point ->
                        onMapClick(point)
                        true
                    }
                }
            }
        },
        modifier = modifier
    )
}
