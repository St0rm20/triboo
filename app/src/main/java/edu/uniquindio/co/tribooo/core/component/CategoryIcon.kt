package edu.uniquindio.co.tribooo.core.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.TheaterComedy
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.ui.graphics.vector.ImageVector
import edu.uniquindio.co.tribooo.domain.model.Category

/** Traduce el nombre de ícono guardado en [Category.icon] a un ImageVector de Compose. */
fun Category.iconVector(): ImageVector = when (icon) {
    "directions_bike" -> Icons.AutoMirrored.Rounded.DirectionsBike
    "theater_comedy" -> Icons.Rounded.TheaterComedy
    "school" -> Icons.Rounded.School
    "volunteer_activism" -> Icons.Rounded.VolunteerActivism
    "storefront" -> Icons.Rounded.Storefront
    else -> Icons.Rounded.Category
}
