package edu.uniquindio.co.tribooo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.demoapp.core.theme.DemoAppTheme
import com.example.demoapp.features.home.HomeScreen
import com.example.demoapp.features.login.LoginScreen
import com.example.demoapp.features.register.RegisterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DemoAppTheme {
                var currentScreen by remember { mutableStateOf("home") }

                when (currentScreen) {
                    "home" -> HomeScreen(
                        onNavigateToLogin = { currentScreen = "login" },
                        onNavigateToRegister = { currentScreen = "register" }
                    )

                    "login" -> LoginScreen(
                        onNavigateToHome = { currentScreen = "home" }
                    )

                    "register" -> RegisterScreen(
                        onNavigateToHome = { currentScreen = "home" }
                    )
                }
            }
        }
    }
}
