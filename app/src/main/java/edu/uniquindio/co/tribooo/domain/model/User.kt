package edu.uniquindio.co.tribooo.domain.model

data class User(
    val id: String,
    val name: String,
    val city: String,
    val address: String,
    val email: String,
    val password: String,
    val role: UserRole = UserRole.USER
)
