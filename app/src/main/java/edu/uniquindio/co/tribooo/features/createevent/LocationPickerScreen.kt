package edu.uniquindio.co.tribooo.features.createevent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mapbox.geojson.Point
import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.core.component.MapBox
import edu.uniquindio.co.tribooo.domain.model.Location

/** Pantalla completa para tocar el mapa y elegir el punto exacto del evento, que parte de [initialLocation]. */
@Composable
fun LocationPickerScreen(
    initialLocation: Location,
    onConfirm: (Location) -> Unit,
    onDismiss: () -> Unit
) {
    // Point.fromLngLat lleva primero la longitud
    var pickedPoint by remember { mutableStateOf(Point.fromLngLat(initialLocation.longitude, initialLocation.latitude)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Box(modifier = Modifier.fillMaxSize()) {
                MapBox(
                    modifier = Modifier.fillMaxSize(),
                    center = pickedPoint,
                    zoom = 16.0,
                    selectedPoint = pickedPoint,
                    onMapClick = { pickedPoint = it }
                )
                Button(
                    onClick = { onConfirm(Location(latitude = pickedPoint.latitude(), longitude = pickedPoint.longitude())) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(stringResource(R.string.location_picker_confirm))
                }
            }
        }
    }
}
