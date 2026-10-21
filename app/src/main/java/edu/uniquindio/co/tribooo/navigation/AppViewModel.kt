package edu.uniquindio.co.tribooo.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Leer la sesión toma milisegundos; sin este mínimo el splash solo parpadearía
private const val MIN_SPLASH_MILLIS = 1_200L

@HiltViewModel
class AppViewModel @Inject constructor(
    getCurrentUser: GetCurrentUserUseCase
) : ViewModel() {

    /** Primera pantalla según la sesión guardada; null mientras se muestra el splash. */
    private val _startDestination = MutableStateFlow<MainRoutes?>(null)
    val startDestination: StateFlow<MainRoutes?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            // La sesión se lee en paralelo a la animación de entrada del splash
            val minSplash = async { delay(MIN_SPLASH_MILLIS) }
            val user = getCurrentUser()
            minSplash.await()

            // Solo se decide una vez: después la navegación la manejan login, registro y logout
            _startDestination.value = if (user != null) MainRoutes.Feed else MainRoutes.Onboarding
        }
    }
}
