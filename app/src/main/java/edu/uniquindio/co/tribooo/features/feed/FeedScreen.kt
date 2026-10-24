package edu.uniquindio.co.tribooo.features.feed

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.uniquindio.co.tribooo.core.component.CategoryChip
import edu.uniquindio.co.tribooo.core.component.EventCard
import edu.uniquindio.co.tribooo.core.component.EventFiltersSheet
import edu.uniquindio.co.tribooo.core.component.MainBottomBar
import edu.uniquindio.co.tribooo.core.component.MainTab
import edu.uniquindio.co.tribooo.core.util.APP_ZONE
import edu.uniquindio.co.tribooo.core.util.longLabel
import edu.uniquindio.co.tribooo.domain.model.Category
import java.time.LocalDate

@Composable
fun FeedScreen(
    onNavigateToCreateEvent: () -> Unit,
    onNavigateToModeration: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToAgenda: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToEventDetail: (eventId: String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    snackbarMessage: String?,
    onSnackbarShown: () -> Unit,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            MainBottomBar(
                selected = MainTab.FEED,
                onCreateClick = if (state.isAdmin) onNavigateToModeration else onNavigateToCreateEvent,
                enabledTabs = setOf(MainTab.FEED, MainTab.EXPLORE, MainTab.AGENDA, MainTab.PROFILE),
                onTabSelected = { tab ->
                    when (tab) {
                        MainTab.EXPLORE -> onNavigateToExplore()
                        MainTab.AGENDA -> onNavigateToAgenda()
                        MainTab.PROFILE -> onNavigateToProfile()
                        else -> Unit
                    }
                },
                isAdmin = state.isAdmin,
                moderationBadge = state.pendingModeration
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .statusBarsPadding()
        ) {
            FeedHeader(
                query = state.query,
                onQueryChange = viewModel::onQueryChange,
                categories = state.categories,
                selectedCategories = state.selectedCategories,
                onToggleCategory = viewModel::toggleCategory,
                activeFilters = state.filters.activeCount,
                onOpenFilters = viewModel::onOpenFilters,
                unreadNotifications = state.unreadNotifications,
                onNavigateToNotifications = onNavigateToNotifications
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val count = state.events.size
                        Text(
                            text = if (count == 1) "1 evento cerca" else "$count eventos cerca",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Más cercanos primero",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(state.events, key = { it.event.id }) { item ->
                    EventCard(
                        item = item,
                        onClick = { onNavigateToEventDetail(item.event.id) },
                        onToggleAttendance = { viewModel.toggleAttendance(item) }
                    )
                }

                if (state.events.isEmpty()) {
                    item { EmptyFeed(onClearFilters = viewModel::clearFilters) }
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

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            snackbarHostState.showSnackbar(snackbarMessage)
            onSnackbarShown()
        }
    }
}

@Composable
private fun FeedHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    categories: List<Category>,
    selectedCategories: Set<String>,
    onToggleCategory: (String) -> Unit,
    activeFilters: Int,
    onOpenFilters: () -> Unit,
    unreadNotifications: Int,
    onNavigateToNotifications: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 8.dp)) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 2.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = LocalDate.now(APP_ZONE).longLabel().uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    color = colors.primary
                )
                Text(
                    text = "Cerca de ti",
                    fontSize = 26.sp,
                    lineHeight = 32.sp,
                    color = colors.onSurface,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            NotificationsBell(count = unreadNotifications, onClick = onNavigateToNotifications)
        }

        // Barra de búsqueda
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(colors.surfaceContainer)
                .padding(start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = colors.onSurfaceVariant)
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 15.sp, color = colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text("Buscar eventos, barrios, lugares", fontSize = 15.sp, color = colors.onSurfaceVariant)
                    }
                    inner()
                }
            )
        }

        // Chips de categorías
        LazyRow(
            contentPadding = PaddingValues(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "filters") { FiltersChip(count = activeFilters, onClick = onOpenFilters) }
            items(categories, key = { it.key }) { category ->
                CategoryChip(
                    category = category,
                    selected = category.key in selectedCategories,
                    onClick = { onToggleCategory(category.key) }
                )
            }
        }
    }
}

/** Chip "Filtros" con el número de filtros activos; toma el tono secundario cuando hay alguno. */
@Composable
private fun FiltersChip(count: Int, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val active = count > 0

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (active) colors.secondaryContainer else Color.Transparent,
        contentColor = if (active) colors.onSecondaryContainer else colors.onSurface,
        border = if (active) null else BorderStroke(1.dp, colors.outline),
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 11.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(if (active) "Filtros · $count" else "Filtros", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun EmptyFeed(onClearFilters: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.EventBusy, contentDescription = null, tint = colors.outlineVariant, modifier = Modifier.size(44.dp))
        Text(
            "Sin eventos con esos filtros",
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            "Prueba con otra búsqueda o quitando categorías.",
            fontSize = 14.sp,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )
        Spacer(Modifier.height(18.dp))
        OutlinedButton(onClick = onClearFilters, shape = RoundedCornerShape(14.dp)) {
            Text("Limpiar filtros")
        }
    }
}

/** Botón circular de campana; muestra la cantidad de avisos sin leer. */
@Composable
private fun NotificationsBell(count: Int, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Box {
        Surface(onClick = onClick, shape = CircleShape, color = colors.surfaceContainer, modifier = Modifier.size(44.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Notifications, contentDescription = "Notificaciones", tint = colors.onSurface)
            }
        }
        if (count > 0) {
            Text(
                text = if (count > 9) "9+" else count.toString(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onError,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(colors.error, CircleShape)
                    .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                    .wrapContentSize(Alignment.Center)
            )
        }
    }
}
