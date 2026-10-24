package edu.uniquindio.co.tribooo.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.uniquindio.co.tribooo.core.component.ConfirmAlertDialog
import edu.uniquindio.co.tribooo.core.component.InitialsAvatar
import edu.uniquindio.co.tribooo.core.component.MainBottomBar
import edu.uniquindio.co.tribooo.core.component.MainTab
import edu.uniquindio.co.tribooo.core.component.avatarColors
import edu.uniquindio.co.tribooo.core.util.atAppZone
import edu.uniquindio.co.tribooo.core.util.initials
import edu.uniquindio.co.tribooo.domain.model.User
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ProfileScreen(
    onNavigateToFeed: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToAgenda: () -> Unit,
    onNavigateToCreateEvent: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToMyEvents: () -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToFollowers: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToModeration: () -> Unit,
    onNavigateToCommunityReports: () -> Unit,
    onNavigateToRejected: () -> Unit,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    snackbarMessage: String?,
    onSnackbarShown: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            MainBottomBar(
                selected = MainTab.PROFILE,
                onCreateClick = if (state.isAdmin) onNavigateToModeration else onNavigateToCreateEvent,
                enabledTabs = setOf(MainTab.FEED, MainTab.EXPLORE, MainTab.AGENDA, MainTab.PROFILE),
                onTabSelected = { tab ->
                    when (tab) {
                        MainTab.FEED -> onNavigateToFeed()
                        MainTab.EXPLORE -> onNavigateToExplore()
                        MainTab.AGENDA -> onNavigateToAgenda()
                        else -> Unit
                    }
                },
                isAdmin = state.isAdmin,
                moderationBadge = state.stats.pendingReview
            )
        }
    ) { innerPadding ->
        val user = state.user
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ProfileTopBar(onLogoutClick = viewModel::onLogoutClick)

            // Tras cerrar sesión el usuario deja de existir un instante antes de salir de la pantalla
            if (user != null) {
                ProfileHeader(user = user, state = state)

                AboutCard(user = user, onEditClick = viewModel::onEditClick)

                if (state.isAdmin) {
                    AdminLinks(
                        stats = state.stats,
                        onNavigateToModeration = onNavigateToModeration,
                        onNavigateToCommunityReports = onNavigateToCommunityReports,
                        onNavigateToRejected = onNavigateToRejected,
                        onNavigateToNotifications = onNavigateToNotifications
                    )
                } else {
                    UserLinks(
                        stats = state.stats,
                        onNavigateToMyEvents = onNavigateToMyEvents,
                        onNavigateToSaved = onNavigateToSaved,
                        onNavigateToFollowers = onNavigateToFollowers,
                        onEditClick = viewModel::onEditClick,
                        onNavigateToNotifications = onNavigateToNotifications
                    )
                }

                ThemeRow(darkTheme = darkTheme, onDarkThemeChange = onDarkThemeChange)

                Button(
                    onClick = viewModel::onDeleteAccountClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Rounded.DeleteForever, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("Eliminar mi cuenta", modifier = Modifier.padding(start = 8.dp))
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (state.showEditDialog) {
        EditProfileSheet(
            state = state,
            cities = viewModel.cities,
            onNameChange = viewModel::onNameChange,
            onCityChange = viewModel::onCityChange,
            onNeighborhoodChange = viewModel::onNeighborhoodChange,
            onBioChange = viewModel::onBioChange,
            onSave = viewModel::onSaveEdit,
            onDismiss = viewModel::onDismissEditDialog
        )
    }

    if (state.showLogoutDialog) {
        ConfirmAlertDialog(
            title = "¿Cerrar sesión?",
            message = "Volverás a la pantalla de inicio de sesión. Tus eventos confirmados y guardados se mantienen en tu cuenta.",
            confirmLabel = "Cerrar sesión",
            dismissLabel = "Seguir aquí",
            onConfirm = viewModel::onConfirmLogout,
            onDismiss = viewModel::onDismissLogoutDialog
        )
    }

    if (state.showDeleteDialog) {
        ConfirmAlertDialog(
            title = "¿Eliminar tu cuenta?",
            message = "Se borrarán los eventos que organizas y tus confirmaciones de asistencia. Esta acción no se puede deshacer.",
            confirmLabel = "Eliminar",
            dismissLabel = "Conservar cuenta",
            onConfirm = viewModel::onConfirmDeleteAccount,
            onDismiss = viewModel::onDismissDeleteDialog
        )
    }

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) onNavigateToLogin()
    }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onSnackbarShown()
        }
    }

    // Mensaje que llega de otra pantalla (p. ej. al publicar un evento desde aquí)
    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            snackbarHostState.showSnackbar(snackbarMessage)
            onSnackbarShown()
        }
    }
}

@Composable
private fun ProfileTopBar(onLogoutClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Perfil",
            fontSize = 26.sp,
            lineHeight = 32.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(onClick = onLogoutClick) {
            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("Cerrar sesión", modifier = Modifier.padding(start = 6.dp))
        }
    }
}

@Composable
private fun ProfileHeader(user: User, state: ProfileUiState) {
    val colors = MaterialTheme.colorScheme
    val (avatarContainer, avatarContent) = avatarColors(user.id.hashCode())
    val registered = user.dateRegistered.atAppZone()
    val month = registered.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es"))

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = colors.surfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                InitialsAvatar(
                    initials = user.name.initials(),
                    container = avatarContainer,
                    content = avatarContent,
                    size = 64.dp,
                    shape = CircleShape,
                    fontSize = 22.sp
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(user.name, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
                    Text(
                        text = "Barrio ${user.neighborhood} · desde $month ${registered.year}",
                        fontSize = 13.sp,
                        color = colors.onSurfaceVariant
                    )
                    Text(
                        text = if (state.isAdmin) "Administrador" else "Usuario",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.onSecondaryContainer,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(colors.secondaryContainer, RoundedCornerShape(8.dp))
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
            }

            val stats = state.stats
            Row(modifier = Modifier.fillMaxWidth()) {
                if (state.isAdmin) {
                    StatCell("Por revisar", stats.pendingReview.toString(), Modifier.weight(1f))
                    StatCell("Rechazadas", stats.rejected.toString(), Modifier.weight(1f))
                    StatCell("Resueltas", stats.resolved.toString(), Modifier.weight(1f))
                } else {
                    StatCell("Seguidores", stats.followers.toString(), Modifier.weight(1f))
                    StatCell("Asistencias", stats.attendances.toString(), Modifier.weight(1f))
                    StatCell("Creados", stats.created.toString(), Modifier.weight(1f))
                    StatCell("Calificación", stats.ratingLabel, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AboutCard(user: User, onEditClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = colors.surfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sobre mí", style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                TextButton(onClick = onEditClick) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Editar", modifier = Modifier.padding(start = 6.dp))
                }
            }
            Text(
                text = user.bio ?: "Aún no has escrito tu biografía.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = if (user.bio == null) colors.onSurfaceVariant else colors.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                InfoItem(Icons.Rounded.LocationCity, user.city)
                InfoItem(Icons.Rounded.Place, user.neighborhood)
            }
        }
    }
}

@Composable
private fun InfoItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Text(text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun UserLinks(
    stats: ProfileStats,
    onNavigateToMyEvents: () -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToFollowers: () -> Unit,
    onEditClick: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    LinksCard {
        LinkRow(Icons.Rounded.Event, "Mis eventos", stats.created, onNavigateToMyEvents)
        LinkRow(Icons.Rounded.Bookmark, "Eventos guardados", stats.saved, onNavigateToSaved)
        LinkRow(Icons.Rounded.Groups, "Seguidores", stats.followers, onNavigateToFollowers)
        LinkRow(Icons.Rounded.Person, "Editar datos personales", null, onEditClick)
        LinkRow(Icons.Rounded.Notifications, "Notificaciones", stats.unreadNotifications, onNavigateToNotifications)
    }
}

@Composable
private fun AdminLinks(
    stats: ProfileStats,
    onNavigateToModeration: () -> Unit,
    onNavigateToCommunityReports: () -> Unit,
    onNavigateToRejected: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    LinksCard {
        LinkRow(Icons.Rounded.Shield, "Moderar publicaciones", stats.pendingReview, onNavigateToModeration)
        LinkRow(Icons.Rounded.Flag, "Reportes de la comunidad", stats.reports, onNavigateToCommunityReports)
        LinkRow(Icons.Rounded.Block, "Publicaciones rechazadas", stats.rejected, onNavigateToRejected)
        LinkRow(Icons.Rounded.Notifications, "Notificaciones", stats.unreadNotifications, onNavigateToNotifications)
    }
}

@Composable
private fun LinksCard(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) { content() }
    }
}

@Composable
private fun LinkRow(icon: ImageVector, label: String, count: Int?, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Surface(onClick = onClick, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
            Text(label, fontSize = 15.sp, color = colors.onSurface, modifier = Modifier.weight(1f))
            if (count != null && count > 0) {
                Text(
                    text = count.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onPrimaryContainer,
                    modifier = Modifier
                        .background(colors.primaryContainer, RoundedCornerShape(10.dp))
                        .padding(horizontal = 9.dp, vertical = 2.dp)
                )
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun ThemeRow(darkTheme: Boolean, onDarkThemeChange: (Boolean) -> Unit) {
    val colors = MaterialTheme.colorScheme

    LinksCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(Icons.Rounded.DarkMode, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
            Text("Tema oscuro", fontSize = 15.sp, color = colors.onSurface, modifier = Modifier.weight(1f))
            Switch(checked = darkTheme, onCheckedChange = onDarkThemeChange)
        }
    }
}
