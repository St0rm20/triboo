package edu.uniquindio.co.tribooo.features.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import edu.uniquindio.co.tribooo.core.component.CategoryChip
import edu.uniquindio.co.tribooo.core.component.MainBottomBar
import edu.uniquindio.co.tribooo.core.component.MainTab
import edu.uniquindio.co.tribooo.core.component.iconVector
import edu.uniquindio.co.tribooo.core.theme.categoryColors
import edu.uniquindio.co.tribooo.core.util.APP_ZONE
import edu.uniquindio.co.tribooo.core.util.atAppZone
import edu.uniquindio.co.tribooo.core.util.eventWhenLabel
import edu.uniquindio.co.tribooo.core.util.formatThousands
import edu.uniquindio.co.tribooo.core.util.longLabel
import edu.uniquindio.co.tribooo.core.util.monthShort
import edu.uniquindio.co.tribooo.domain.model.Category
import java.time.LocalDate

/** Porcentaje de ocupación a partir del cual el cupo se resalta como "últimos cupos". */
private const val CUPO_WARNING = 0.85f

@Composable
fun FeedScreen(
    onNavigateToCreateEvent: () -> Unit,
    snackbarMessage: String?,
    onSnackbarShown: () -> Unit,
    viewModel: FeedViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            MainBottomBar(
                selected = MainTab.FEED,
                onCreateClick = onNavigateToCreateEvent,
                enabledTabs = setOf(MainTab.FEED),
                onTabSelected = {}
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
                onToggleCategory = viewModel::toggleCategory
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
                    EventCard(item = item, onToggleAttendance = { viewModel.toggleAttendance(item) })
                }

                if (state.events.isEmpty()) {
                    item { EmptyFeed(onClearFilters = viewModel::clearFilters) }
                }
            }
        }
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
    onToggleCategory: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 8.dp)) {
        Column(modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 2.dp, bottom = 10.dp)) {
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

@Composable
private fun EventCard(item: FeedEventItem, onToggleAttendance: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val event = item.event
    val start = event.startDate.atAppZone().toLocalDate()

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = colors.surfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Portada
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
            ) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.surfaceContainerHigh)
                )

                val tones = categoryColors(event.category)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(tones.container)
                        .padding(start = 9.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(event.category.iconVector(), null, tint = tones.content, modifier = Modifier.size(16.dp))
                    Text(event.category.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = tones.content)
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceContainerLowest)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .widthIn(min = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        start.monthShort().uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.6.sp,
                        color = colors.onSurfaceVariant
                    )
                    Text(
                        start.dayOfMonth.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.onSurface
                    )
                }

                if (item.isMine) {
                    Text(
                        text = "Tú organizas",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.onTertiaryContainer,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.tertiaryContainer)
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp)) {
                Text(
                    text = event.title,
                    fontSize = 18.sp,
                    lineHeight = 23.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface
                )
                Column(
                    modifier = Modifier.padding(top = 9.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    InfoRow(Icons.Rounded.Schedule, eventWhenLabel(event.startDate, event.endDate))
                    InfoRow(Icons.Rounded.Place, "${event.place} · ${event.neighborhood}")
                }

                event.cupo?.let { cupo -> CupoBar(item = item, cupo = cupo) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        val avatarColors = listOf(
                            colors.primaryContainer to colors.onPrimaryContainer,
                            colors.secondaryContainer to colors.onSecondaryContainer,
                            colors.tertiaryContainer to colors.onTertiaryContainer
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy((-7).dp)) {
                            item.attendeeInitials.forEachIndexed { index, initials ->
                                val (bg, fg) = avatarColors[index % avatarColors.size]
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .border(2.dp, colors.surfaceContainerLowest, CircleShape)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(bg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(initials, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = fg)
                                }
                            }
                        }
                        Text(
                            text = "${item.attendeesCount.formatThousands()} asistirán",
                            fontSize = 13.sp,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    AttendButton(item = item, onClick = onToggleAttendance)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(17.dp))
        Text(text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CupoBar(item: FeedEventItem, cupo: Int) {
    val colors = MaterialTheme.colorScheme
    val tight = item.occupancy >= CUPO_WARNING
    val label = when {
        item.isFull -> "Cupo completo"
        tight -> "Últimos cupos · ${cupo - item.attendeesCount} libres"
        else -> "${item.attendeesCount} de $cupo confirmados"
    }
    val accent = if (tight) colors.error else colors.primary

    Column(modifier = Modifier.padding(top = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val fg = if (tight) colors.error else colors.onSurfaceVariant
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = fg)
            Text("${(item.occupancy * 100).toInt()}% ocupado", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = fg)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.surfaceContainerHigh)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(item.occupancy)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(accent)
            )
        }
    }
}

@Composable
private fun AttendButton(item: FeedEventItem, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val waitlist = item.isFull && !item.isAttending
    Surface(
        onClick = onClick,
        enabled = !waitlist,
        shape = RoundedCornerShape(14.dp),
        color = when {
            item.isAttending -> colors.errorContainer
            waitlist -> colors.surfaceContainerHigh
            else -> colors.primary
        },
        contentColor = when {
            item.isAttending -> colors.onErrorContainer
            waitlist -> colors.onSurfaceVariant
            else -> colors.onPrimary
        },
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (item.isAttending) Icons.Rounded.EventBusy else Icons.Rounded.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = when {
                    item.isAttending -> "Cancelar"
                    waitlist -> "Lista de espera"
                    else -> "Asistiré"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
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
