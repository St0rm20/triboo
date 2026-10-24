package edu.uniquindio.co.tribooo.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.core.util.FormTexts
import edu.uniquindio.co.tribooo.core.util.MessageTarget
import edu.uniquindio.co.tribooo.core.util.RequestResult
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

    val emailError: String? get() = Validators.email(email).takeIf { visible(LoginField.EMAIL) }
    val passwordError: String? get() = Validators.password(password).takeIf { visible(LoginField.PASSWORD) }

    val isEmailValid: Boolean get() = Validators.email(email) == null

    val firstInvalidField: LoginField?
        get() = LoginField.entries.firstOrNull {
            when (it) {
                LoginField.EMAIL -> Validators.email(email) != null
                LoginField.PASSWORD -> Validators.password(password) != null
            }
        }
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val messenger: SnackbarMessenger
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
        val invalid = state.firstInvalidField
        if (invalid != null) {
            _uiState.update {
                it.copy(submitAttempted = true, focusField = invalid, message = FormTexts.SUBMIT_INVALID)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loginResult = RequestResult.Loading) }
            val user = loginUseCase(email = state.email, password = state.password)
            val result = if (user != null) {
                messenger.post(MessageTarget.FEED, FormTexts.LOGIN_SUCCESS)
                RequestResult.Success(FormTexts.LOGIN_SUCCESS)
            } else {
                RequestResult.Failure("Email o contraseña incorrectos")
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
                messenger.post(MessageTarget.FEED, FormTexts.LOGIN_SUCCESS)
                RequestResult.Success(FormTexts.LOGIN_SUCCESS)
            } else {
                RequestResult.Failure("No se pudo iniciar sesión con Google")
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
