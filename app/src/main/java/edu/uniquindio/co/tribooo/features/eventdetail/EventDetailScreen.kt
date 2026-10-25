package edu.uniquindio.co.tribooo.features.eventdetail

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import edu.uniquindio.co.tribooo.core.component.AttendanceDialogs
import edu.uniquindio.co.tribooo.core.component.ConfirmAlertDialog
import edu.uniquindio.co.tribooo.core.component.FieldSupport
import edu.uniquindio.co.tribooo.core.component.RequiredLegend
import edu.uniquindio.co.tribooo.core.component.errorSemantics
import edu.uniquindio.co.tribooo.core.component.formTarget
import edu.uniquindio.co.tribooo.core.util.FormLimits
import edu.uniquindio.co.tribooo.core.util.FormTexts
import edu.uniquindio.co.tribooo.core.component.InitialsAvatar
import edu.uniquindio.co.tribooo.core.component.MapPlaceholder
import edu.uniquindio.co.tribooo.core.component.WrapRow
import edu.uniquindio.co.tribooo.core.component.avatarColors
import edu.uniquindio.co.tribooo.core.component.iconVector
import edu.uniquindio.co.tribooo.core.theme.categoryColors
import edu.uniquindio.co.tribooo.core.util.agoLabel
import edu.uniquindio.co.tribooo.core.util.cupoLabel
import edu.uniquindio.co.tribooo.core.util.eventDaysLabel
import edu.uniquindio.co.tribooo.core.util.eventHoursLabel
import edu.uniquindio.co.tribooo.core.util.eventWhenLabel
import edu.uniquindio.co.tribooo.core.util.firstName
import edu.uniquindio.co.tribooo.core.util.formatMoney
import edu.uniquindio.co.tribooo.core.util.formatThousands
import edu.uniquindio.co.tribooo.core.util.initials
import edu.uniquindio.co.tribooo.core.util.isCupoTight
import edu.uniquindio.co.tribooo.core.util.kmLabel
import edu.uniquindio.co.tribooo.core.util.occupancy
import edu.uniquindio.co.tribooo.core.util.waitlistLabel
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.usecase.attendance.AttendanceStep
import kotlinx.coroutines.launch
import java.util.Locale

/** Margen lateral del contenido bajo la portada. */
private val ContentPadding = 20.dp

private val HeroHeight = 224.dp

@Composable
fun EventDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUser: (userId: String) -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    // Sin datos (cargando o evento inexistente) la pantalla igual ofrece volver
    val detail = state.detail ?: run {
        MissingEventContent(isLoading = state.isLoading, onBack = onNavigateBack)
        return
    }

    EventDetailContent(
        detail = detail,
        draft = state.draft,
        showAttendees = state.showAttendees,
        report = state.report,
        attendanceStep = state.attendanceStep,
        showFinalizeDialog = state.showFinalizeDialog,
        snackbarHostState = snackbarHostState,
        onBack = onNavigateBack,
        onShare = { context.shareEvent(detail.event) },
        onNavigateToUser = onNavigateToUser,
        onAction = viewModel::onAction
    )
}

/** Pantalla mínima mientras el evento carga o si ya no está disponible; la flecha de atrás siempre funciona. */
@Composable
private fun MissingEventContent(isLoading: Boolean, onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Scaffold(containerColor = colors.surface) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            IconButton(onClick = onBack, modifier = Modifier.padding(start = 4.dp, top = 4.dp)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver", tint = colors.onSurface)
            }
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("Este evento ya no está disponible", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
                    Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) { Text("Volver") }
                }
            }
        }
    }
}

/**
 * Plantilla del detalle: pinta cualquier evento a partir de [detail], sin depender del ViewModel.
 * Las acciones del usuario se reportan con [onAction].
 */
@Composable
fun EventDetailContent(
    detail: EventDetail,
    draft: CommentDraft,
    showAttendees: Boolean,
    report: ReportDraft?,
    attendanceStep: AttendanceStep?,
    showFinalizeDialog: Boolean,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onNavigateToUser: (userId: String) -> Unit,
    onAction: (EventDetailAction) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { DetailBottomBar(detail = detail, onAction = onAction) }
    ) { innerPadding ->
        val bottomPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
        val listState = rememberLazyListState()
        // Al salir la portada de pantalla, la barra de estado necesita fondo para no tapar el texto
        val heroHidden by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

        Box {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottomPadding)
                    // El teclado solo empuja lo que no cubre ya la barra inferior
                    .consumeWindowInsets(bottomPadding)
                    .imePadding(),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                item(key = "hero") {
                    HeroCover(
                        detail = detail,
                        onBack = onBack,
                        onToggleSaved = { onAction(EventDetailAction.ToggleSaved) },
                        onShare = onShare,
                        onReport = { onAction(EventDetailAction.ShowReport) }
                    )
                }

                item(key = "header") { DetailHeader(event = detail.event) }

                item(key = "info") { InfoCard(detail = detail) }

                if (detail.event.cupo != null) {
                    item(key = "cupo") { CupoCard(detail = detail) }
                }

                item(key = "organizer") {
                    OrganizerCard(
                        detail = detail,
                        onToggleFollow = { onAction(EventDetailAction.ToggleFollow) },
                        onOpenProfile = {
                            if (!detail.isMine) onNavigateToUser(detail.event.organizer.id)
                        }
                    )
                }

                item(key = "about") {
                    Column(modifier = Modifier.padding(horizontal = ContentPadding)) {
                        SectionTitle("Sobre el evento")
                        Text(
                            text = detail.event.description.ifBlank { "El organizador no agregó una descripción." },
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item(key = "meeting-point") {
                    Column(modifier = Modifier.padding(horizontal = ContentPadding)) {
                        SectionTitle("Punto de encuentro")
                        MapPlaceholder(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(128.dp)
                        )
                    }
                }

                item(key = "attendees") {
                    AttendeesSection(detail = detail, onSeeAll = { onAction(EventDetailAction.ShowAttendees) })
                }

                item(key = "comments-header") { CommentsHeader(detail = detail) }

                items(detail.comments, key = { it.id }) { comment ->
                    CommentCard(
                        comment = comment,
                        onOpenProfile = {
                            if (!comment.author.isCurrentUser) onNavigateToUser(comment.author.id)
                        },
                        modifier = Modifier.padding(horizontal = ContentPadding, vertical = 5.dp)
                    )
                }

                item(key = "comment-form") {
                    CommentForm(draft = draft, onAction = onAction)
                }

                if (detail.gallery.isNotEmpty()) {
                    item(key = "gallery") {
                        EventGallery(images = detail.gallery, eventTitle = detail.event.title)
                    }
                }
            }

            if (heroHidden) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(MaterialTheme.colorScheme.surface)
                )
            }
        }
    }

    if (report != null) {
        ReportEventSheet(event = detail.event, draft = report, onAction = onAction)
    }

    AttendanceDialogs(
        step = attendanceStep,
        onCancel = { onAction(EventDetailAction.CancelAttendance) },
        onContinue = { onAction(EventDetailAction.ContinueAttendance) }
    )

    if (showFinalizeDialog) {
        ConfirmAlertDialog(
            title = "¿Finalizar este evento?",
            message = "Dejará de aparecer en el feed y no aceptará nuevas confirmaciones. " +
                "Quedará archivado en Mis eventos, en Finalizados. Esta acción no se puede deshacer.",
            confirmLabel = "Finalizar evento",
            dismissLabel = "Cancelar",
            onConfirm = { onAction(EventDetailAction.ConfirmFinalize) },
            onDismiss = { onAction(EventDetailAction.DismissFinalize) }
        )
    }

    if (showAttendees) {
        AttendeesSheet(
            detail = detail,
            onDismiss = { onAction(EventDetailAction.HideAttendees) },
            onOpenProfile = { userId ->
                // Se cierra la hoja para que no quede abierta al volver
                onAction(EventDetailAction.HideAttendees)
                onNavigateToUser(userId)
            }
        )
    }
}

@Composable
private fun HeroCover(
    detail: EventDetail,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
    onShare: () -> Unit,
    onReport: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val cover = detail.event.imageUrl
    // La portada se extiende detrás de la barra de estado
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HeroHeight + statusBarHeight)
            .background(colors.surfaceContainerHigh)
    ) {
        if (cover == null) {
            Icon(
                imageVector = detail.event.category.iconVector(),
                contentDescription = null,
                tint = colors.outline,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp)
            )
        } else {
            AsyncImage(
                model = cover,
                contentDescription = "Portada · ${detail.event.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingCircleButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Volver",
                onClick = onBack
            )
            Spacer(Modifier.weight(1f))
            FloatingCircleButton(
                icon = if (detail.isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                contentDescription = if (detail.isSaved) "Quitar de guardados" else "Guardar evento",
                onClick = onToggleSaved,
                container = if (detail.isSaved) colors.primaryContainer else colors.surfaceContainerLowest,
                content = if (detail.isSaved) colors.onPrimaryContainer else colors.onSurface
            )
            FloatingCircleButton(
                icon = Icons.Rounded.Share,
                contentDescription = "Compartir evento",
                onClick = onShare
            )
            // El organizador no puede reportar su propio evento
            if (!detail.isMine) {
                FloatingCircleButton(
                    icon = if (detail.hasReported) Icons.Rounded.Flag else Icons.Outlined.Flag,
                    contentDescription = if (detail.hasReported) "Ya reportaste este evento" else "Reportar evento",
                    onClick = onReport,
                    container = if (detail.hasReported) colors.errorContainer else colors.surfaceContainerLowest,
                    content = if (detail.hasReported) colors.onErrorContainer else colors.onSurface
                )
            }
        }

        // Borde redondeado de la hoja de contenido que se monta sobre la portada
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(26.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(colors.surface)
        )
    }
}

/** Carrusel con las fotos que el organizador agregó al evento. */
@Composable
private fun EventGallery(images: List<String>, eventTitle: String) {
    val colors = MaterialTheme.colorScheme
    val pagerState = rememberPagerState(pageCount = { images.size })
    val scope = rememberCoroutineScope()
    // Las flechas dan la vuelta al llegar a un extremo
    val goTo = { page: Int ->
        scope.launch { pagerState.animateScrollToPage((page + images.size) % images.size) }
    }

    Column(modifier = Modifier.padding(horizontal = ContentPadding)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp, bottom = 10.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Galería del evento", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
            Text(
                "${pagerState.currentPage + 1} de ${images.size}",
                fontSize = 12.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 1.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(186.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(colors.surfaceContainer)
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(
                    model = images[page],
                    contentDescription = "Foto ${page + 1} de ${images.size} · $eventTitle",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (images.size > 1) {
                FloatingCircleButton(
                    icon = Icons.Rounded.ChevronLeft,
                    contentDescription = "Foto anterior",
                    onClick = { goTo(pagerState.currentPage - 1) },
                    size = 36.dp,
                    iconSize = 20.dp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                )
                FloatingCircleButton(
                    icon = Icons.Rounded.ChevronRight,
                    contentDescription = "Foto siguiente",
                    onClick = { goTo(pagerState.currentPage + 1) },
                    size = 36.dp,
                    iconSize = 20.dp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                )
                PagerDots(
                    pagerState = pagerState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun PagerDots(pagerState: PagerState, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(pagerState.pageCount) { page ->
            val selected = pagerState.currentPage == page
            val width by animateDpAsState(if (selected) 20.dp else 8.dp, label = "dotWidth")
            Box(
                modifier = Modifier
                    .width(width)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (selected) Color.White else Color.White.copy(alpha = 0.6f))
                    .clickable { scope.launch { pagerState.animateScrollToPage(page) } }
            )
        }
    }
}

@Composable
private fun DetailHeader(event: Event) {
    val colors = MaterialTheme.colorScheme
    val tones = categoryColors(event.category)

    Column(modifier = Modifier.padding(horizontal = ContentPadding)) {
        WrapRow {
            Pill(icon = event.category.iconVector(), text = event.category.name, container = tones.container, content = tones.content)
            if (event.verified) {
                Pill(
                    icon = Icons.Rounded.Verified,
                    text = "Verificado por moderación",
                    container = colors.secondaryContainer,
                    content = colors.onSecondaryContainer
                )
            }
        }
        Text(
            text = event.title,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = (-0.5).sp,
            color = colors.onSurface,
            modifier = Modifier.padding(top = 14.dp)
        )
    }
}

@Composable
private fun InfoCard(detail: EventDetail) {
    val event = detail.event
    val contribution = event.contribution
    val rows = listOf(
        InfoRowData(Icons.Rounded.Event, eventDaysLabel(event.startDate, event.endDate), "Inicio y fin del evento"),
        InfoRowData(Icons.Rounded.Schedule, eventHoursLabel(event.startDate, event.endDate), "Hora local (Armenia, UTC-5)"),
        InfoRowData(Icons.Rounded.Place, event.place, "${event.neighborhood} · a ${detail.distanceKm.kmLabel()} de ti"),
        InfoRowData(
            Icons.Rounded.Category,
            event.category.name,
            if (event.verified) "Categoría verificada por moderación" else "Categoría del evento"
        ),
        if (contribution != null) {
            InfoRowData(
                Icons.Rounded.VolunteerActivism,
                "${contribution.amount.formatMoney()} · aporte benéfico",
                contribution.cause ?: "Aporte voluntario para la causa del evento"
            )
        } else {
            InfoRowData(Icons.Rounded.Payments, "Entrada libre", "Sin costo ni aporte de inscripción")
        }
    )

    DetailCard(modifier = Modifier.padding(start = ContentPadding, end = ContentPadding, top = 18.dp)) {
        rows.forEachIndexed { index, row ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
            InfoRow(row)
        }
    }
}

private data class InfoRowData(val icon: ImageVector, val value: String, val label: String)

@Composable
private fun InfoRow(row: InfoRowData) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(row.icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
        Column {
            Text(row.value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
            Text(
                row.label,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun CupoCard(detail: EventDetail) {
    val colors = MaterialTheme.colorScheme
    val event = detail.event
    val tight = event.isCupoTight(detail.attendeesCount)
    val container = if (tight) colors.errorContainer else colors.primaryContainer
    val content = if (tight) colors.onErrorContainer else colors.onPrimaryContainer

    // La barra crece desde cero al abrir la pantalla
    var started by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (started) event.occupancy(detail.attendeesCount) else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "cupoProgress"
    )
    LaunchedEffect(Unit) { started = true }

    DetailCard(
        color = container,
        modifier = Modifier.padding(start = ContentPadding, end = ContentPadding, top = 12.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(event.cupoLabel(detail.attendeesCount), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = content)
                Text("cupo ${event.cupo}", fontSize = 12.sp, color = content)
            }
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.Black.copy(alpha = 0.12f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(content)
                )
            }
        }
    }
}

@Composable
private fun OrganizerCard(detail: EventDetail, onToggleFollow: () -> Unit, onOpenProfile: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val organizer = detail.event.organizer
    val eventsLabel = if (detail.organizerEventsCount == 1) "1 evento" else "${detail.organizerEventsCount} eventos"

    DetailCard(modifier = Modifier.padding(start = ContentPadding, end = ContentPadding, top = 12.dp)) {
        Row(
            modifier = Modifier
                .then(if (detail.isMine) Modifier else Modifier.clickable(onClick = onOpenProfile))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InitialsAvatar(
                initials = organizer.name.initials(),
                container = colors.tertiaryContainer,
                content = colors.onTertiaryContainer,
                size = 46.dp,
                shape = RoundedCornerShape(16.dp),
                fontSize = 15.sp,
                photo = detail.organizerPhoto
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(organizer.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
                Text(
                    text = if (detail.isMine) "Tú organizas · $eventsLabel" else "Organizador · $eventsLabel",
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (!detail.isMine) {
                val following = detail.isFollowingOrganizer
                Surface(
                    onClick = onToggleFollow,
                    shape = RoundedCornerShape(12.dp),
                    color = if (following) colors.secondaryContainer else Color.Transparent,
                    contentColor = if (following) colors.onSecondaryContainer else colors.primary,
                    border = if (following) null else BorderStroke(1.dp, colors.outline),
                    modifier = Modifier.height(36.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                        Text(if (following) "Siguiendo" else "Seguir", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendeesSection(detail: EventDetail, onSeeAll: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = ContentPadding, end = ContentPadding - 8.dp, top = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Asistentes · ${detail.attendeesCount.formatThousands()}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (detail.attendees.isNotEmpty()) {
                TextButton(onClick = onSeeAll, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("Ver todos", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        if (detail.attendees.isEmpty()) {
            Text(
                "Aún nadie confirma. ¡Sé el primero!",
                fontSize = 14.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = ContentPadding, vertical = 10.dp)
            )
        } else {
            LazyRow(
                modifier = Modifier.padding(top = 4.dp),
                contentPadding = PaddingValues(horizontal = ContentPadding),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(detail.attendeesPreview, key = { _, person -> person.id }) { index, person ->
                    val (bg, fg) = avatarColors(index)
                    Column(modifier = Modifier.width(66.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        InitialsAvatar(
                            initials = person.initials,
                            container = bg,
                            content = fg,
                            size = 48.dp,
                            shape = RoundedCornerShape(18.dp),
                            fontSize = 15.sp,
                            photo = person.photo
                        )
                        Text(
                            person.name.firstName(),
                            fontSize = 11.sp,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

/** Hoja con la lista completa de asistentes confirmados. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttendeesSheet(detail: EventDetail, onDismiss: () -> Unit, onOpenProfile: (userId: String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    // Cierra con la animación de la hoja antes de quitarla de la composición
    val close: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }
    val summary = if (detail.attendeesCount == 1) {
        "1 persona confirmó su asistencia"
    } else {
        "${detail.attendeesCount.formatThousands()} personas confirmaron su asistencia"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceContainerLowest
    ) {
        Column(modifier = Modifier.padding(horizontal = ContentPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Rounded.Groups, contentDescription = null, tint = colors.primary, modifier = Modifier.size(24.dp))
                Text(
                    "Asistentes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
                FloatingCircleButton(
                    icon = Icons.Rounded.Close,
                    contentDescription = "Cerrar",
                    onClick = close,
                    size = 40.dp,
                    iconSize = 21.dp,
                    container = colors.surfaceContainerLow,
                    elevation = 0.dp
                )
            }
            Text(
                summary,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.padding(top = 14.dp),
            contentPadding = PaddingValues(start = ContentPadding, end = ContentPadding, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(detail.attendees, key = { _, person -> person.id }) { index, person ->
                AttendeeRow(person = person, seed = index, onClick = { onOpenProfile(person.id) })
            }
        }
    }
}

@Composable
private fun AttendeeRow(person: PersonItem, seed: Int, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val (bg, fg) = avatarColors(seed)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceContainerLow)
            .then(if (person.isCurrentUser) Modifier else Modifier.clickable(onClick = onClick))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InitialsAvatar(
            initials = person.initials,
            container = bg,
            content = fg,
            size = 44.dp,
            shape = RoundedCornerShape(16.dp),
            fontSize = 14.sp,
            photo = person.photo
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(person.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
            Text(
                text = if (person.isCurrentUser) "Confirmaste tu asistencia" else "Vecino de ${person.neighborhood}",
                fontSize = 12.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        if (person.isCurrentUser) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun CommentsHeader(detail: EventDetail) {
    val average = detail.averageRating
    val ratingsLabel = if (detail.ratingsCount == 1) "1 reseña" else "${detail.ratingsCount} reseñas"

    Row(
        modifier = Modifier.padding(start = ContentPadding, end = ContentPadding, top = 24.dp, bottom = 5.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Comentarios · ${detail.comments.size}", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text(
            text = if (average != null) "${String.format(Locale.US, "%.1f", average)} ★ de $ratingsLabel" else "Sin reseñas aún",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 1.dp)
        )
    }
}

@Composable
private fun CommentCard(comment: CommentItem, onOpenProfile: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val (bg, fg) = avatarColors(comment.author.id.hashCode())

    DetailCard(shape = RoundedCornerShape(20.dp), modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InitialsAvatar(
                initials = comment.author.initials,
                container = bg,
                content = fg,
                modifier = if (comment.author.isCurrentUser) Modifier else Modifier.clickable(onClick = onOpenProfile),
                photo = comment.author.photo
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = if (comment.author.isCurrentUser) Modifier else Modifier.clickable(onClick = onOpenProfile),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(comment.author.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
                    Text(comment.date.agoLabel(), fontSize = 11.sp, color = colors.onSurfaceVariant)
                    comment.rating?.let { rating ->
                        Pill(
                            icon = Icons.Rounded.Star,
                            text = "$rating",
                            container = colors.primaryContainer,
                            content = colors.onPrimaryContainer,
                            height = 22.dp,
                            cornerRadius = 8.dp,
                            iconSize = 13.dp,
                            fontSize = 11.sp
                        )
                    }
                }
                Text(
                    comment.text,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun CommentForm(draft: CommentDraft, onAction: (EventDetailAction) -> Unit) {
    val colors = MaterialTheme.colorScheme

    // Cuando el teclado termina de abrir, el campo completo (con el botón) queda visible
    val inputRequester = remember { BringIntoViewRequester() }
    val imeVisible = WindowInsets.isImeVisible
    var inputFocused by remember { mutableStateOf(false) }
    LaunchedEffect(inputFocused, imeVisible) {
        if (inputFocused && imeVisible) inputRequester.bringIntoView()
    }

    val focusManager = LocalFocusManager.current
    val send = {
        onAction(EventDetailAction.SendComment)
        // Con el comentario vacío el foco debe quedarse (o volver) en el campo
        if (draft.text.isNotBlank()) focusManager.clearFocus()
    }

    Column(modifier = Modifier.padding(start = ContentPadding, end = ContentPadding, top = 7.dp)) {
        RequiredLegend(modifier = Modifier.padding(bottom = 6.dp))

        // Calificación opcional
        DetailCard(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Calificación (opcional)",
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (draft.rating > 0) "${draft.rating} / 5" else "Sin calificar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (draft.rating > 0) colors.primary else colors.onSurfaceVariant
                    )
                    if (draft.rating > 0) {
                        Surface(
                            onClick = { onAction(EventDetailAction.RatingChange(draft.rating)) },
                            shape = RoundedCornerShape(10.dp),
                            color = colors.surfaceContainer,
                            contentColor = colors.onSurfaceVariant,
                            modifier = Modifier.height(28.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 7.dp, end = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text("Quitar", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
                Row {
                    (1..5).forEach { star ->
                        val filled = star <= draft.rating
                        IconButton(onClick = { onAction(EventDetailAction.RatingChange(star)) }) {
                            Icon(
                                imageVector = if (filled) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                contentDescription = "$star de 5",
                                tint = if (filled) colors.primary else colors.outlineVariant,
                                modifier = Modifier.size(27.dp)
                            )
                        }
                    }
                }
                Text("Toca de nuevo la misma estrella para quitarla.", fontSize = 11.sp, color = colors.onSurfaceVariant)
            }
        }

        // Campo del comentario
        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .bringIntoViewRequester(inputRequester)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(colors.surfaceContainer)
                .then(if (draft.error != null) Modifier.border(1.dp, colors.error, RoundedCornerShape(24.dp)) else Modifier)
                .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BasicTextField(
                value = draft.text,
                onValueChange = { onAction(EventDetailAction.CommentChange(it)) },
                maxLines = 4,
                textStyle = TextStyle(fontSize = 14.sp, color = colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { send() }),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp)
                    .errorSemantics(draft.error)
                    .formTarget(draft.focusRequested, { onAction(EventDetailAction.CommentFocusHandled) })
                    .onFocusChanged { inputFocused = it.isFocused },
                decorationBox = { inner ->
                    if (draft.text.isEmpty()) {
                        Text("Escribe un comentario *", fontSize = 14.sp, color = colors.onSurfaceVariant)
                    }
                    inner()
                }
            )
            Surface(
                onClick = send,
                enabled = !draft.isSending,
                shape = CircleShape,
                color = colors.primary,
                contentColor = colors.onPrimary,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (draft.isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(19.dp), strokeWidth = 2.dp, color = colors.onPrimary)
                    } else {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Publicar comentario", modifier = Modifier.size(19.dp))
                    }
                }
            }
        }
        FieldSupport(
            error = draft.error,
            hint = FormTexts.COMMENT_HINT,
            counter = draft.text.length to FormLimits.COMMENT,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp)
        )
    }
}

@Composable
private fun DetailBottomBar(detail: EventDetail, onAction: (EventDetailAction) -> Unit) {
    val colors = MaterialTheme.colorScheme

    Surface(color = colors.surfaceContainerLow, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val interested = detail.isInterested
            Surface(
                onClick = { onAction(EventDetailAction.ToggleInterest) },
                shape = RoundedCornerShape(18.dp),
                color = if (interested) colors.tertiaryContainer else Color.Transparent,
                contentColor = if (interested) colors.onTertiaryContainer else colors.onSurface,
                border = if (interested) null else BorderStroke(1.dp, colors.outline),
                modifier = Modifier.height(56.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Icon(
                        imageVector = if (interested) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(detail.interestCount.formatThousands(), fontSize = 15.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium)
                        Text("Me interesa", fontSize = 10.sp, lineHeight = 12.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }

            if (detail.mainAction == MainAction.FINALIZE) {
                MainActionButton(
                    label = "Finalizar evento",
                    container = colors.secondaryContainer,
                    content = colors.onSecondaryContainer,
                    enabled = true,
                    onClick = { onAction(EventDetailAction.FinalizeEvent) },
                    modifier = Modifier.weight(1f)
                )
                return@Row
            }
            if (detail.mainAction == MainAction.FINALIZED) {
                MainActionButton(
                    label = "Evento finalizado",
                    container = colors.surfaceContainerHigh,
                    content = colors.onSurfaceVariant,
                    enabled = false,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                return@Row
            }

            val waitlist = detail.isWaitlistMode
            val position = detail.waitlistPosition.takeIf { waitlist }
            Surface(
                onClick = { onAction(EventDetailAction.ToggleAttendance) },
                enabled = detail.canToggleAttendance,
                shape = RoundedCornerShape(18.dp),
                color = when {
                    detail.isAttending -> colors.errorContainer
                    position != null -> colors.secondaryContainer
                    waitlist -> colors.surfaceContainerHigh
                    else -> colors.primary
                },
                contentColor = when {
                    detail.isAttending -> colors.onErrorContainer
                    position != null -> colors.onSecondaryContainer
                    waitlist -> colors.onSurfaceVariant
                    else -> colors.onPrimary
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (detail.isAttending) Icons.Rounded.EventBusy else Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = when {
                            detail.isAttending -> "Cancelar asistencia"
                            position != null -> waitlistLabel(position)
                            waitlist -> "Lista de espera"
                            else -> "Confirmar"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/** Botón principal de la barra inferior para los estados del evento (finalizar / ya finalizado). */
@Composable
private fun MainActionButton(
    label: String,
    container: Color,
    content: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        color = container,
        contentColor = content,
        modifier = modifier.height(56.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Rounded.TaskAlt, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(text = label, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(top = 24.dp, bottom = 8.dp)
    )
}

/** Tarjeta blanca redondeada que agrupa la información del detalle. */
@Composable
private fun DetailCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    content: @Composable () -> Unit
) {
    Surface(shape = shape, color = color, modifier = modifier.fillMaxWidth()) {
        Column { content() }
    }
}

/** Insignia con ícono y texto (categoría, verificación, calificación). */
@Composable
private fun Pill(
    icon: ImageVector,
    text: String,
    container: Color,
    content: Color,
    height: Dp = 30.dp,
    cornerRadius: Dp = 10.dp,
    iconSize: Dp = 16.dp,
    fontSize: TextUnit = 12.sp
) {
    Row(
        modifier = Modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(container)
            .padding(start = 7.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(iconSize))
        Text(text, fontSize = fontSize, fontWeight = FontWeight.Medium, color = content)
    }
}

/** Botón circular flotante sobre la portada y la galería. */
@Composable
private fun FloatingCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    container: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    content: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Dp = 3.dp
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = container,
        contentColor = content,
        shadowElevation = elevation,
        modifier = modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(iconSize))
        }
    }
}

/** Comparte el evento como texto con las apps instaladas. */
private fun Context.shareEvent(event: Event) {
    val text = buildString {
        appendLine(event.title)
        appendLine(eventWhenLabel(event.startDate, event.endDate))
        appendLine("${event.place} · ${event.neighborhood}")
        append("Nos vemos en Triboo.")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, "Compartir evento"))
}
