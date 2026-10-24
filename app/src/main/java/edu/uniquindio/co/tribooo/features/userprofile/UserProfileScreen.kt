package edu.uniquindio.co.tribooo.features.userprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.HowToReg
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.uniquindio.co.tribooo.core.component.EventThumbnail
import edu.uniquindio.co.tribooo.core.component.InitialsAvatar
import edu.uniquindio.co.tribooo.core.component.avatarColors
import edu.uniquindio.co.tribooo.core.component.iconVector
import edu.uniquindio.co.tribooo.core.theme.categoryColors
import edu.uniquindio.co.tribooo.core.util.CommunityRole
import edu.uniquindio.co.tribooo.core.util.eventWhenLabel
import edu.uniquindio.co.tribooo.core.util.firstName
import edu.uniquindio.co.tribooo.core.util.formatThousands
import edu.uniquindio.co.tribooo.core.util.initials
import edu.uniquindio.co.tribooo.core.util.monthYearLabel
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.User

@Composable
fun UserProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEventDetail: (eventId: String) -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
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
            UserTopBar(showReport = state.canInteract, onBack = onNavigateBack, onReport = viewModel::onReport)

            val user = state.user
            when {
                state.isLoading -> Unit
                user == null -> UserNotFound(onBack = onNavigateBack)
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item(key = "card") { UserCard(user = user, state = state, onToggleFollow = viewModel::onToggleFollow) }
                    item(key = "about") { AboutSection(user = user) }
                    item(key = "events-title") {
                        Text(
                            text = if (state.events.isEmpty()) "Eventos que organiza" else "Eventos que organiza · ${state.events.size}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    if (state.events.isEmpty()) {
                        item(key = "events-empty") {
                            Text(
                                text = "Todavía no ha organizado eventos. Asiste a los de su barrio.",
                                fontSize = 14.sp,
                                color = colors.onSurfaceVariant
                            )
                        }
                    } else {
                        items(state.events, key = { it.id }) { event ->
                            CompactEventCard(event = event, onClick = { onNavigateToEventDetail(event.id) })
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }
}

@Composable
private fun UserTopBar(showReport: Boolean, onBack: () -> Unit, onReport: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainer)
            .statusBarsPadding()
            .padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver", tint = colors.onSurface)
        }
        Text("Detalle de usuario", fontSize = 20.sp, color = colors.onSurface, modifier = Modifier.weight(1f))
        if (showReport) {
            IconButton(onClick = onReport) {
                Icon(Icons.Rounded.Flag, contentDescription = "Reportar perfil", tint = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun UserCard(user: User, state: UserProfileUiState, onToggleFollow: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val (avatarBg, avatarFg) = avatarColors(user.id.hashCode())

    Surface(shape = RoundedCornerShape(28.dp), color = colors.surfaceContainerLowest, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InitialsAvatar(
                initials = user.name.initials(),
                container = avatarBg,
                content = avatarFg,
                size = 78.dp,
                shape = RoundedCornerShape(26.dp),
                fontSize = 26.sp
            )
            Text(
                text = user.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = "${user.neighborhood} · en la comunidad desde ${user.dateRegistered.monthYearLabel()}",
                fontSize = 13.sp,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
            RoleBadge(state.role)

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(state.followers.formatThousands(), "seguidores", Modifier.weight(1f))
                StatCard(state.attendances.formatThousands(), "asistencias", Modifier.weight(1f))
                StatCard(state.createdEventsLabel, "eventos creados", Modifier.weight(1f))
                StatCard(state.ratingLabel, "calificación", Modifier.weight(1f))
            }

            if (state.canInteract) {
                val following = state.isFollowing
                Button(
                    onClick = onToggleFollow,
                    shape = RoundedCornerShape(16.dp),
                    colors = if (following) {
                        ButtonDefaults.buttonColors(
                            containerColor = colors.secondaryContainer,
                            contentColor = colors.onSecondaryContainer
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(
                        imageVector = if (following) Icons.Rounded.HowToReg else Icons.Rounded.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (following) "Siguiendo" else "Seguir",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleBadge(role: CommunityRole) {
    val colors = MaterialTheme.colorScheme
    val icon: ImageVector = when (role) {
        CommunityRole.ADMIN -> Icons.Rounded.AdminPanelSettings
        CommunityRole.LEADER, CommunityRole.ORGANIZER -> Icons.Rounded.WorkspacePremium
        CommunityRole.NEIGHBOR -> Icons.Rounded.Person
    }

    Row(
        modifier = Modifier
            .padding(top = 10.dp)
            .background(colors.secondaryContainer, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, contentDescription = null, tint = colors.onSecondaryContainer, modifier = Modifier.size(16.dp))
        Text(role.label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.onSecondaryContainer)
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .background(colors.surfaceContainer, RoundedCornerShape(16.dp))
            .padding(horizontal = 4.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
        Text(
            text = label,
            fontSize = 10.sp,
            lineHeight = 13.sp,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun AboutSection(user: User) {
    val colors = MaterialTheme.colorScheme

    Column(modifier = Modifier.padding(top = 6.dp)) {
        Text(
            text = "Sobre ${user.name.firstName()}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface
        )
        Text(
            text = user.bio?.takeIf { it.isNotBlank() } ?: "Esta persona aún no ha escrito su presentación.",
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun CompactEventCard(event: Event, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val tones = categoryColors(event.category)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = colors.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EventThumbnail(imageUrl = event.imageUrl, size = 56.dp)

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
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 5.dp)
                )
                Text(
                    text = eventWhenLabel(event.startDate, event.endDate),
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun UserNotFound(onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Este perfil ya no está disponible", fontSize = 16.sp, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) { Text("Volver") }
    }
}
