package edu.uniquindio.co.tribooo.domain.model

import java.time.Instant

/** Perfil del usuario; la contraseña no forma parte del modelo: la gestiona Firebase Authentication. */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val city: String,
    val neighborhood: String,
    val bio: String? = null,
    val photo: String? = null,
    val role: UserRole = UserRole.USER,
    val dateRegistered: Instant = Instant.now()
)
