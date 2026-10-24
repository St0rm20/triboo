package edu.uniquindio.co.tribooo.features.saved

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import edu.uniquindio.co.tribooo.core.component.iconVector
import edu.uniquindio.co.tribooo.core.theme.categoryColors
import edu.uniquindio.co.tribooo.core.util.eventWhenLabel
import edu.uniquindio.co.tribooo.domain.model.Event
import kotlinx.coroutines.withTimeoutOrNull

/** Tiempo que el snackbar ofrece "Deshacer". */
private const val UNDO_MILLIS = 6_000L

@Composable
fun SavedEventsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEventDetail: (eventId: String) -> Unit,
    onNavigateToFeed: () -> Unit,
    viewModel: SavedEventsViewModel = hiltViewModel()
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
            SavedTopBar(summary = state.summary, onBack = onNavigateBack)

            if (state.events.isEmpty()) {
                EmptySaved(onExplore = onNavigateToFeed)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.events, key = { it.id }) { event ->
                        SavedEventCard(
                            event = event,
                            onClick = { onNavigateToEventDetail(event.id) },
                            onRemove = { viewModel.onRemove(event) }
                        )
                    }
                }
            }
        }
    }

    // El snackbar con "Deshacer" vive en esta pantalla; cualquier otro mensaje se descarta solo
    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        val undoEvent = message.undoEvent
        val result = withTimeoutOrNull(UNDO_MILLIS) {
            snackbarHostState.showSnackbar(
                message = message.text,
                actionLabel = if (undoEvent != null) "Deshacer" else null,
                duration = SnackbarDuration.Indefinite
            )
        }
        if (result == SnackbarResult.ActionPerformed && undoEvent != null) {
            viewModel.onUndo(undoEvent)
        } else {
            viewModel.onMessageShown()
        }
    }
}

@Composable
private fun SavedTopBar(summary: String, onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainer)
            .statusBarsPadding()
            .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver", tint = colors.onSurface)
            }
            Text("Eventos guardados", fontSize = 20.sp, color = colors.onSurface)
        }
        Text(
            text = summary,
            fontSize = 13.sp,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(start = 48.dp, top = 2.dp)
        )
    }
}

@Composable
private fun SavedEventCard(event: Event, onClick: () -> Unit, onRemove: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = colors.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Thumbnail(event)

            Column(modifier = Modifier.weight(1f)) {
                CategoryBadge(event)
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    text = eventWhenLabel(event.startDate, event.endDate),
                    fontSize = 13.sp,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "${event.place} · ${event.neighborhood}",
                    fontSize = 13.sp,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Marcador relleno: quita el evento de guardados sin abrir el detalle
            Surface(
                onClick = onRemove,
                shape = CircleShape,
                color = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Bookmark, contentDescription = "Quitar de guardados", modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

/** Miniatura de 62 dp: rayas diagonales suaves y, si el evento trae portada, la imagen encima. */
@Composable
private fun Thumbnail(event: Event) {
    val colors = MaterialTheme.colorScheme
    val stripe = colors.outlineVariant

    Box(
        modifier = Modifier
            .size(62.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainerHigh)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 10.dp.toPx()
            var x = -size.height
            while (x < size.width) {
                drawLine(
                    color = stripe,
                    start = Offset(x, size.height),
                    end = Offset(x + size.height, 0f),
                    strokeWidth = 2.dp.toPx()
                )
                x += step
            }
        }
        if (event.imageUrl != null) {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/** Categoría del evento (ícono y nombre) con los colores de la categoría. */
@Composable
private fun CategoryBadge(event: Event) {
    val category = event.category
    val tones = categoryColors(category)
    Row(
        modifier = Modifier
            .background(tones.container, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(category.iconVector(), contentDescription = null, tint = tones.content, modifier = Modifier.size(14.dp))
        Text(category.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = tones.content)
    }
}

@Composable
private fun EmptySaved(onExplore: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.BookmarkBorder, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(48.dp))
        Text(
            text = "Sin eventos guardados",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "Toca el marcador en el detalle de un evento para guardarlo y volver después.",
            fontSize = 14.sp,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
        )
        Button(onClick = onExplore) { Text("Explorar eventos") }
    }
}
