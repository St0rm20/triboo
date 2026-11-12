package edu.uniquindio.co.tribooo.core.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/** Miniatura cuadrada: rayas diagonales suaves y, si hay portada, la imagen encima. */
@Composable
fun EventThumbnail(imageUrl: String?, modifier: Modifier = Modifier, size: Dp = 62.dp) {
    val colors = MaterialTheme.colorScheme
    val stripe = colors.outlineVariant

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainerHigh)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 10.dp.toPx()
            var x = -this.size.height
            while (x < this.size.width) {
                drawLine(
                    color = stripe,
                    start = Offset(x, this.size.height),
                    end = Offset(x + this.size.height, 0f),
                    strokeWidth = 2.dp.toPx()
                )
                x += step
            }
        }
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
