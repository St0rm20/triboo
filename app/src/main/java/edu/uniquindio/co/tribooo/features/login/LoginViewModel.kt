package edu.uniquindio.co.tribooo.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.uniquindio.co.tribooo.core.util.RequestResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isFormValid: Boolean = false,
    val loginResult: RequestResult? = null
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { currentState ->
            currentState.copy(
                email = email,
                emailError = null
            )
        }
        validateForm()
    }

    fun onPasswordChange(password: String) {
        _uiState.update { currentState ->
            currentState.copy(
                password = password,
                passwordError = null
            )
        }
        validateForm()
    }

    private fun validateForm() {
        val currentState = _uiState.value

        val emailError = when {
            currentState.email.isBlank() -> "El email es obligatorio"
            !isValidEmail(currentState.email) -> "El email no es válido"
            else -> null
        }
        val passwordError = when {
            currentState.password.isBlank() -> "La contraseña es obligatoria"
            currentState.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }

        _uiState.update {
            it.copy(
                emailError = emailError,
                passwordError = passwordError,
                isFormValid = emailError == null && passwordError == null
            )
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email)
    }

    fun login() {
        if (!_uiState.value.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(loginResult = RequestResult.Loading) }
            delay(2000)
            val message = "Inicio de sesión exitoso"
            _uiState.update { it.copy(loginResult = RequestResult.Success(message)) }
        }
    }

    fun resetLoginResult() {
        _uiState.update { it.copy(loginResult = null) }
    }
}
