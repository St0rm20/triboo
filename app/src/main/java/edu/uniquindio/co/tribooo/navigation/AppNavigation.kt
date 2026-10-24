package edu.uniquindio.co.tribooo.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.uniquindio.co.tribooo.domain.model.ModerationTab
import edu.uniquindio.co.tribooo.features.agenda.AgendaScreen
import edu.uniquindio.co.tribooo.features.createevent.CreateEventScreen
import edu.uniquindio.co.tribooo.features.eventdetail.EventDetailScreen
import edu.uniquindio.co.tribooo.features.explore.ExploreScreen
import edu.uniquindio.co.tribooo.features.feed.FeedScreen
import edu.uniquindio.co.tribooo.features.eventstatus.EventStatusScreen
import edu.uniquindio.co.tribooo.features.followers.FollowersScreen
import edu.uniquindio.co.tribooo.features.login.LoginScreen
import edu.uniquindio.co.tribooo.features.login.PasswordRecoveryScreen
import edu.uniquindio.co.tribooo.features.moderation.ModerationScreen
import edu.uniquindio.co.tribooo.features.myevents.MyEventsScreen
import edu.uniquindio.co.tribooo.features.notifications.NotificationsScreen
import edu.uniquindio.co.tribooo.features.onboarding.OnboardingScreen
import edu.uniquindio.co.tribooo.features.profile.ProfileScreen
import edu.uniquindio.co.tribooo.features.register.RegisterScreen
import edu.uniquindio.co.tribooo.features.saved.SavedEventsScreen
import edu.uniquindio.co.tribooo.features.splash.SplashScreen
import edu.uniquindio.co.tribooo.features.userprofile.UserProfileScreen

// Clave para pasar el mensaje del snackbar de una pantalla al feed o al perfil
private const val FEED_MESSAGE_KEY = "feed_message"

@Composable
fun AppNavigation(
    appViewModel: AppViewModel = hiltViewModel()
) {
    // Controlador que maneja la navegación entre pantallas
    val navController = rememberNavController()
    val startDestination by appViewModel.startDestination.collectAsStateWithLifecycle()
    val darkTheme by appViewModel.darkTheme.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        // Mientras se lee la sesión guardada se muestra el splash
        val start = startDestination
        if (start == null) {
            SplashScreen()
            return@Surface
        }

        NavHost(
            navController = navController,
            startDestination = start // Feed si hay sesión guardada; guía inicial si no
        ) {

            composable<MainRoutes.Onboarding> {
                OnboardingScreen(
                    onNavigateToRegister = {
                        navController.replaceWith(MainRoutes.Register)
                    },
                    onNavigateToLogin = {
                        navController.replaceWith(MainRoutes.Login)
                    }
                )
            }

            composable<MainRoutes.Login> {
                LoginScreen(
                    onNavigateToHome = {
                        navController.navigateToFeed()
                    },
                    onNavigateToRegister = {
                        navController.replaceWith(MainRoutes.Register)
                    },
                    onNavigateToPasswordRecovery = {
                        navController.navigate(MainRoutes.PasswordRecovery) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable<MainRoutes.PasswordRecovery> {
                PasswordRecoveryScreen(
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable<MainRoutes.Register> {
                RegisterScreen(
                    onNavigateToHome = {
                        navController.navigateToFeed()
                    },
                    onNavigateToLogin = {
                        navController.replaceWith(MainRoutes.Login)
                    }
                )
            }

            composable<MainRoutes.Feed> { backStackEntry ->
                val savedStateHandle = backStackEntry.savedStateHandle
                val message by savedStateHandle
                    .getStateFlow<String?>(FEED_MESSAGE_KEY, null)
                    .collectAsStateWithLifecycle()

                FeedScreen(
                    onNavigateToCreateEvent = {
                        navController.navigate(MainRoutes.CreateEvent)
                    },
                    onNavigateToModeration = {
                        navController.navigate(MainRoutes.Moderation(ModerationTab.PENDIENTES.name)) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToExplore = {
                        navController.navigateToTab(MainRoutes.Explore)
                    },
                    onNavigateToAgenda = {
                        navController.navigateToTab(MainRoutes.Agenda)
                    },
                    onNavigateToProfile = {
                        navController.navigateToTab(MainRoutes.Profile)
                    },
                    onNavigateToEventDetail = { eventId ->
                        navController.navigate(MainRoutes.EventDetail(eventId))
                    },
                    onNavigateToNotifications = {
                        navController.navigate(MainRoutes.Notifications) {
                            launchSingleTop = true
                        }
                    },
                    snackbarMessage = message,
                    onSnackbarShown = {
                        savedStateHandle[FEED_MESSAGE_KEY] = null
                    }
                )
            }

            composable<MainRoutes.Profile> { backStackEntry ->
                val savedStateHandle = backStackEntry.savedStateHandle
                val message by savedStateHandle
                    .getStateFlow<String?>(FEED_MESSAGE_KEY, null)
                    .collectAsStateWithLifecycle()

                ProfileScreen(
                    onNavigateToFeed = {
                        navController.navigateToTab(MainRoutes.Feed)
                    },
                    onNavigateToExplore = {
                        navController.navigateToTab(MainRoutes.Explore)
                    },
                    onNavigateToAgenda = {
                        navController.navigateToTab(MainRoutes.Agenda)
                    },
                    onNavigateToCreateEvent = {
                        navController.navigate(MainRoutes.CreateEvent)
                    },
                    onNavigateToLogin = {
                        navController.replaceWith(MainRoutes.Login)
                    },
                    onNavigateToMyEvents = {
                        navController.navigate(MainRoutes.MyEvents) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSaved = {
                        navController.navigate(MainRoutes.Saved) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToFollowers = {
                        navController.navigate(MainRoutes.Followers) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToNotifications = {
                        navController.navigate(MainRoutes.Notifications) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToModeration = {
                        navController.navigate(MainRoutes.Moderation(ModerationTab.PENDIENTES.name)) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToCommunityReports = {
                        navController.navigate(MainRoutes.Moderation(ModerationTab.REPORTES.name)) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToRejected = {
                        navController.navigate(MainRoutes.Moderation(ModerationTab.RECHAZADAS.name)) {
                            launchSingleTop = true
                        }
                    },
                    darkTheme = darkTheme ?: isSystemInDarkTheme(),
                    onDarkThemeChange = appViewModel::setDarkTheme,
                    snackbarMessage = message,
                    onSnackbarShown = {
                        savedStateHandle[FEED_MESSAGE_KEY] = null
                    }
                )
            }

            composable<MainRoutes.Notifications> { backStackEntry ->
                val savedStateHandle = backStackEntry.savedStateHandle
                val message by savedStateHandle
                    .getStateFlow<String?>(FEED_MESSAGE_KEY, null)
                    .collectAsStateWithLifecycle()

                NotificationsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEventDetail = { eventId, commentId ->
                        navController.navigate(MainRoutes.EventDetail(eventId, commentId))
                    },
                    onNavigateToUser = { userId ->
                        navController.navigate(MainRoutes.UserProfile(userId)) {
                            launchSingleTop = true
                        }
                    },
                    snackbarMessage = message,
                    onSnackbarShown = {
                        savedStateHandle[FEED_MESSAGE_KEY] = null
                    }
                )
            }

            composable<MainRoutes.MyEvents> { backStackEntry ->
                val savedStateHandle = backStackEntry.savedStateHandle
                val message by savedStateHandle
                    .getStateFlow<String?>(FEED_MESSAGE_KEY, null)
                    .collectAsStateWithLifecycle()

                MyEventsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEventDetail = { eventId ->
                        navController.navigate(MainRoutes.EventDetail(eventId))
                    },
                    onNavigateToEventStatus = { eventId ->
                        navController.navigate(MainRoutes.EventStatus(eventId)) {
                            launchSingleTop = true
                        }
                    },
                    snackbarMessage = message,
                    onSnackbarShown = {
                        savedStateHandle[FEED_MESSAGE_KEY] = null
                    }
                )
            }

            composable<MainRoutes.Saved> {
                SavedEventsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEventDetail = { eventId ->
                        navController.navigate(MainRoutes.EventDetail(eventId))
                    },
                    onNavigateToFeed = {
                        navController.navigateToTab(MainRoutes.Feed)
                    }
                )
            }

            composable<MainRoutes.Followers> { backStackEntry ->
                val savedStateHandle = backStackEntry.savedStateHandle
                val message by savedStateHandle
                    .getStateFlow<String?>(FEED_MESSAGE_KEY, null)
                    .collectAsStateWithLifecycle()

                FollowersScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToUser = { userId ->
                        navController.navigate(MainRoutes.UserProfile(userId)) {
                            launchSingleTop = true
                        }
                    },
                    snackbarMessage = message,
                    onSnackbarShown = {
                        savedStateHandle[FEED_MESSAGE_KEY] = null
                    }
                )
            }

            composable<MainRoutes.CreateEvent> {
                CreateEventScreen(
                    onClose = {
                        navController.popBackStack()
                    },
                    onEventPublished = { message ->
                        // El feed muestra la confirmación al volver
                        navController.previousBackStackEntry?.savedStateHandle?.set(FEED_MESSAGE_KEY, message)
                        navController.popBackStack()
                    }
                )
            }

            composable<MainRoutes.EventDetail> {
                // El id del evento llega al ViewModel por su SavedStateHandle
                EventDetailScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToUser = { userId ->
                        navController.navigate(MainRoutes.UserProfile(userId)) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable<MainRoutes.EventStatus> { backStackEntry ->
                val savedStateHandle = backStackEntry.savedStateHandle
                val message by savedStateHandle
                    .getStateFlow<String?>(FEED_MESSAGE_KEY, null)
                    .collectAsStateWithLifecycle()

                // El id del evento llega al ViewModel por su SavedStateHandle
                EventStatusScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToUser = { userId ->
                        navController.navigate(MainRoutes.UserProfile(userId)) {
                            launchSingleTop = true
                        }
                    },
                    onEventRemoved = { message ->
                        // Mis eventos muestra la confirmación al volver
                        navController.previousBackStackEntry?.savedStateHandle?.set(FEED_MESSAGE_KEY, message)
                        navController.popBackStack()
                    },
                    // PROVISIONAL: reenviar, editar y duplicar necesitan que Crear evento acepte datos previos.
                    // Quien lo implemente solo debe reemplazar cada línea por navController.navigate(MainRoutes.X(eventId)).
                    onResubmitEvent = { savedStateHandle[FEED_MESSAGE_KEY] = "Disponible pronto" },
                    onEditEvent = { savedStateHandle[FEED_MESSAGE_KEY] = "Disponible pronto" },
                    onDuplicateEvent = { savedStateHandle[FEED_MESSAGE_KEY] = "Disponible pronto" },
                    snackbarMessage = message,
                    onSnackbarShown = {
                        savedStateHandle[FEED_MESSAGE_KEY] = null
                    }
                )
            }

            composable<MainRoutes.Explore> {
                ExploreScreen(
                    onNavigateToFeed = {
                        navController.navigateToTab(MainRoutes.Feed)
                    },
                    onNavigateToAgenda = {
                        navController.navigateToTab(MainRoutes.Agenda)
                    },
                    onNavigateToProfile = {
                        navController.navigateToTab(MainRoutes.Profile)
                    },
                    onNavigateToCreateEvent = {
                        navController.navigate(MainRoutes.CreateEvent)
                    },
                    onNavigateToEventDetail = { eventId ->
                        navController.navigate(MainRoutes.EventDetail(eventId))
                    }
                )
            }

            composable<MainRoutes.Agenda> {
                AgendaScreen(
                    onNavigateToFeed = {
                        navController.navigateToTab(MainRoutes.Feed)
                    },
                    onNavigateToExplore = {
                        navController.navigateToTab(MainRoutes.Explore)
                    },

                    onNavigateToProfile = {
                        navController.navigateToTab(MainRoutes.Profile)
                    },
                    onNavigateToCreateEvent = {
                        navController.navigate(MainRoutes.CreateEvent)
                    },
                    onNavigateToEventDetail = { eventId ->
                        navController.navigate(MainRoutes.EventDetail(eventId))
                    }
                )
            }

            composable<MainRoutes.Moderation> {
                // La pestaña inicial llega al ViewModel por su SavedStateHandle
                ModerationScreen(
                    onNavigateToFeed = {
                        navController.navigateToTab(MainRoutes.Feed)
                    },
                    onNavigateToExplore = {
                        navController.navigateToTab(MainRoutes.Explore)
                    },
                    onNavigateToAgenda = {
                        navController.navigateToTab(MainRoutes.Agenda)
                    },
                    onNavigateToProfile = {
                        navController.navigateToTab(MainRoutes.Profile)
                    },
                    onNavigateToUser = { userId ->
                        navController.navigate(MainRoutes.UserProfile(userId)) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable<MainRoutes.UserProfile> {
                // El id del usuario llega al ViewModel por su SavedStateHandle
                UserProfileScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEventDetail = { eventId ->
                        navController.navigate(MainRoutes.EventDetail(eventId))
                    }
                )
            }

        }
    }
}

/** Cambia de pestaña (Inicio/Perfil) sin apilar pantallas y conservando el estado de cada una. */
private fun NavHostController.navigateToTab(route: MainRoutes) {
    navigate(route) {
        popUpTo(MainRoutes.Feed) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Entra al feed y elimina del historial la guía y las pantallas de autenticación. */
private fun NavHostController.navigateToFeed() {
    navigate(MainRoutes.Feed) {
        popUpTo(graph.id) { inclusive = true }
    }
}

/**
 * Cambia entre guía, login y registro sin acumularlas en el historial:
 * "atrás" desde cualquiera de ellas sale de la app, como en el prototipo.
 */
private fun NavHostController.replaceWith(route: MainRoutes) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
