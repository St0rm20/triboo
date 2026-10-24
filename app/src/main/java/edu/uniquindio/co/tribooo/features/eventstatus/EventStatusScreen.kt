package edu.uniquindio.co.tribooo.features.eventstatus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.Reviews
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.uniquindio.co.tribooo.core.component.ConfirmAlertDialog
import edu.uniquindio.co.tribooo.core.component.EventThumbnail
import edu.uniquindio.co.tribooo.core.component.FormTextField
import edu.uniquindio.co.tribooo.core.component.InitialsAvatar
import edu.uniquindio.co.tribooo.core.component.RequiredLegend
import edu.uniquindio.co.tribooo.core.component.avatarColors
import edu.uniquindio.co.tribooo.core.component.formTarget
import edu.uniquindio.co.tribooo.core.component.iconVector
import edu.uniquindio.co.tribooo.core.util.FormLimits
import edu.uniquindio.co.tribooo.core.util.FormTexts
import edu.uniquindio.co.tribooo.core.theme.categoryColors
import edu.uniquindio.co.tribooo.core.util.eventWhenLabel
import edu.uniquindio.co.tribooo.core.util.finishedFooter
import edu.uniquindio.co.tribooo.core.util.formatThousands
import edu.uniquindio.co.tribooo.core.util.pendingFooter
import edu.uniquindio.co.tribooo.core.util.rejectedFooter
import edu.uniquindio.co.tribooo.core.util.removeDialogTexts
import edu.uniquindio.co.tribooo.core.util.statusBannerText
import edu.uniquindio.co.tribooo.core.util.statusBannerTitle
import edu.uniquindio.co.tribooo.core.util.statusContextLabel
import edu.uniquindio.co.tribooo.core.util.statusParagraph
import edu.uniquindio.co.tribooo.core.util.statusPillLabel
import edu.uniquindio.co.tribooo.core.util.statusPolicyLine
import edu.uniquindio.co.tribooo.core.util.statusScreenTitle
import edu.uniquindio.co.tribooo.core.util.statusSectionTitle
import edu.uniquindio.co.tribooo.core.util.statusSteps
import edu.uniquindio.co.tribooo.core.util.statusStepsTitle
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus

@Composable
fun EventStatusScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUser: (userId: String) -> Unit,
    onEventRemoved: (message: String) -> Unit,
    onResubmitEvent: (eventId: String) -> Unit,
    onEditEvent: (eventId: String) -> Unit,
    onDuplicateEvent: (eventId: String) -> Unit,
    snackbarMessage: String?,
    onSnackbarShown: () -> Unit,
    viewModel: EventStatusViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val colors = MaterialTheme.colorScheme
    val event = state.event

    // Tras eliminar o retirar el evento se vuelve a Mis eventos con el aviso
    LaunchedEffect(state.exitMessage) {
        state.exitMessage?.let { onEventRemoved(it) }
    }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    // Mensaje que llega de otra pantalla (aviso provisional de reenviar, editar y duplicar)
    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            snackbarHostState.showSnackbar(snackbarMessage)
            onSnackbarShown()
        }
    }

    // Mientras llegan los datos, o ya eliminado el evento, no se pinta nada
    if (state.isLoading || state.exitMessage != null) return

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.surface,
        bottomBar = {
            if (event != null) {
                ActionBar(
                    status = event.status,
                    onRemove = viewModel::onRemoveClick,
                    onResubmit = { onResubmitEvent(event.id) },
                    onEdit = { onEditEvent(event.id) },
                    onDuplicate = { onDuplicateEvent(event.id) },
                    onDownload = viewModel::onDownloadAttendance
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            StatusTopBar(
                title = event?.let { statusScreenTitle(it.status) } ?: "Evento",
                onBack = onNavigateBack
            )

            if (event == null) {
                EventUnavailable(onBack = onNavigateBack)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "banner") { StatusBanner(event = event, state = state) }
                    item(key = "summary") { EventSummaryCard(event) }
                    if (event.status == EventStatus.FINALIZADO) {
                        item(key = "stats") { StatsRow(state) }
                    }
                    item(key = "section") { MainSection(event = event, state = state) }
                    if (event.status == EventStatus.FINALIZADO && state.reviews.isNotEmpty()) {
                        item(key = "reviews-title") {
                            Text(
                                text = "Reseñas de asistentes · ${state.reviews.size}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.onSurface,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        items(state.reviews, key = { it.comment.id }) { review ->
                            ReviewCard(
                                review = review,
                                isEditing = state.editingReviewId == review.comment.id,
                                draft = state.replyDraft,
                                error = state.replyError,
                                isPublishing = state.isPublishingReply,
                                focusRequested = state.replyFocusRequested,
                                onOpenProfile = { onNavigateToUser(review.authorId) },
                                onReplyClick = { viewModel.onReplyClick(review) },
                                onDraftChange = viewModel::onReplyDraftChange,
                                onFocusLost = viewModel::onReplyFocusLost,
                                onFocusHandled = viewModel::onReplyFocusHandled,
                                onCancel = viewModel::onReplyCancel,
                                onPublish = { viewModel.onReplyPublish(review) },
                                onDelete = { viewModel.onReplyDelete(review) }
                            )
                        }
                    }
                    item(key = "steps") { StepsSection(status = event.status, reviewsCount = state.reviews.size) }
                }
            }
        }
    }

    if (state.showRemoveDialog && event != null) {
        val texts = removeDialogTexts(event.status)
        ConfirmAlertDialog(
            title = texts.title,
            message = texts.message,
            confirmLabel = texts.confirmLabel,
            dismissLabel = "Cancelar",
            onConfirm = viewModel::onConfirmRemove,
            onDismiss = viewModel::onDismissRemoveDialog
        )
    }
}

@Composable
private fun StatusTopBar(title: String, onBack: () -> Unit) {
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
        Text(title, fontSize = 20.sp, color = colors.onSurface)
    }
}

/** Banner tonal con el significado del estado y su contexto temporal. */
@Composable
private fun StatusBanner(event: Event, state: EventStatusUiState) {
    val colors = MaterialTheme.colorScheme
    val status = event.status

    val container: Color
    val content: Color
    val pillContainer: Color
    val pillContent: Color
    val icon: ImageVector
    val pillIcon: ImageVector
    when (status) {
        EventStatus.RECHAZADO -> {
            container = colors.errorContainer; content = colors.onErrorContainer
            pillContainer = colors.error; pillContent = colors.onError
            icon = Icons.Rounded.Block; pillIcon = Icons.Rounded.VisibilityOff
        }
        EventStatus.PENDIENTE -> {
            container = colors.tertiaryContainer; content = colors.onTertiaryContainer
            pillContainer = colors.tertiary; pillContent = colors.onTertiary
            icon = Icons.Rounded.HourglassTop; pillIcon = Icons.Rounded.Schedule
        }
        else -> {
            container = colors.secondaryContainer; content = colors.onSecondaryContainer
            pillContainer = colors.surfaceVariant; pillContent = colors.onSurfaceVariant
            icon = Icons.Rounded.TaskAlt; pillIcon = Icons.Rounded.Inventory2
        }
    }

    Surface(shape = RoundedCornerShape(24.dp), color = container, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(36.dp))
                Column {
                    Text(statusBannerTitle(status), fontSize = 17.sp, fontWeight = FontWeight.Medium, color = content)
                    Text(
                        text = statusBannerText(status),
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = content,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.background(pillContainer, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(pillIcon, contentDescription = null, tint = pillContent, modifier = Modifier.size(15.dp))
                    Text(statusPillLabel(status), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = pillContent)
                }
                Text(
                    text = statusContextLabel(status, event.publicationDate, event.endDate, state.rejection?.date),
                    fontSize = 12.sp,
                    color = content,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun EventSummaryCard(event: Event) {
    val colors = MaterialTheme.colorScheme
    val tones = categoryColors(event.category)

    Surface(shape = RoundedCornerShape(24.dp), color = colors.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EventThumbnail(imageUrl = event.imageUrl)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.background(tones.container, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(event.category.iconVector(), contentDescription = null, tint = tones.content, modifier = Modifier.size(14.dp))
                    Text(event.category.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = tones.content)
                }
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
        }
    }
}

@Composable
private fun StatsRow(state: EventStatusUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        StatTile(state.attendees.formatThousands(), "asistentes", Modifier.weight(1f))
        StatTile(state.ratingLabel, "calificación", Modifier.weight(1f))
        StatTile(state.reviews.size.toString(), "reseñas", Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .background(colors.surfaceContainer, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
        Text(label, fontSize = 11.sp, color = colors.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
    }
}

/** Sección principal del estado: motivo del rechazo, estado de la revisión o cierre del evento. */
@Composable
private fun MainSection(event: Event, state: EventStatusUiState) {
    val colors = MaterialTheme.colorScheme
    val status = event.status
    val icon = when (status) {
        EventStatus.RECHAZADO -> Icons.Rounded.Gavel
        EventStatus.PENDIENTE -> Icons.Rounded.PendingActions
        else -> Icons.Rounded.EventAvailable
    }
    val footer = when (status) {
        EventStatus.RECHAZADO -> rejectedFooter(state.rejection?.admin?.name, state.rejection?.date)
        EventStatus.PENDIENTE -> pendingFooter(event.publicationDate)
        else -> finishedFooter(event.endDate)
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
            Text(statusSectionTitle(status), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
        }
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(statusPolicyLine(status), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.primary)
                Text(
                    text = statusParagraph(status, state.rejection?.reason),
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = colors.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
                HorizontalDivider(color = colors.outlineVariant, modifier = Modifier.padding(top = 12.dp))
                Text(footer, fontSize = 12.sp, color = colors.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
            }
        }
    }
}

@Composable
private fun ReviewCard(
    review: ReviewItem,
    isEditing: Boolean,
    draft: String,
    error: String?,
    isPublishing: Boolean,
    focusRequested: Boolean,
    onOpenProfile: () -> Unit,
    onReplyClick: () -> Unit,
    onDraftChange: (String) -> Unit,
    onFocusLost: () -> Unit,
    onFocusHandled: () -> Unit,
    onCancel: () -> Unit,
    onPublish: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val (avatarBg, avatarFg) = avatarColors(review.authorId.hashCode())

    Surface(shape = RoundedCornerShape(20.dp), color = colors.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InitialsAvatar(
                    initials = review.initials,
                    container = avatarBg,
                    content = avatarFg,
                    size = 38.dp,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.clickable(onClick = onOpenProfile)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = review.authorName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(review.ago, fontSize = 11.sp, color = colors.onSurfaceVariant)
                    }
                    Row(
                        modifier = Modifier.padding(top = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        repeat(5) { index ->
                            Icon(
                                Icons.Rounded.Star,
                                contentDescription = null,
                                tint = if (index < review.rating) colors.primary else colors.outlineVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "${review.rating}.0",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(start = 5.dp)
                        )
                    }
                }
            }

            Text(
                text = review.text,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = colors.onSurface,
                modifier = Modifier.padding(top = 10.dp)
            )

            review.reply?.let { reply ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(colors.surfaceContainer, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                        Text("Respuesta del organizador", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.primary)
                    }
                    Text(reply.text, fontSize = 13.sp, lineHeight = 19.sp, color = colors.onSurface, modifier = Modifier.padding(top = 4.dp))
                }
            }

            if (isEditing) {
                RequiredLegend(modifier = Modifier.padding(top = 10.dp))
                FormTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    label = "Tu respuesta",
                    required = true,
                    placeholder = "Responde como organizador del evento…",
                    hint = FormTexts.REPLY_HINT,
                    error = error,
                    maxLength = FormLimits.REPLY,
                    minLines = 3,
                    containerColor = Color.Transparent,
                    onFocusLost = onFocusLost,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .formTarget(focusRequested, onFocusHandled)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (review.reply != null) {
                        TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = colors.error)) {
                            Text("Eliminar")
                        }
                    }
                    TextButton(onClick = onCancel) { Text("Cancelar") }
                    Button(onClick = onPublish, enabled = !isPublishing) {
                        if (isPublishing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = colors.onPrimary)
                        } else {
                            Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Publicar", modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                }
            } else {
                TextButton(onClick = onReplyClick, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.Reply, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        text = if (review.reply == null) "Responder" else "Editar respuesta",
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StepsSection(status: EventStatus, reviewsCount: Int) {
    val colors = MaterialTheme.colorScheme
    val icons = when (status) {
        EventStatus.RECHAZADO -> listOf(Icons.Rounded.EditNote, Icons.AutoMirrored.Rounded.Send)
        EventStatus.PENDIENTE -> listOf(Icons.Rounded.EditNote, Icons.Rounded.NotificationsActive)
        else -> listOf(Icons.Rounded.ContentCopy, Icons.Rounded.Download, Icons.Rounded.Reviews)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(statusStepsTitle(status), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
        statusSteps(status, reviewsCount).forEachIndexed { index, step ->
            Surface(shape = RoundedCornerShape(20.dp), color = colors.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(icons[index], contentDescription = null, tint = colors.primary, modifier = Modifier.size(24.dp))
                    Column {
                        Text(step.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
                        Text(
                            text = step.description,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/** Barra fija con la acción secundaria (texto) a la izquierda y la principal (relleno) a la derecha. */
@Composable
private fun ActionBar(
    status: EventStatus,
    onRemove: () -> Unit,
    onResubmit: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDownload: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val secondary: Triple<ImageVector, String, Color>
    val primary: Triple<ImageVector, String, () -> Unit>
    val onSecondary: () -> Unit
    when (status) {
        EventStatus.RECHAZADO -> {
            secondary = Triple(Icons.Rounded.Delete, "Eliminar", colors.error); onSecondary = onRemove
            primary = Triple(Icons.Rounded.Edit, "Reenviar", onResubmit)
        }
        EventStatus.PENDIENTE -> {
            secondary = Triple(Icons.AutoMirrored.Rounded.Undo, "Retirar", colors.onSurfaceVariant); onSecondary = onRemove
            primary = Triple(Icons.Rounded.Edit, "Editar", onEdit)
        }
        else -> {
            secondary = Triple(Icons.Rounded.Download, "Asistencia", colors.primary); onSecondary = onDownload
            primary = Triple(Icons.Rounded.ContentCopy, "Duplicar", onDuplicate)
        }
    }

    Surface(color = colors.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onSecondary, colors = ButtonDefaults.textButtonColors(contentColor = secondary.third)) {
                Icon(secondary.first, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(secondary.second, modifier = Modifier.padding(start = 8.dp))
            }
            Button(onClick = primary.third) {
                Icon(primary.first, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(primary.second, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun EventUnavailable(onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Este evento ya no está disponible aquí",
            fontSize = 16.sp,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) { Text("Volver") }
    }
}
