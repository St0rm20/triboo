package edu.uniquindio.co.tribooo.core.util

import androidx.annotation.StringRes
import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.domain.model.AuthErrorReason
import edu.uniquindio.co.tribooo.domain.model.AuthException

/** Texto para el usuario de un fallo de autenticación; [fallback] cubre las causas sin mensaje propio. */
fun authErrorMessage(rp: ResourceProvider, error: Throwable, @StringRes fallback: Int = R.string.auth_error_generic): String =
    rp.getString(
        when ((error as? AuthException)?.reason) {
            AuthErrorReason.INVALID_CREDENTIALS -> R.string.login_error_invalid_credentials
            AuthErrorReason.EMAIL_ALREADY_IN_USE -> R.string.register_error_email_exists
            AuthErrorReason.WEAK_PASSWORD -> R.string.auth_error_weak_password
            AuthErrorReason.NETWORK -> R.string.auth_error_network
            AuthErrorReason.RECENT_LOGIN_REQUIRED, AuthErrorReason.UNKNOWN, null -> fallback
        }
    )
