package edu.uniquindio.co.tribooo.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.core.util.MessageTarget
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.ResourceProvider
import edu.uniquindio.co.tribooo.core.util.SnackbarMessenger
import edu.uniquindio.co.tribooo.core.util.Validators
import edu.uniquindio.co.tribooo.domain.usecase.auth.LoginUseCase
import edu.uniquindio.co.tribooo.domain.usecase.auth.LoginWithGoogleUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LoginField { EMAIL, PASSWORD }

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val touched: Set<LoginField> = emptySet(),
    val submitAttempted: Boolean = false,
    /** Campo que debe recibir el foco tras un envío inválido. */
    val focusField: LoginField? = null,
    /** Aviso de una sola vez para el snackbar. */
    val message: String? = null,
    val loginResult: RequestResult? = null
) {
    private fun visible(field: LoginField) = submitAttempted || field in touched

    fun emailError(resources: ResourceProvider): String? =
        Validators.email(resources, email).takeIf { visible(LoginField.EMAIL) }

    fun passwordError(resources: ResourceProvider): String? =
        Validators.password(resources, password).takeIf { visible(LoginField.PASSWORD) }

    val isEmailValid: Boolean get() = email.isNotBlank() && Validators.isValidEmail(email)

    fun firstInvalidField(resources: ResourceProvider): LoginField? = LoginField.entries.firstOrNull {
        when (it) {
            LoginField.EMAIL -> Validators.email(resources, email) != null
            LoginField.PASSWORD -> Validators.password(resources, password) != null
        }
    }
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val messenger: SnackbarMessenger,
    private val resources: ResourceProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** Avisos enviados a esta pantalla desde otra (por ejemplo, "Cuenta creada"). */
    val messages: Flow<String> = messenger.messages(MessageTarget.LOGIN)

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email) }

    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password) }

    fun onFieldFocusLost(field: LoginField) = _uiState.update { it.copy(touched = it.touched + field) }

    fun login() {
        val state = _uiState.value
        if (state.loginResult == RequestResult.Loading) return
        val invalid = state.firstInvalidField(resources)
        if (invalid != null) {
            _uiState.update {
                it.copy(submitAttempted = true, focusField = invalid, message = resources.getString(R.string.form_submit_invalid))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loginResult = RequestResult.Loading) }
            val user = loginUseCase(email = state.email, password = state.password)
            val result = if (user != null) {
                val success = resources.getString(R.string.form_login_success)
                messenger.post(MessageTarget.FEED, success)
                RequestResult.Success(success)
            } else {
                RequestResult.Failure(resources.getString(R.string.login_error_invalid_credentials))
            }
            _uiState.update { it.copy(loginResult = result) }
        }
    }

    /** Simulación de "Continuar con mi cuenta Google": entra con la cuenta sembrada sin pedir credenciales. */
    fun loginWithGoogle() {
        if (_uiState.value.loginResult == RequestResult.Loading) return

        viewModelScope.launch {
            _uiState.update { it.copy(loginResult = RequestResult.Loading) }
            val result = if (loginWithGoogleUseCase() != null) {
                val success = resources.getString(R.string.form_login_success)
                messenger.post(MessageTarget.FEED, success)
                RequestResult.Success(success)
            } else {
                RequestResult.Failure(resources.getString(R.string.login_error_google))
            }
            _uiState.update { it.copy(loginResult = result) }
        }
    }

    fun resetLoginResult() {
        _uiState.update { it.copy(loginResult = null) }
    }

    fun onFocusHandled() = _uiState.update { it.copy(focusField = null) }

    fun onMessageShown() = _uiState.update { it.copy(message = null) }
}
