package edu.uniquindio.co.tribooo.features.followers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.uniquindio.co.tribooo.core.component.InitialsAvatar
import edu.uniquindio.co.tribooo.core.component.avatarColors

@Composable
fun FollowersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUser: (userId: String) -> Unit,
    snackbarMessage: String?,
    onSnackbarShown: () -> Unit,
    viewModel: FollowersViewModel = hiltViewModel()
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
            FollowersTopBar(summary = state.summary, onBack = onNavigateBack)

            if (state.followers.isEmpty()) {
                EmptyFollowers()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(state.followers, key = { _, item -> item.user.id }) { index, item ->
                        FollowerCard(
                            item = item,
                            avatarSeed = index,
                            onOpenProfile = { onNavigateToUser(item.user.id) },
                            onToggleFollow = { viewModel.onToggleFollow(item) }
                        )
                    }
                }
            }
        }
    }

    // Mensaje propio del seguimiento y mensaje que llega de otra pantalla (aviso provisional del perfil)
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
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
private fun FollowersTopBar(summary: String, onBack: () -> Unit) {
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
            Text("Seguidores", fontSize = 20.sp, color = colors.onSurface)
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
private fun FollowerCard(
    item: FollowerItem,
    avatarSeed: Int,
    onOpenProfile: () -> Unit,
    onToggleFollow: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val (avatarBg, avatarFg) = avatarColors(avatarSeed)

    Surface(shape = RoundedCornerShape(20.dp), color = colors.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Zona izquierda: abre el perfil de la persona
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenProfile),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InitialsAvatar(
                    initials = item.initials,
                    container = avatarBg,
                    content = avatarFg,
                    size = 44.dp,
                    shape = RoundedCornerShape(16.dp),
                    fontSize = 14.sp
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.user.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.subtitle,
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            val padding = PaddingValues(horizontal = 16.dp)
            if (item.isFollowing) {
                Button(
                    onClick = onToggleFollow,
                    contentPadding = padding,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.secondaryContainer,
                        contentColor = colors.onSecondaryContainer
                    ),
                    modifier = Modifier.height(36.dp)
                ) { Text("Siguiendo", fontSize = 13.sp, fontWeight = FontWeight.Medium) }
            } else {
                OutlinedButton(
                    onClick = onToggleFollow,
                    contentPadding = padding,
                    border = BorderStroke(1.dp, colors.primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
                    modifier = Modifier.height(36.dp)
                ) { Text("Seguir", fontSize = 13.sp, fontWeight = FontWeight.Medium) }
            }
        }
    }
}

@Composable
private fun EmptyFollowers() {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.Groups, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(48.dp))
        Text(
            text = "Aún no tienes seguidores",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "Cuando alguien siga tus eventos, aparecerá aquí.",
            fontSize = 14.sp,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
