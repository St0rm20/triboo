package edu.uniquindio.co.tribooo.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.uniquindio.co.tribooo.features.home.HomeScreen
import edu.uniquindio.co.tribooo.features.login.LoginScreen
import edu.uniquindio.co.tribooo.features.register.RegisterScreen

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
                    // Por ahora no hay otra pantalla después del login, se regresa a Home
                    onNavigateToHome = {
                        navController.popBackStack()
                    }
                )
            }

            composable<MainRoutes.Register> {
                RegisterScreen(
                    onNavigateToHome = {
                        navController.popBackStack()
                    }
                )
            }

        }
    }
}
