package edu.uniquindio.co.tribooo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import edu.uniquindio.co.tribooo.core.theme.TriboooTheme
import edu.uniquindio.co.tribooo.navigation.AppNavigation
import edu.uniquindio.co.tribooo.navigation.AppViewModel

// Permite que Hilt inyecte dependencias (y los ViewModels) en esta Activity y sus pantallas
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Mismo ViewModel que usa AppNavigation: guarda el tema elegido en Perfil
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // El splash del sistema (solo color primario) da paso de inmediato al splash de Compose
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkTheme by appViewModel.darkTheme.collectAsStateWithLifecycle()
            TriboooTheme(darkTheme = darkTheme ?: isSystemInDarkTheme()) {
                // La navegación de la aplicación se maneja en AppNavigation
                AppNavigation()
            }
        }
    }
}
