package edu.uniquindio.co.tribooo.features.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.uniquindio.co.tribooo.core.util.RequestResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val city: String = "Ciudad 1",
    val address: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val cityError: String? = null,
    val addressError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isFormValid: Boolean = false,
    val registerResult: RequestResult? = null,
    val showConfirmDialog: Boolean = false
)

class RegisterViewModel : ViewModel() {

    val cities = listOf("Ciudad 1", "Ciudad 2", "Ciudad 3")

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
        validateForm()
    }

    fun onCityChange(city: String) {
        _uiState.update { it.copy(city = city, cityError = null) }
        validateForm()
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address, addressError = null) }
        validateForm()
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
        validateForm()
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
        validateForm()
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
        validateForm()
    }

    private fun validateForm() {
        val currentState = _uiState.value

        val nameError = when {
            currentState.name.isBlank() -> "El nombre es obligatorio"
            currentState.name.length < 3 -> "El nombre debe tener al menos 3 caracteres"
            else -> null
        }
        val cityError = if (currentState.city.isBlank()) "La ciudad es obligatoria" else null
        val addressError = if (currentState.address.isBlank()) "La dirección es obligatoria" else null
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
        val confirmPasswordError = when {
            currentState.confirmPassword.isBlank() -> "Debes confirmar la contraseña"
            currentState.confirmPassword != currentState.password -> "Las contraseñas no coinciden"
            else -> null
        }

        _uiState.update {
            it.copy(
                nameError = nameError,
                cityError = cityError,
                addressError = addressError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                isFormValid = listOf(
                    nameError,
                    cityError,
                    addressError,
                    emailError,
                    passwordError,
                    confirmPasswordError
                ).all { error -> error == null }
            )
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email)
    }

    fun onRegisterClick() {
        if (!_uiState.value.isFormValid) return
        _uiState.update { it.copy(showConfirmDialog = true) }
    }

    fun onDismissConfirmDialog() {
        _uiState.update { it.copy(showConfirmDialog = false) }
    }

    fun onConfirmRegister() {
        _uiState.update { it.copy(showConfirmDialog = false) }
        register()
    }

    private fun register() {
        viewModelScope.launch {
            _uiState.update { it.copy(registerResult = RequestResult.Loading) }
            delay(2000)
            val message = "Registro exitoso"
            _uiState.update { it.copy(registerResult = RequestResult.Success(message)) }
        }
    }

    fun resetRegisterResult() {
        _uiState.update { it.copy(registerResult = null) }
    }
}
