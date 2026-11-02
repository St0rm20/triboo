package edu.uniquindio.co.tribooo.features.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.core.util.MessageTarget
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.ResourceProvider
import edu.uniquindio.co.tribooo.core.util.SnackbarMessenger
import edu.uniquindio.co.tribooo.core.util.Validators
import edu.uniquindio.co.tribooo.domain.usecase.auth.RegisterUseCase
import edu.uniquindio.co.tribooo.domain.usecase.location.GetCitiesUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RegisterField { NAME, CITY, ADDRESS, EMAIL, PASSWORD, CONFIRM_PASSWORD, TERMS }

data class RegisterUiState(
    val name: String = "",
    val city: String = "",
    val address: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    val touched: Set<RegisterField> = emptySet(),
    val submitAttempted: Boolean = false,
    /** Campo que debe recibir el foco tras un envío inválido. */
    val focusField: RegisterField? = null,
    /** Aviso de una sola vez para el snackbar. */
    val message: String? = null,
    val registerResult: RequestResult? = null,
    val showConfirmDialog: Boolean = false
) {
    private fun rawError(field: RegisterField, resources: ResourceProvider): String? = when (field) {
        RegisterField.NAME -> Validators.name(resources, name)
        RegisterField.CITY -> Validators.city(resources, city)
        RegisterField.ADDRESS -> Validators.address(resources, address)
        RegisterField.EMAIL -> Validators.email(resources, email)
        RegisterField.PASSWORD -> Validators.newPassword(resources, password)
        RegisterField.CONFIRM_PASSWORD -> Validators.confirmPassword(resources, password, confirmPassword)
        RegisterField.TERMS -> Validators.terms(resources, termsAccepted)
    }

    /** El error del campo solo se ve si el usuario ya lo tocó o intentó enviar el formulario. */
    private fun shownError(field: RegisterField, resources: ResourceProvider): String? =
        rawError(field, resources).takeIf { submitAttempted || field in touched }

    fun nameError(resources: ResourceProvider): String? = shownError(RegisterField.NAME, resources)
    fun cityError(resources: ResourceProvider): String? = shownError(RegisterField.CITY, resources)
    fun addressError(resources: ResourceProvider): String? = shownError(RegisterField.ADDRESS, resources)
    fun emailError(resources: ResourceProvider): String? = shownError(RegisterField.EMAIL, resources)
    fun passwordError(resources: ResourceProvider): String? = shownError(RegisterField.PASSWORD, resources)
    fun confirmPasswordError(resources: ResourceProvider): String? = shownError(RegisterField.CONFIRM_PASSWORD, resources)
    fun termsError(resources: ResourceProvider): String? = shownError(RegisterField.TERMS, resources)

    val isEmailValid: Boolean get() = email.isNotBlank() && Validators.isValidEmail(email)

    /** Las dos contraseñas coinciden y la primera es válida. */
    fun passwordsMatch(resources: ResourceProvider): Boolean =
        confirmPassword.isNotEmpty() && rawError(RegisterField.PASSWORD, resources) == null &&
            rawError(RegisterField.CONFIRM_PASSWORD, resources) == null

    fun firstInvalidField(resources: ResourceProvider): RegisterField? =
        RegisterField.entries.firstOrNull { rawError(it, resources) != null }

    fun isFormValid(resources: ResourceProvider): Boolean = firstInvalidField(resources) == null
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    getCities: GetCitiesUseCase,
    private val registerUseCase: RegisterUseCase,
    private val messenger: SnackbarMessenger,
    private val resources: ResourceProvider
) : ViewModel() {

    val cities: List<String> = getCities()

    // La primera ciudad queda seleccionada por defecto
    private val _uiState = MutableStateFlow(RegisterUiState(city = cities.firstOrNull().orEmpty()))
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name) }

    fun onCityChange(city: String) = _uiState.update { it.copy(city = city, touched = it.touched + RegisterField.CITY) }

    fun onAddressChange(address: String) = _uiState.update { it.copy(address = address) }

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email) }

    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password) }

    fun onConfirmPasswordChange(confirmPassword: String) = _uiState.update { it.copy(confirmPassword = confirmPassword) }

    fun onTermsChange(accepted: Boolean) =
        _uiState.update { it.copy(termsAccepted = accepted, touched = it.touched + RegisterField.TERMS) }

    fun onFieldFocusLost(field: RegisterField) = _uiState.update { it.copy(touched = it.touched + field) }

    fun onRegisterClick() {
        val state = _uiState.value
        if (state.registerResult == RequestResult.Loading) return
        val invalid = state.firstInvalidField(resources)
        if (invalid != null) {
            _uiState.update {
                it.copy(submitAttempted = true, focusField = invalid, message = resources.getString(R.string.form_submit_invalid))
            }
            return
        }
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
        val state = _uiState.value
        if (state.registerResult == RequestResult.Loading) return

        viewModelScope.launch {
            _uiState.update { it.copy(registerResult = RequestResult.Loading) }
            val user = registerUseCase(
                name = state.name,
                city = state.city,
                address = state.address,
                email = state.email,
                password = state.password
            )
            val result = if (user != null) {
                messenger.post(MessageTarget.LOGIN, resources.getString(R.string.form_account_created))
                RequestResult.Success(resources.getString(R.string.register_success))
            } else {
                RequestResult.Failure(resources.getString(R.string.register_error_email_exists))
            }
            _uiState.update { it.copy(registerResult = result) }
        }
    }

    /** Deja el formulario como recién abierto: campos y errores vacíos y la primera ciudad. */
    fun resetForm() {
        _uiState.value = RegisterUiState(city = cities.firstOrNull().orEmpty())
    }

    fun resetRegisterResult() {
        _uiState.update { it.copy(registerResult = null) }
    }

    fun onFocusHandled() = _uiState.update { it.copy(focusField = null) }

    fun onMessageShown() = _uiState.update { it.copy(message = null) }
}
