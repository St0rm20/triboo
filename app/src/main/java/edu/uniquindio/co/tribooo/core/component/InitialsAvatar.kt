package edu.uniquindio.co.tribooo.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

/** Avatar con las iniciales de la persona sobre un fondo de color. */
@Composable
fun InitialsAvatar(
    initials: String,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    fontSize: TextUnit = 12.sp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, fontSize = fontSize, fontWeight = FontWeight.Medium, color = content)
    }
}

/** Par (fondo, contenido) para avatares: rota entre los contenedores primario, secundario y terciario. */
@Composable
fun avatarColors(seed: Int): Pair<Color, Color> {
    val colors = MaterialTheme.colorScheme
    return when (seed.absoluteValue % 3) {
        0 -> colors.primaryContainer to colors.onPrimaryContainer
        1 -> colors.secondaryContainer to colors.onSecondaryContainer
        else -> colors.tertiaryContainer to colors.onTertiaryContainer
    }
}
