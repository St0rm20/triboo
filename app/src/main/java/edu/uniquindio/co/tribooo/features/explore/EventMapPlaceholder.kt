package edu.uniquindio.co.tribooo.features.explore

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import edu.uniquindio.co.tribooo.core.component.iconVector
import edu.uniquindio.co.tribooo.core.theme.categoryColors
import edu.uniquindio.co.tribooo.core.util.MapPoint
import edu.uniquindio.co.tribooo.domain.model.Category
import kotlin.math.roundToInt

private val GRID_STEP = 46.dp
private val PIN_SIZE = 38.dp
private val SELECTED_PIN_SIZE = 48.dp

/** La punta de una gota de lado L (cuadrado girado 45°) queda a L·√2/2 bajo su centro. */
private const val PIN_TIP_RATIO = 0.7071f

/** Pin de un evento; [point] ya viene proyectado al mapa (ver `MapProjection`). */
data class MapPin(val id: String, val category: Category, val point: MapPoint)

/**
 * Mapa simulado: cuadrícula, una calle diagonal y un parque dibujados, con un pin por evento.
 * Solo recibe pines y la selección, así que luego puede cambiarse por un mapa real.
 */
@Composable
fun EventMapPlaceholder(
    pins: List<MapPin>,
    selectedId: String?,
    onPinClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val gridColor = colors.outlineVariant
    val streetColor = colors.surfaceContainerHighest

    BoxWithConstraints(modifier = modifier.background(colors.surfaceContainerLow)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = GRID_STEP.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += step
            }
            drawLine(streetColor, Offset(0f, size.height * 0.78f), Offset(size.width, size.height * 0.3f), strokeWidth = 16.dp.toPx())
        }

        Box(
            modifier = Modifier
                .offset(x = maxWidth * 0.56f, y = maxHeight * 0.42f)
                .size(width = maxWidth * 0.3f, height = maxHeight * 0.12f)
                .background(colors.tertiaryContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("parque de la vida", fontSize = 11.sp, color = colors.onTertiaryContainer)
        }

        Text(
            text = "mapa — placeholder",
            fontSize = 11.sp,
            color = colors.onSurfaceVariant,
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
        )

        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val heightPx = with(LocalDensity.current) { maxHeight.toPx() }
        pins.forEach { pin ->
            val selected = pin.id == selectedId
            val size = if (selected) SELECTED_PIN_SIZE else PIN_SIZE
            val sizePx = with(LocalDensity.current) { size.toPx() }
            Pin(
                category = pin.category,
                selected = selected,
                size = size,
                onClick = { onPinClick(pin.id) },
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (pin.point.x * widthPx - sizePx / 2).roundToInt(),
                            (pin.point.y * heightPx - sizePx * PIN_TIP_RATIO - sizePx / 2).roundToInt()
                        )
                    }
                    // El seleccionado se dibuja encima de los demás
                    .zIndex(if (selected) 1f else 0f)
            )
        }
    }
}

/** Gota: cuadrado redondeado salvo la esquina inferior izquierda, girado para que la punta apunte abajo. */
@Composable
private fun Pin(category: Category, selected: Boolean, size: Dp, onClick: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val tones = categoryColors(category)
    val container = if (selected) colors.primary else tones.container
    val content = if (selected) colors.onPrimary else tones.content

    Box(
        modifier = modifier
            .size(size)
            .rotate(-45f)
            .shadow(4.dp, PIN_SHAPE)
            .background(container, PIN_SHAPE)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = category.iconVector(),
            contentDescription = category.name,
            tint = content,
            modifier = Modifier.size(size * 0.5f).rotate(45f)
        )
    }
}

private val PIN_SHAPE = RoundedCornerShape(topStartPercent = 50, topEndPercent = 50, bottomEndPercent = 50, bottomStartPercent = 0)
