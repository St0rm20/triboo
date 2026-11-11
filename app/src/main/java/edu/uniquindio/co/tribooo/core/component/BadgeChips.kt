package edu.uniquindio.co.tribooo.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.uniquindio.co.tribooo.domain.usecase.user.Badge

/** Insignias ganadas, en chips que pasan a la siguiente línea si no caben. No muestra nada si no hay. */
@Composable
fun BadgeChips(badges: List<Badge>, modifier: Modifier = Modifier) {
    if (badges.isEmpty()) return
    val colors = MaterialTheme.colorScheme

    WrapRow(modifier = modifier, horizontalSpacing = 6.dp, verticalSpacing = 6.dp) {
        badges.forEach { badge ->
            Row(
                modifier = Modifier
                    .background(colors.tertiaryContainer, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = colors.onTertiaryContainer, modifier = Modifier.size(14.dp))
                Text(stringResource(badge.label), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.onTertiaryContainer)
            }
        }
    }
}
