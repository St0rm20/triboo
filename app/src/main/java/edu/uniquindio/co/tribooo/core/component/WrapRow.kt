package edu.uniquindio.co.tribooo.core.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Fila que pasa sus hijos a la siguiente línea cuando no caben en el ancho disponible. */
@Composable
fun WrapRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val hGap = horizontalSpacing.roundToPx()
        val vGap = verticalSpacing.roundToPx()
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }

        // Posición (x, y) de cada hijo
        val positions = ArrayList<Pair<Int, Int>>(placeables.size)
        var x = 0
        var y = 0
        var lineHeight = 0
        placeables.forEach { placeable ->
            if (x > 0 && x + placeable.width > constraints.maxWidth) {
                x = 0
                y += lineHeight + vGap
                lineHeight = 0
            }
            positions += x to y
            x += placeable.width + hGap
            lineHeight = maxOf(lineHeight, placeable.height)
        }

        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else placeables.sumOf { it.width }
        layout(width, y + lineHeight) {
            placeables.forEachIndexed { i, placeable ->
                placeable.placeRelative(positions[i].first, positions[i].second)
            }
        }
    }
}
