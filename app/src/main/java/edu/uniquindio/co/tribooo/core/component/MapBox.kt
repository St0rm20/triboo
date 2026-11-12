package edu.uniquindio.co.tribooo.core.component

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.google.gson.JsonPrimitive
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotationGroup
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationSourceOptions
import com.mapbox.maps.plugin.annotation.ClusterOptions
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.DefaultViewportTransitionOptions
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.core.theme.categoryColors
import edu.uniquindio.co.tribooo.data.repository.memory.MockData
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Location

/** Pin de un evento en [location]. */
data class MapPin(val id: String, val category: Category, val location: Location)

private val ARMENIA: Point = MockData.currentLocation.toPoint()
private const val CLUSTER_RADIUS = 60L
private const val FOLLOW_USER_MAX_DURATION_MS = 1500L

/** Mapbox usa el orden (longitud, latitud); [Location] usa (latitud, longitud). */
private fun Location.toPoint(): Point = Point.fromLngLat(longitude, latitude)

private fun Point.toLocation(): Location = Location(latitude = latitude(), longitude = longitude())

// region Permiso de ubicación

private val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

private fun Context.hasLocationPermission(): Boolean =
    locationPermissions.any { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

/** Estado del permiso de ubicación: [hasPermission] es observable y [requestPermission] abre el diálogo del sistema. */
class LocationPermissionState internal constructor(
    private val granted: MutableState<Boolean>,
    private val launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
) {
    val hasPermission: Boolean get() = granted.value

    fun requestPermission() = launcher.launch(locationPermissions)
}

/** Recuerda el permiso de ubicación y lo relee al volver a primer plano (el usuario pudo cambiarlo en Ajustes). */
@Composable
fun rememberLocationPermissionState(onPermissionResult: (Boolean) -> Unit = {}): LocationPermissionState {
    val context = LocalContext.current
    val granted = remember { mutableStateOf(context.hasLocationPermission()) }
    val currentOnResult by rememberUpdatedState(onPermissionResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        val isGranted = result.values.any { it }
        granted.value = isGranted
        currentOnResult(isGranted)
    }
    LifecycleResumeEffect(Unit) {
        granted.value = context.hasLocationPermission()
        onPauseOrDispose { }
    }
    return remember(launcher) { LocationPermissionState(granted, launcher) }
}

// endregion

/**
 * Mapa de Mapbox reutilizable: encapsula la cámara, los marcadores (agrupados por categoría y en clusters),
 * el permiso y la ubicación del usuario, y los toques. Las pantallas solo deciden qué hacer con los callbacks.
 *
 * - [center] y [zoom] fijan la cámara inicial.
 * - [pins] se dibujan con el color de su categoría; tocar uno llama [onMarkerClick] con el id del evento.
 * - [selectedPoint] dibuja un marcador adicional, por ejemplo el punto que se está eligiendo.
 * - [interactive] en false deja el mapa de solo lectura y oculta el botón de ubicación; [myLocationButtonPadding]
 *   lo separa de los bordes (arriba a la derecha) cuando otros controles tapan la esquina.
 * - [onMapClick] recibe el punto tocado y [onUserLocation] cada nueva posición del usuario (con permiso).
 */
@Composable
fun MapBox(
    modifier: Modifier = Modifier,
    center: Point = ARMENIA,
    zoom: Double = 13.5,
    pins: List<MapPin> = emptyList(),
    selectedPoint: Point? = null,
    interactive: Boolean = true,
    showMyLocationButton: Boolean = true,
    myLocationButtonPadding: PaddingValues = PaddingValues(12.dp),
    onMarkerClick: (eventId: String) -> Unit = {},
    onMapClick: (Point) -> Unit = {},
    onUserLocation: (Location) -> Unit = {}
) {
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(zoom)
            center(center)
        }
    }
    var followUserRequest by remember { mutableIntStateOf(0) }
    val permission = rememberLocationPermissionState { granted -> if (granted) followUserRequest++ }
    val currentOnUserLocation by rememberUpdatedState(onUserLocation)
    val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)
    val currentOnMapClick by rememberUpdatedState(onMapClick)
    val marker = rememberIconImage(key = "map-marker", painter = painterResource(R.drawable.ic_map_marker))
    val pinsByCategory = remember(pins) { pins.groupBy { it.category } }

    Box(modifier = modifier) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
            onMapClickListener = OnMapClickListener { point ->
                currentOnMapClick(point)
                true
            }
        ) {
            // Con permiso se activa el puck aunque no se toque el botón; solo el botón mueve la cámara
            MapEffect(followUserRequest, permission.hasPermission) { mapView ->
                if (!permission.hasPermission) return@MapEffect
                mapView.location.updateSettings {
                    enabled = true
                    locationPuck = createDefault2DPuck(withBearing = true)
                    puckBearingEnabled = true
                    puckBearing = PuckBearing.COURSE
                }
                if (followUserRequest > 0) {
                    mapViewportState.transitionToFollowPuckState(
                        followPuckViewportStateOptions = FollowPuckViewportStateOptions.Builder().build(),
                        defaultTransitionOptions = DefaultViewportTransitionOptions.Builder()
                            .maxDurationMs(FOLLOW_USER_MAX_DURATION_MS)
                            .build()
                    )
                }
            }

            DisposableMapEffect(permission.hasPermission) { mapView ->
                val listener = OnIndicatorPositionChangedListener { point -> currentOnUserLocation(point.toLocation()) }
                if (permission.hasPermission) mapView.location.addOnIndicatorPositionChangedListener(listener)
                onDispose { mapView.location.removeOnIndicatorPositionChangedListener(listener) }
            }

            pinsByCategory.forEach { (category, categoryPins) ->
                key(category.id) {
                    val icon = rememberIconImage(
                        key = "map-marker-${category.id}",
                        painter = TintedPainter(painterResource(R.drawable.ic_map_marker), categoryColors(category).content)
                    )
                    val annotations = remember(categoryPins) {
                        categoryPins.map { pin ->
                            PointAnnotationOptions()
                                .withPoint(pin.location.toPoint())
                                .withData(JsonPrimitive(pin.id))
                        }
                    }
                    PointAnnotationGroup(
                        annotations = annotations,
                        annotationConfig = AnnotationConfig(
                            annotationSourceOptions = AnnotationSourceOptions(
                                clusterOptions = ClusterOptions(clusterRadius = CLUSTER_RADIUS)
                            )
                        )
                    ) {
                        iconImage = icon
                        iconAnchor = IconAnchor.BOTTOM
                        interactionsState.onClicked { annotation ->
                            annotation.getData()?.asString?.let(currentOnMarkerClick)
                            true
                        }
                    }
                }
            }

            selectedPoint?.let { point ->
                PointAnnotation(point = point) {
                    iconImage = marker
                    iconAnchor = IconAnchor.BOTTOM
                }
            }
        }

        if (!interactive) {
            // Solo lectura: este Box absorbe los toques para que el mapa no se mueva
            Box(Modifier.matchParentSize().pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent() } })
        } else if (showMyLocationButton) {
            SmallFloatingActionButton(
                onClick = {
                    if (permission.hasPermission) followUserRequest++ else permission.requestPermission()
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(myLocationButtonPadding)
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = stringResource(R.string.explore_my_location))
            }
        }
    }
}

/** Pinta [base] con un solo color, para dar a cada categoría su propio marcador. */
private class TintedPainter(private val base: Painter, private val color: Color) : Painter() {
    override val intrinsicSize: Size = base.intrinsicSize

    override fun DrawScope.onDraw() {
        with(base) { draw(size = size, colorFilter = ColorFilter.tint(color)) }
    }
}
