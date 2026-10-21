package edu.uniquindio.co.tribooo.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Separación entre las líneas de la cuadrícula que simula las calles. */
private val GridStep = 34.dp

/**
 * Espacio reservado para un mapa que todavía no está integrado:
 * una cuadrícula con el pin en el centro y una etiqueta que lo identifica.
 */
@Composable
fun MapPlaceholder(
    modifier: Modifier = Modifier,
    label: String = "mapa — solo lectura"
) {
    val colors = MaterialTheme.colorScheme
    val lineColor = colors.surfaceContainerHigh

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(colors.surfaceContainer)
            .drawBehind {
                val step = GridStep.toPx()
                val stroke = 1.dp.toPx()
                var x = 0f
                while (x < size.width) {
                    drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), stroke)
                    x += step
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(lineColor, Offset(0f, y), Offset(size.width, y), stroke)
                    y += step
                }
            }
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 17.dp, topEnd = 17.dp, bottomEnd = 17.dp, bottomStart = 12.dp),
            color = colors.primary,
            contentColor = colors.onPrimary,
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.Center)
                .size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Place, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }

        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = colors.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 10.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colors.surfaceContainerLowest)
                .padding(horizontal = 7.dp, vertical = 4.dp)
        )
    }
}
