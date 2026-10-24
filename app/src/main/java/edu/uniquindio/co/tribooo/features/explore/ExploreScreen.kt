package edu.uniquindio.co.tribooo.features.explore

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.uniquindio.co.tribooo.core.component.CategoryChip
import edu.uniquindio.co.tribooo.core.component.EventCard
import edu.uniquindio.co.tribooo.core.component.EventFiltersSheet
import edu.uniquindio.co.tribooo.core.component.EventThumbnail
import edu.uniquindio.co.tribooo.core.component.FeedEventItem
import edu.uniquindio.co.tribooo.core.component.MainBottomBar
import edu.uniquindio.co.tribooo.core.component.MainTab
import edu.uniquindio.co.tribooo.core.util.eventWhenLabel
import edu.uniquindio.co.tribooo.core.util.kmLabel

@Composable
fun ExploreScreen(
    onNavigateToFeed: () -> Unit,
    onNavigateToAgenda: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCreateEvent: () -> Unit,
    onNavigateToEventDetail: (eventId: String) -> Unit,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            MainBottomBar(
                selected = MainTab.EXPLORE,
                onCreateClick = onNavigateToCreateEvent,
                enabledTabs = setOf(MainTab.FEED, MainTab.EXPLORE, MainTab.AGENDA, MainTab.PROFILE),
                onTabSelected = { tab ->
                    when (tab) {
                        MainTab.FEED -> onNavigateToFeed()
                        MainTab.AGENDA -> onNavigateToAgenda()
                        MainTab.PROFILE -> onNavigateToProfile()
                        else -> Unit
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
            when (state.view) {
                ExploreView.MAP -> {
                    EventMapPlaceholder(
                        pins = state.pins,
                        selectedId = state.selected?.event?.id,
                        onPinClick = viewModel::onPinClick,
                        modifier = Modifier.fillMaxSize()
                    )
                    TopBar(state, viewModel, modifier = Modifier.align(Alignment.TopCenter))
                    val selected = state.selected
                    if (selected == null) {
                        EmptyResults(onClearFilters = viewModel::clearFilters, modifier = Modifier.align(Alignment.Center))
                    } else {
                        Column(modifier = Modifier.align(Alignment.BottomCenter), horizontalAlignment = Alignment.End) {
                            RecenterButton(onClick = viewModel::onRecenter)
                            SelectedEventSheet(
                                item = selected,
                                onOpenDetail = { onNavigateToEventDetail(selected.event.id) },
                                onToggleAttendance = { viewModel.toggleAttendance(selected) }
                            )
                        }
                    }
                }
                ExploreView.LIST -> Column(modifier = Modifier.fillMaxSize()) {
                    TopBar(state, viewModel, modifier = Modifier)
                    EventList(
                        events = state.events,
                        onEventClick = onNavigateToEventDetail,
                        onToggleAttendance = viewModel::toggleAttendance,
                        onClearFilters = viewModel::clearFilters
                    )
                }
            }
        }
    }

    if (state.showFilters) {
        EventFiltersSheet(
            filters = state.filters,
            categories = state.categories,
            onApply = viewModel::onApplyFilters,
            onDismiss = viewModel::onDismissFilters
        )
    }
}

/** Búsqueda, botón de filtros con su contador y alternador Mapa / Lista. */
@Composable
private fun TopBar(state: ExploreUiState, viewModel: ExploreViewModel, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(colors.surfaceContainerHigh)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = colors.onSurfaceVariant)
                BasicTextField(
                    value = state.filters.query,
                    onValueChange = viewModel::onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 15.sp, color = colors.onSurface),
                    cursorBrush = SolidColor(colors.primary),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (state.filters.query.isEmpty()) {
                            Text("Buscar en el mapa", fontSize = 15.sp, color = colors.onSurfaceVariant)
                        }
                        inner()
                    }
                )
            }
            Box {
                Surface(
                    onClick = viewModel::onOpenFilters,
                    shape = CircleShape,
                    color = colors.surfaceContainerHigh,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Tune, contentDescription = "Filtros", tint = colors.onSurface)
                    }
                }
                if (state.filters.activeCount > 0) {
                    Badge(modifier = Modifier.align(Alignment.TopEnd)) { Text(state.filters.activeCount.toString()) }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ViewOption("Mapa", state.view == ExploreView.MAP) { viewModel.onViewChange(ExploreView.MAP) }
            ViewOption("Lista", state.view == ExploreView.LIST) { viewModel.onViewChange(ExploreView.LIST) }
        }
    }
}

@Composable
private fun ViewOption(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) colors.secondaryContainer else colors.surfaceContainerHigh,
        contentColor = if (selected) colors.onSecondaryContainer else colors.onSurface,
        border = if (selected) null else BorderStroke(1.dp, colors.outlineVariant)
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp))
    }
}

@Composable
private fun RecenterButton(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = colors.surfaceContainerHigh,
        shadowElevation = 4.dp,
        modifier = Modifier.padding(end = 16.dp, bottom = 12.dp).size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.MyLocation, contentDescription = "Mi ubicación", tint = colors.primary)
        }
    }
}

/** Tarjeta anclada abajo con el evento seleccionado en el mapa. */
@Composable
private fun SelectedEventSheet(item: FeedEventItem, onOpenDetail: () -> Unit, onToggleAttendance: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val event = item.event

    Surface(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = colors.surfaceContainerLow,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EventThumbnail(imageUrl = event.imageUrl, size = 74.dp)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    CategoryChip(category = event.category, selected = true, onClick = onOpenDetail, height = 28.dp, cornerRadius = 8.dp)
                    Text(
                        text = event.title,
                        fontSize = 16.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${eventWhenLabel(event.startDate, event.endDate)} · ${item.distanceKm.kmLabel().replace('.', ',')}",
                        fontSize = 13.sp,
                        color = colors.onSurfaceVariant
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onOpenDetail, modifier = Modifier.weight(1f).height(44.dp)) { Text("Ver detalle") }
                AttendanceButton(item = item, onClick = onToggleAttendance, modifier = Modifier.weight(1f))
            }
        }
    }
}

/** "Voy", "Ya voy" (tonal) o "Lleno" (deshabilitado si el usuario no asiste). */
@Composable
private fun AttendanceButton(item: FeedEventItem, onClick: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val full = item.isFull && !item.isAttending

    Surface(
        onClick = onClick,
        enabled = !full,
        shape = RoundedCornerShape(14.dp),
        color = when {
            item.isAttending -> colors.secondaryContainer
            full -> colors.surfaceContainerHigh
            else -> colors.primary
        },
        contentColor = when {
            item.isAttending -> colors.onSecondaryContainer
            full -> colors.onSurfaceVariant
            else -> colors.onPrimary
        },
        modifier = modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (!full && !item.isAttending) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Text(
                text = when {
                    item.isAttending -> "Ya voy"
                    full -> "Lleno"
                    else -> "Voy"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun EventList(
    events: List<FeedEventItem>,
    onEventClick: (String) -> Unit,
    onToggleAttendance: (FeedEventItem) -> Unit,
    onClearFilters: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = if (events.size == 1) "1 evento cerca" else "${events.size} eventos cerca",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 2.dp, vertical = 8.dp)
            )
        }
        items(events, key = { it.event.id }) { item ->
            EventCard(
                item = item,
                onClick = { onEventClick(item.event.id) },
                onToggleAttendance = { onToggleAttendance(item) }
            )
        }
        if (events.isEmpty()) {
            item { EmptyResults(onClearFilters = onClearFilters, modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp)) }
        }
    }
}

@Composable
private fun EmptyResults(onClearFilters: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme

    Column(modifier = modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Rounded.EventBusy, contentDescription = null, tint = colors.outline, modifier = Modifier.size(44.dp))
        Text(
            "No hay eventos con estos filtros",
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, bottom = 16.dp)
        )
        OutlinedButton(onClick = onClearFilters, shape = RoundedCornerShape(14.dp)) { Text("Limpiar filtros") }
    }
}
