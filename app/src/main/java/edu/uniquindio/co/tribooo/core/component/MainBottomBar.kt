package edu.uniquindio.co.tribooo.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class MainTab(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    FEED("Inicio", Icons.Outlined.Home, Icons.Rounded.Home),
    EXPLORE("Explorar", Icons.Outlined.Map, Icons.Rounded.Map),
    AGENDA("Agenda", Icons.Outlined.CalendarMonth, Icons.Rounded.CalendarMonth),
    PROFILE("Perfil", Icons.Outlined.Person, Icons.Rounded.Person)
}

/**
 * Barra inferior del prototipo: dos pestañas a cada lado y botón central para crear evento.
 * Las pestañas que no están en [enabledTabs] (aún sin pantalla) se muestran deshabilitadas.
 */
@Composable
fun MainBottomBar(
    selected: MainTab,
    onCreateClick: () -> Unit,
    enabledTabs: Set<MainTab>,
    onTabSelected: (MainTab) -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(80.dp)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabItem(MainTab.FEED, selected, enabledTabs, onTabSelected)
            TabItem(MainTab.EXPLORE, selected, enabledTabs, onTabSelected)
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Surface(
                    onClick = onCreateClick,
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .size(62.dp)
                        .offset(y = (-16).dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Add, contentDescription = "Crear evento", modifier = Modifier.size(29.dp))
                    }
                }
            }
            TabItem(MainTab.AGENDA, selected, enabledTabs, onTabSelected)
            TabItem(MainTab.PROFILE, selected, enabledTabs, onTabSelected)
        }
    }
}

@Composable
private fun RowScope.TabItem(
    tab: MainTab,
    selected: MainTab,
    enabledTabs: Set<MainTab>,
    onTabSelected: (MainTab) -> Unit
) {
    val isSelected = tab == selected
    val enabled = tab in enabledTabs
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .weight(1f)
            .alpha(if (enabled) 1f else 0.38f)
            .clickable(enabled = enabled) { onTabSelected(tab) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 60.dp, height = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) colors.secondaryContainer else colors.surfaceContainerLow),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) tab.selectedIcon else tab.icon,
                contentDescription = null,
                tint = if (isSelected) colors.onSecondaryContainer else colors.onSurfaceVariant,
                modifier = Modifier.size(23.dp)
            )
        }
        Text(
            text = tab.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) colors.onSurface else colors.onSurfaceVariant
        )
    }
}
