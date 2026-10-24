package edu.uniquindio.co.tribooo.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.core.util.FormTexts
import edu.uniquindio.co.tribooo.core.util.MessageTarget
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.SnackbarMessenger
import edu.uniquindio.co.tribooo.core.util.Validators
import edu.uniquindio.co.tribooo.domain.usecase.auth.RequestPasswordRecoveryUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PasswordRecoveryUiState(
    val email: String = "",
    val emailTouched: Boolean = false,
    val submitAttempted: Boolean = false,
    val focusEmail: Boolean = false,
    val isSending: Boolean = false,
    /** Correo al que se envió el código; null mientras el formulario sigue abierto. */
    val sentTo: String? = null,
    /** Aviso de una sola vez para el snackbar. */
    val message: String? = null
) {
    val emailError: String? get() = Validators.email(email).takeIf { submitAttempted || emailTouched }

    val isEmailValid: Boolean get() = Validators.email(email) == null
}

@HiltViewModel
class PasswordRecoveryViewModel @Inject constructor(
    private val requestRecovery: RequestPasswordRecoveryUseCase,
    private val messenger: SnackbarMessenger
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordRecoveryUiState())
    val uiState: StateFlow<PasswordRecoveryUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email) }

    fun onEmailFocusLost() = _uiState.update { it.copy(emailTouched = true) }

    fun onSend() {
        val state = _uiState.value
        if (state.isSending) return
        if (!state.isEmailValid) {
            _uiState.update { it.copy(submitAttempted = true, focusEmail = true, message = FormTexts.SUBMIT_INVALID) }
            return
        }

        _uiState.update { it.copy(isSending = true) }
        viewModelScope.launch {
            val result = requestRecovery(state.email)
            _uiState.update {
                when (result) {
                    is RequestResult.Success -> it.copy(isSending = false, sentTo = state.email, message = result.message)
                    is RequestResult.Failure -> it.copy(isSending = false, message = result.errorMessage)
                    RequestResult.Loading -> it
                }
            }
        }
    }

    /** Vuelve al formulario con el correo escrito para corregirlo. */
    fun onUseAnotherEmail() = _uiState.update { it.copy(sentTo = null, submitAttempted = false, emailTouched = false) }

    /** Al volver al inicio de sesión se recuerda a qué correo se envió el código. */
    fun onBackToLogin() {
        _uiState.value.sentTo?.let { messenger.post(MessageTarget.LOGIN, FormTexts.recoveryReminder(it)) }
    }

    fun onFocusHandled() = _uiState.update { it.copy(focusEmail = false) }

    fun onMessageShown() = _uiState.update { it.copy(message = null) }
}
