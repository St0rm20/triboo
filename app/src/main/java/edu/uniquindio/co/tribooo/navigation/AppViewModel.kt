package edu.uniquindio.co.tribooo.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Leer la sesión toma milisegundos; este mínimo deja ver la animación de entrada del logo (~600 ms)
private const val MIN_SPLASH_MILLIS = 700L

@HiltViewModel
class AppViewModel @Inject constructor(
    getCurrentUser: GetCurrentUserUseCase
) : ViewModel() {

    /** Primera pantalla según la sesión guardada; null mientras se muestra el splash. */
    private val _startDestination = MutableStateFlow<MainRoutes?>(null)
    val startDestination: StateFlow<MainRoutes?> = _startDestination.asStateFlow()

    /** Tema elegido en Perfil (solo en memoria); null mientras se siga el del sistema. */
    private val _darkTheme = MutableStateFlow<Boolean?>(null)
    val darkTheme: StateFlow<Boolean?> = _darkTheme.asStateFlow()

    fun setDarkTheme(enabled: Boolean) {
        _darkTheme.value = enabled
    }

    init {
        viewModelScope.launch {
            // La sesión se lee en paralelo a la animación de entrada del splash
            val minSplash = async { delay(MIN_SPLASH_MILLIS) }
            // Fuera del hilo principal: la sesión y los repositorios en memoria no deben retrasar el primer cuadro
            val user = withContext(Dispatchers.Default) { getCurrentUser() }
            minSplash.await()

            // Solo se decide una vez: después la navegación la manejan login, registro y logout
            _startDestination.value = if (user != null) MainRoutes.Feed else MainRoutes.Onboarding
        }
    }
}
