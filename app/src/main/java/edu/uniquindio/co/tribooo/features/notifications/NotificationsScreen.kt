package edu.uniquindio.co.tribooo.features.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Verified
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.uniquindio.co.tribooo.core.component.InitialsAvatar
import edu.uniquindio.co.tribooo.core.component.avatarColors
import edu.uniquindio.co.tribooo.domain.model.NotificationType

@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEventDetail: (eventId: String, commentId: String?) -> Unit,
    onNavigateToUser: (userId: String) -> Unit,
    snackbarMessage: String?,
    onSnackbarShown: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
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
            NotificationsTopBar(
                summary = state.summary,
                hasUnread = state.unreadCount > 0,
                onBack = onNavigateBack,
                onMarkAllRead = viewModel::onMarkAllRead
            )

            if (state.items.isEmpty()) {
                EmptyNotifications()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.items, key = { it.id }) { item ->
                        NotificationCard(
                            item = item,
                            avatarSeed = state.items.indexOf(item),
                            onClick = {
                                viewModel.onNotificationClick(item)
                                val userId = item.userId
                                val eventId = item.eventId
                                when {
                                    userId != null -> onNavigateToUser(userId)
                                    eventId != null -> onNavigateToEventDetail(eventId, item.commentId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Mensaje que llega de otra pantalla (p. ej. el aviso provisional del perfil de usuario)
    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            snackbarHostState.showSnackbar(snackbarMessage)
            onSnackbarShown()
        }
    }
}

@Composable
private fun NotificationsTopBar(
    summary: String,
    hasUnread: Boolean,
    onBack: () -> Unit,
    onMarkAllRead: () -> Unit
) {
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
            Text("Notificaciones", fontSize = 20.sp, color = colors.onSurface, modifier = Modifier.weight(1f))
            if (hasUnread) {
                TextButton(onClick = onMarkAllRead) {
                    Text("Marcar leídas", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
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
private fun NotificationCard(item: NotificationItem, avatarSeed: Int, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val cardColor = if (item.unread) colors.surfaceContainerLowest else colors.surfaceContainerLow

    Surface(onClick = onClick, shape = RoundedCornerShape(24.dp), color = cardColor, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NotificationAvatar(item = item, avatarSeed = avatarSeed, borderColor = cardColor)

            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                    Text(
                        text = headline(item),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = colors.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(item.ago, fontSize = 11.sp, color = colors.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                }

                if (item.quote != null) {
                    Text(
                        text = item.quote,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .background(colors.surfaceContainer, RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(callToAction(item.type), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.primary)
                        Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = colors.primary, modifier = Modifier.size(17.dp))
                    }
                    if (item.unread) {
                        Box(modifier = Modifier.size(8.dp).background(colors.primary, CircleShape))
                    }
                }
            }
        }
    }
}

/** Avatar de quien generó el aviso (o ícono de moderación) con la insignia del tipo en la esquina. */
@Composable
private fun NotificationAvatar(item: NotificationItem, avatarSeed: Int, borderColor: Color) {
    val colors = MaterialTheme.colorScheme
    val isModeration = item.who == null
    val (avatarBg, avatarFg) = avatarColors(avatarSeed)

    val (badgeIcon, badgeBg, badgeFg) = when (item.type) {
        NotificationType.COMMENT -> Triple(Icons.Rounded.Forum, colors.primary, colors.onPrimary)
        NotificationType.FOLLOWER -> Triple(Icons.Rounded.PersonAdd, colors.tertiary, colors.onTertiary)
        NotificationType.APPROVAL -> Triple(Icons.Rounded.Verified, colors.primary, colors.onPrimary)
        NotificationType.REJECTION -> Triple(Icons.Rounded.Block, colors.error, colors.onError)
    }

    Box {
        if (isModeration) {
            Box(
                modifier = Modifier.size(44.dp).background(colors.secondaryContainer, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(moderationIcon(item.type), contentDescription = null, tint = colors.onSecondaryContainer)
            }
        } else {
            InitialsAvatar(
                initials = item.initials,
                container = avatarBg,
                content = avatarFg,
                size = 44.dp,
                shape = RoundedCornerShape(16.dp),
                fontSize = 14.sp
            )
        }
        Surface(
            shape = CircleShape,
            color = badgeBg,
            border = BorderStroke(2.dp, borderColor),
            modifier = Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 4.dp).size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(badgeIcon, contentDescription = null, tint = badgeFg, modifier = Modifier.size(13.dp))
            }
        }
    }
}

@Composable
private fun EmptyNotifications() {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.NotificationsNone, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(48.dp))
        Text("Aún no tienes avisos", fontSize = 15.sp, color = colors.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
    }
}

private fun moderationIcon(type: NotificationType): ImageVector =
    if (type == NotificationType.REJECTION) Icons.Rounded.Block else Icons.Rounded.Verified

private fun callToAction(type: NotificationType): String = when (type) {
    NotificationType.COMMENT -> "Ver comentario"
    NotificationType.FOLLOWER -> "Ver perfil"
    NotificationType.APPROVAL, NotificationType.REJECTION -> "Ver evento"
}

/** Línea principal: nombre en negrita + acción. */
private fun headline(item: NotificationItem) = buildAnnotatedString {
    val bold = SpanStyle(fontWeight = FontWeight.Medium)
    when (item.type) {
        NotificationType.COMMENT -> {
            withStyle(bold) { append(item.who.orEmpty()) }
            append(" comentó en tu evento “${item.eventTitle}”")
        }
        NotificationType.FOLLOWER -> {
            withStyle(bold) { append(item.who.orEmpty()) }
            append(" empezó a seguirte")
        }
        NotificationType.APPROVAL -> append("Tu evento “${item.eventTitle}” fue aprobado y ya está en el feed")
        NotificationType.REJECTION -> append("Tu evento “${item.eventTitle}” fue rechazado")
    }
}
