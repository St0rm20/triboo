package edu.uniquindio.co.tribooo.features.moderation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.RestorePage
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.uniquindio.co.tribooo.core.component.InitialsAvatar
import edu.uniquindio.co.tribooo.core.component.MainBottomBar
import edu.uniquindio.co.tribooo.core.component.MainTab
import edu.uniquindio.co.tribooo.core.component.WrapRow
import edu.uniquindio.co.tribooo.core.component.avatarColors
import edu.uniquindio.co.tribooo.core.component.iconVector
import edu.uniquindio.co.tribooo.core.theme.categoryColors
import edu.uniquindio.co.tribooo.core.util.ModerationCardAction
import edu.uniquindio.co.tribooo.core.util.eventWhenLabel
import edu.uniquindio.co.tribooo.core.util.initials
import edu.uniquindio.co.tribooo.core.util.moderationActions
import edu.uniquindio.co.tribooo.core.util.moderationContext
import edu.uniquindio.co.tribooo.core.util.moderationCupoLabel
import edu.uniquindio.co.tribooo.core.util.moderationEmptyTexts
import edu.uniquindio.co.tribooo.core.util.moderationStatusLabel
import edu.uniquindio.co.tribooo.core.util.moderationSummary
import edu.uniquindio.co.tribooo.core.util.moderationTabLabel
import edu.uniquindio.co.tribooo.core.util.reportLines
import edu.uniquindio.co.tribooo.core.util.reportedByLabel
import edu.uniquindio.co.tribooo.core.util.rejectionFooter
import edu.uniquindio.co.tribooo.core.util.resolutionFooter
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.ModerationCase
import edu.uniquindio.co.tribooo.domain.model.ModerationTab
import edu.uniquindio.co.tribooo.domain.usecase.moderation.ModerationCounts

@Composable
fun ModerationScreen(
    onNavigateToFeed: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToAgenda: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToUser: (userId: String) -> Unit,
    viewModel: ModerationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val colors = MaterialTheme.colorScheme

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.surface,
        bottomBar = {
            MainBottomBar(
                selected = null,
                // El martillo lleva a la cola de pendientes
                onCreateClick = { viewModel.onTabSelected(ModerationTab.PENDIENTES) },
                enabledTabs = setOf(MainTab.FEED, MainTab.EXPLORE, MainTab.AGENDA, MainTab.PROFILE),
                onTabSelected = { tab ->
                    when (tab) {
                        MainTab.FEED -> onNavigateToFeed()
                        MainTab.EXPLORE -> onNavigateToExplore()
                        MainTab.AGENDA -> onNavigateToAgenda()
                        MainTab.PROFILE -> onNavigateToProfile()
                    }
                },
                isAdmin = state.isAdmin,
                moderationBadge = state.counts.pending
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .statusBarsPadding()
        ) {
            if (!state.isAdmin) {
                CenteredMessage(Icons.Rounded.Lock, "No tienes acceso a esta sección", null)
            } else {
                ModerationHeader(state.counts)
                TabRow(selected = state.tab, counts = state.counts, onSelect = viewModel::onTabSelected)

                if (state.cases.isEmpty()) {
                    val (title, text) = moderationEmptyTexts(state.tab)
                    CenteredMessage(emptyIcon(state.tab), title, text)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(state.cases, key = { it.event.id }) { case ->
                            CaseCard(
                                case = case,
                                tab = state.tab,
                                onAuthorClick = { onNavigateToUser(case.event.organizer.id) },
                                onVerify = { viewModel.onVerify(case) },
                                onReject = { viewModel.onRejectClick(case) },
                                onResolve = { viewModel.onResolve(case) },
                                onRestore = { viewModel.onRestore(case) }
                            )
                        }
                    }
                }
            }
        }
    }

    state.rejectSheet?.let { sheet ->
        RejectSheet(
            sheet = sheet,
            onReasonChange = viewModel::onReasonChange,
            onConfirm = viewModel::onConfirmReject,
            onDismiss = viewModel::onDismissRejectSheet
        )
    }

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.onMessageShown()
    }
}

private fun emptyIcon(tab: ModerationTab): ImageVector = when (tab) {
    ModerationTab.PENDIENTES -> Icons.Rounded.Inbox
    ModerationTab.REPORTES -> Icons.Rounded.Flag
    ModerationTab.RECHAZADAS -> Icons.Rounded.Block
    ModerationTab.RESUELTAS -> Icons.Rounded.TaskAlt
}

@Composable
private fun ModerationHeader(counts: ModerationCounts) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(shape = RoundedCornerShape(16.dp), color = colors.primaryContainer, modifier = Modifier.size(48.dp)) {
            Icon(
                Icons.Rounded.Gavel,
                contentDescription = null,
                tint = colors.onPrimaryContainer,
                modifier = Modifier.padding(12.dp)
            )
        }
        Column {
            Text("Moderación", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
            Text(
                text = moderationSummary(counts.pending, counts.reports),
                fontSize = 13.sp,
                color = colors.onSurfaceVariant
            )
        }
    }
}

/** Chips de las cuatro pestañas, con desplazamiento horizontal si no caben. */
@Composable
private fun TabRow(selected: ModerationTab, counts: ModerationCounts, onSelect: (ModerationTab) -> Unit) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ModerationTab.entries.forEach { tab ->
            val count = when (tab) {
                ModerationTab.PENDIENTES -> counts.pending
                ModerationTab.RECHAZADAS -> counts.rejected
                ModerationTab.REPORTES -> counts.reports
                ModerationTab.RESUELTAS -> counts.resolved
            }
            val isSelected = tab == selected
            Surface(
                onClick = { onSelect(tab) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) colors.primary else Color.Transparent,
                contentColor = if (isSelected) colors.onPrimary else colors.onSurfaceVariant,
                border = if (isSelected) null else BorderStroke(1.dp, colors.outline)
            ) {
                Text(
                    text = moderationTabLabel(tab, count),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun CaseCard(
    case: ModerationCase,
    tab: ModerationTab,
    onAuthorClick: () -> Unit,
    onVerify: () -> Unit,
    onReject: () -> Unit,
    onResolve: () -> Unit,
    onRestore: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val event = case.event

    Surface(shape = RoundedCornerShape(24.dp), color = colors.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Etiquetas: estado, categoría y, si está cerrado, "Resuelta"
            WrapRow(horizontalSpacing = 6.dp, verticalSpacing = 6.dp) {
                StatusTag(event.status)
                CategoryTag(case)
                if (tab == ModerationTab.RESUELTAS && case.resolution != null) {
                    Tag(Icons.Rounded.TaskAlt, "Resuelta", colors.secondaryContainer, colors.onSecondaryContainer)
                }
            }

            Text(event.title, fontSize = 18.sp, lineHeight = 23.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val (container, content) = avatarColors(event.organizer.id.hashCode())
                InitialsAvatar(
                    initials = event.organizer.name.initials(),
                    container = container,
                    content = content,
                    modifier = Modifier.clickable(onClick = onAuthorClick)
                )
                Column {
                    Text(event.organizer.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
                    Text(moderationContext(case, tab), fontSize = 12.sp, color = colors.onSurfaceVariant)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                InfoLine(Icons.Rounded.Schedule, eventWhenLabel(event.startDate, event.endDate))
                InfoLine(Icons.Rounded.Place, "${event.place} · ${event.neighborhood}")
                InfoLine(Icons.Rounded.Groups, moderationCupoLabel(event.cupo))
            }

            Text(event.description, fontSize = 14.sp, lineHeight = 20.sp, color = colors.onSurfaceVariant)

            if (case.openReports.isNotEmpty() && event.status != EventStatus.RECHAZADO) {
                NoteBlock(
                    icon = Icons.Rounded.Flag,
                    title = reportedByLabel(case.openReports),
                    lines = reportLines(case.openReports),
                    container = colors.tertiaryContainer,
                    content = colors.onTertiaryContainer
                )
            }
            if (event.status == EventStatus.RECHAZADO) {
                NoteBlock(
                    icon = Icons.Rounded.Block,
                    title = "Motivo enviado al autor",
                    lines = listOfNotNull(case.rejection?.reason, rejectionFooter(case.rejection)),
                    container = colors.errorContainer,
                    content = colors.onErrorContainer
                )
            }
            if (tab == ModerationTab.RESUELTAS && case.resolution != null) {
                NoteBlock(
                    icon = Icons.Rounded.TaskAlt,
                    title = resolutionFooter(case.resolution!!),
                    lines = emptyList(),
                    container = colors.secondaryContainer,
                    content = colors.onSecondaryContainer
                )
            }

            val actions = moderationActions(event.status, tab)
            if (actions.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    actions.forEach { action ->
                        when (action) {
                            ModerationCardAction.VERIFY ->
                                Button(onClick = onVerify, modifier = Modifier.weight(1f)) {
                                    ButtonContent(Icons.Rounded.Verified, "Verificar")
                                }
                            ModerationCardAction.REJECT ->
                                OutlinedButton(
                                    onClick = onReject,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                                    border = BorderStroke(1.dp, colors.error),
                                    modifier = Modifier.weight(1f)
                                ) { ButtonContent(Icons.Rounded.Block, "Rechazar") }
                            ModerationCardAction.RESOLVE ->
                                Button(onClick = onResolve, modifier = Modifier.weight(1f)) {
                                    ButtonContent(Icons.Rounded.TaskAlt, "Marcar resuelta")
                                }
                            ModerationCardAction.RESTORE ->
                                Button(onClick = onRestore, modifier = Modifier.weight(1f)) {
                                    ButtonContent(Icons.Rounded.RestorePage, "Restaurar")
                                }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ButtonContent(icon: ImageVector, label: String) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
    Text(label, modifier = Modifier.padding(start = 6.dp), maxLines = 1)
}

@Composable
private fun StatusTag(status: EventStatus) {
    val colors = MaterialTheme.colorScheme
    when (status) {
        EventStatus.PENDIENTE ->
            Tag(Icons.Rounded.PendingActions, moderationStatusLabel(status), colors.tertiaryContainer, colors.onTertiaryContainer)
        EventStatus.RECHAZADO ->
            Tag(Icons.Rounded.Block, moderationStatusLabel(status), colors.errorContainer, colors.onErrorContainer)
        else ->
            Tag(Icons.Rounded.Verified, moderationStatusLabel(status), colors.primaryContainer, colors.onPrimaryContainer)
    }
}

@Composable
private fun CategoryTag(case: ModerationCase) {
    val category = case.event.category
    val tones = categoryColors(category)
    Tag(category.iconVector(), category.name, tones.container, tones.content)
}

@Composable
private fun Tag(icon: ImageVector, label: String, container: Color, content: Color) {
    Row(
        modifier = Modifier.background(container, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(14.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = content)
    }
}

@Composable
private fun InfoLine(icon: ImageVector, text: String) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Text(text, fontSize = 13.sp, color = colors.onSurface)
    }
}

/** Bloque de color con ícono, título y líneas de apoyo (reportes, motivo de rechazo o cierre). */
@Composable
private fun NoteBlock(icon: ImageVector, title: String, lines: List<String>, container: Color, content: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(container, RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = content)
            lines.forEach { Text(it, fontSize = 13.sp, lineHeight = 18.sp, color = content) }
        }
    }
}

@Composable
private fun CenteredMessage(icon: ImageVector, title: String, text: String?) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(48.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
        if (text != null) {
            Text(
                text = text,
                fontSize = 14.sp,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
