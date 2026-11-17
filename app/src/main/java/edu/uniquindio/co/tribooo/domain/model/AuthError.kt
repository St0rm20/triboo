package edu.uniquindio.co.tribooo.domain.model

/** Causas de fallo de la autenticación, independientes del proveedor. */
enum class AuthErrorReason {
    INVALID_CREDENTIALS,
    EMAIL_ALREADY_IN_USE,
    WEAK_PASSWORD,
    RECENT_LOGIN_REQUIRED,
    NETWORK,
    UNKNOWN
}

/** Error que lanza el [edu.uniquindio.co.tribooo.domain.repository.AuthRepository]; el dominio no conoce las excepciones de Firebase. */
class AuthException(val reason: AuthErrorReason, cause: Throwable? = null) : Exception(reason.name, cause)
