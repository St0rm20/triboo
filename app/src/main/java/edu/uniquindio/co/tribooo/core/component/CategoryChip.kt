package edu.uniquindio.co.tribooo.core.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.uniquindio.co.tribooo.core.theme.categoryColors
import edu.uniquindio.co.tribooo.domain.model.Category

/** Chip seleccionable de categoría: con borde cuando no está seleccionado y tono de la categoría cuando sí. */
@Composable
fun CategoryChip(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 36.dp,
    cornerRadius: Dp = 12.dp
) {
    val tones = categoryColors(category)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(cornerRadius),
        color = if (selected) tones.container else Color.Transparent,
        contentColor = if (selected) tones.content else MaterialTheme.colorScheme.onSurface,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.height(height)
    ) {
        Row(
            modifier = Modifier.padding(start = 11.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(category.iconVector(), contentDescription = null, modifier = Modifier.size(18.dp))
            Text(category.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
