package edu.uniquindio.co.tribooo.features.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import edu.uniquindio.co.tribooo.core.component.EventThumbnail
import edu.uniquindio.co.tribooo.core.component.MainBottomBar
import edu.uniquindio.co.tribooo.core.component.MainTab
import edu.uniquindio.co.tribooo.core.util.WEEKDAY_LETTERS
import edu.uniquindio.co.tribooo.core.util.eventHoursLabel
import java.time.LocalDate

@Composable
fun AgendaScreen(
    onNavigateToFeed: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCreateEvent: () -> Unit,
    onNavigateToEventDetail: (eventId: String) -> Unit,
    viewModel: AgendaViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.surface,
        bottomBar = {
            MainBottomBar(
                selected = MainTab.AGENDA,
                onCreateClick = onNavigateToCreateEvent,
                enabledTabs = setOf(MainTab.FEED, MainTab.AGENDA, MainTab.PROFILE),
                onTabSelected = { tab ->
                    when (tab) {
                        MainTab.FEED -> onNavigateToFeed()
                        MainTab.PROFILE -> onNavigateToProfile()
                        else -> Unit
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                    Text("Mi agenda", fontSize = 24.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
                    Text(state.summary, fontSize = 13.sp, color = colors.onSurfaceVariant)
                }
            }
            item {
                CalendarCard(
                    state = state,
                    onPreviousMonth = viewModel::onPreviousMonth,
                    onNextMonth = viewModel::onNextMonth,
                    onDaySelected = viewModel::onDaySelected
                )
            }
            item {
                Text(
                    text = state.selectedDayLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
            if (state.events.isEmpty()) {
                item { EmptyDay() }
            } else {
                items(state.events, key = { it.event.id }) { item ->
                    AgendaEventCard(item = item, onClick = { onNavigateToEventDetail(item.event.id) })
                }
            }
        }
    }
}

@Composable
private fun CalendarCard(
    state: AgendaUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDaySelected: (LocalDate) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = colors.surfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Rounded.ChevronLeft, contentDescription = "Mes anterior", tint = colors.onSurface)
                }
                Text(
                    text = state.monthLabel,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Rounded.ChevronRight, contentDescription = "Mes siguiente", tint = colors.onSurface)
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                WEEKDAY_LETTERS.forEach { letter ->
                    Text(
                        text = letter,
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f).padding(vertical = 6.dp)
                    )
                }
            }

            // Filas de siete celdas; la última se completa con huecos para que las columnas no se estiren
            state.cells.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    (week + List(7 - week.size) { null }).forEach { cell ->
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                            if (cell != null) DayCell(cell, onClick = { onDaySelected(cell.date) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(cell: AgendaCell, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val textColor = when {
        cell.isSelected -> colors.onPrimary
        cell.isToday -> colors.primary
        else -> colors.onSurface
    }

    Column(
        modifier = Modifier
            .size(40.dp)
            .background(if (cell.isSelected) colors.primary else colors.surfaceContainerLowest, CircleShape)
            .then(if (cell.isToday && !cell.isSelected) Modifier.border(1.dp, colors.primary, CircleShape) else Modifier)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = cell.date.dayOfMonth.toString(),
            fontSize = 14.sp,
            fontWeight = if (cell.isSelected || cell.isToday) FontWeight.Medium else FontWeight.Normal,
            color = textColor
        )
        // Punto fuerte si el usuario va a algún evento del día; suave si no
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(5.dp)
                .background(
                    color = when {
                        !cell.hasEvents -> Color.Transparent
                        cell.hasConfirmed -> if (cell.isSelected) colors.onPrimary else colors.primary
                        else -> colors.outline
                    },
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun AgendaEventCard(item: AgendaEventItem, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val event = item.event

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = colors.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EventThumbnail(imageUrl = event.imageUrl)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = eventHoursLabel(event.startDate, event.endDate),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.primary
                )
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
                    text = "${event.place} · ${event.neighborhood}",
                    fontSize = 13.sp,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.confirmed) {
                    Row(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .background(colors.primaryContainer, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = colors.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Text("Vas", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.onPrimaryContainer)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyDay() {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Nada agendado este día.", fontSize = 15.sp, color = colors.onSurface)
        Text(
            "Los días con punto tienen eventos cerca.",
            fontSize = 13.sp,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
