package edu.uniquindio.co.tribooo.features.myevents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.uniquindio.co.tribooo.core.component.iconVector
import edu.uniquindio.co.tribooo.core.theme.categoryColors
import edu.uniquindio.co.tribooo.core.util.eventWhenLabel
import edu.uniquindio.co.tribooo.domain.model.EventStatus

@Composable
fun MyEventsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEventDetail: (eventId: String) -> Unit,
    onNavigateToEventStatus: (eventId: String) -> Unit,
    snackbarMessage: String?,
    onSnackbarShown: () -> Unit,
    viewModel: MyEventsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val colors = MaterialTheme.colorScheme

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            MyEventsTopBar(onBack = onNavigateBack)
            MyEventsTabs(selected = state.selectedTab, onSelect = viewModel::onTabSelected)

            if (state.events.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No tienes eventos en este estado.",
                        fontSize = 15.sp,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.events, key = { it.event.id }) { item ->
                        MyEventCard(
                            item = item,
                            onClick = {
                                // Solo los activos tienen detalle de evento; el resto, el de su estado
                                if (item.status == EventStatus.ACTIVO) {
                                    onNavigateToEventDetail(item.event.id)
                                } else {
                                    onNavigateToEventStatus(item.event.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Mensaje que llega de otra pantalla (p. ej. el aviso provisional del detalle por estado)
    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            snackbarHostState.showSnackbar(snackbarMessage)
            onSnackbarShown()
        }
    }
}

@Composable
private fun MyEventsTopBar(onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainer)
            .statusBarsPadding()
            .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver", tint = colors.onSurface)
        }
        Text("Mis eventos", fontSize = 20.sp, color = colors.onSurface)
    }
}

/** Pestañas de texto con un indicador inferior en la activa. */
@Composable
private fun MyEventsTabs(selected: MyEventsTab, onSelect: (MyEventsTab) -> Unit) {
    val colors = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxWidth().background(colors.surfaceContainer)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            MyEventsTab.entries.forEach { tab ->
                val isSelected = tab == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tab.label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) colors.primary else colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 14.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(
                                if (isSelected) colors.primary else Color.Transparent,
                                RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                            )
                    )
                }
            }
        }
        HorizontalDivider(color = colors.outlineVariant)
    }
}

@Composable
private fun MyEventCard(item: MyEventItem, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val event = item.event
    val isRejected = item.status == EventStatus.RECHAZADO
    val linkColor = if (isRejected) colors.error else colors.primary

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = colors.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(status = item.status, label = item.statusLabel)
                CategoryBadge(item)
            }

            Text(
                text = event.title,
                fontSize = 17.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = eventWhenLabel(event.startDate, event.endDate),
                fontSize = 13.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                text = "${event.place} · ${event.neighborhood}",
                fontSize = 13.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = item.summary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                modifier = Modifier.padding(top = 10.dp)
            )

            HorizontalDivider(color = colors.outlineVariant, modifier = Modifier.padding(top = 12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.footer,
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isRejected) "Ver motivo del rechazo" else "Ver detalle",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = linkColor
                    )
                    Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = linkColor, modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}

/** Etiqueta del estado del evento, con el color propio de cada estado. */
@Composable
private fun StatusBadge(status: EventStatus, label: String) {
    val colors = MaterialTheme.colorScheme
    val (container, content) = when (status) {
        EventStatus.ACTIVO -> colors.primaryContainer to colors.onPrimaryContainer
        EventStatus.PENDIENTE -> colors.tertiaryContainer to colors.onTertiaryContainer
        EventStatus.FINALIZADO -> colors.surfaceVariant to colors.onSurfaceVariant
        EventStatus.RECHAZADO -> colors.errorContainer to colors.onErrorContainer
    }
    Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = content,
        modifier = Modifier
            .background(container, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/** Categoría del evento (ícono y nombre) con los colores de la categoría. */
@Composable
private fun CategoryBadge(item: MyEventItem) {
    val category = item.event.category
    val tones = categoryColors(category)
    Row(
        modifier = Modifier
            .background(tones.container, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(category.iconVector(), contentDescription = null, tint = tones.content, modifier = Modifier.size(14.dp))
        Text(category.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = tones.content)
    }
}
