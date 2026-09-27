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
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private const val OSM_STYLE_JSON = """
{
  "version": 8,
  "sources": {
    "osm-raster-tiles": {
      "type": "raster",
      "tiles": [
        "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
      ],
      "tileSize": 256,
      "attribution": "© OpenStreetMap contributors"
    }
  },
  "layers": [
    {
      "id": "osm-raster-layer",
      "type": "raster",
      "source": "osm-raster-tiles",
      "minzoom": 0,
      "maxzoom": 19
    }
  ]
}
"""

/**
 * 100% Free, OpenSource MapLibre + OpenStreetMap Compose Map View.
 *
 * NOTE:
 * Renders official OpenStreetMap raster tiles with full street, city, state,
 * and natural geography detail. Requires NO API keys or paid services.
 */
@Composable
fun MapLibreMapView(
    latitude: Double,
    longitude: Double,
    zoom: Double = 13.5,
    publicCases: List<CaseReport> = emptyList(),
    modifier: Modifier = Modifier
) {
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
                map.setStyle(Style.Builder().fromJson(OSM_STYLE_JSON)) { _ ->
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
                    publicCases.forEach { caseReport ->
                        if (caseReport.latitude != 0.0 || caseReport.longitude != 0.0) {
                            map.addMarker(
                                MarkerOptions()
                                    .position(LatLng(caseReport.latitude, caseReport.longitude))
                                    .title(caseReport.category)
                                    .snippet("${caseReport.status.label} • ${caseReport.approximateArea}")
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    )
}
