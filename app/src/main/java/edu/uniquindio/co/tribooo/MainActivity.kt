package edu.uniquindio.co.tribooo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import edu.uniquindio.co.tribooo.core.theme.TriboooTheme
import edu.uniquindio.co.tribooo.navigation.AppNavigation

// Permite que Hilt inyecte dependencias (y los ViewModels) en esta Activity y sus pantallas
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TriboooTheme {
                // La navegación de la aplicación se maneja en AppNavigation
                AppNavigation()
            }
        }
    }
}
