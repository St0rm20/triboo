package edu.uniquindio.co.tribooo.domain.model

/** Cuenta autenticada por el proveedor de identidad (Firebase Authentication); [uid] es también el id del [User]. */
data class AuthAccount(
    val uid: String,
    val email: String,
    val displayName: String? = null,
    val photoUrl: String? = null
)
