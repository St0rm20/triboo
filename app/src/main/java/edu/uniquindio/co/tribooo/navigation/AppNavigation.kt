package edu.uniquindio.co.tribooo.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.uniquindio.co.tribooo.features.createevent.CreateEventScreen
import edu.uniquindio.co.tribooo.features.feed.FeedScreen
import edu.uniquindio.co.tribooo.features.home.HomeScreen
import edu.uniquindio.co.tribooo.features.login.LoginScreen
import edu.uniquindio.co.tribooo.features.register.RegisterScreen

// Clave para pasar el mensaje del snackbar de una pantalla al feed
private const val FEED_MESSAGE_KEY = "feed_message"

@Composable
fun AppNavigation() {
    // Controlador que maneja la navegación entre pantallas
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = MainRoutes.Home // Primera pantalla al iniciar la app
        ) {

            composable<MainRoutes.Home> {
                HomeScreen(
                    onNavigateToLogin = {
                        navController.navigate(MainRoutes.Login)
                    },
                    onNavigateToRegister = {
                        navController.navigate(MainRoutes.Register)
                    }
                )
            }

            composable<MainRoutes.Login> {
                LoginScreen(
                    onNavigateToHome = {
                        navController.navigateToFeed()
                    }
                )
            }

            composable<MainRoutes.Register> {
                RegisterScreen(
                    onNavigateToHome = {
                        navController.navigateToFeed()
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

        }
    }
}

/** Entra al feed y elimina del historial las pantallas de bienvenida y autenticación. */
private fun NavHostController.navigateToFeed() {
    navigate(MainRoutes.Feed) {
        popUpTo(MainRoutes.Home) { inclusive = true }
    }
}
