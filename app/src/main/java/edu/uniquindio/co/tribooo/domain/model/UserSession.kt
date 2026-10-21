package edu.uniquindio.co.tribooo.domain.model

/** Datos mínimos de la sesión que se guardan en el dispositivo. */
data class UserSession(
    val userId: String,
    val role: UserRole
)
