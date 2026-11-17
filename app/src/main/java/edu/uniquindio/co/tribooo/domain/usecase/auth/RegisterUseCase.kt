package edu.uniquindio.co.tribooo.domain.usecase.auth

import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.AuthRepository
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
import jakarta.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    /**
     * Crea la cuenta en Firebase Authentication (que guarda la contraseña) y el perfil con el mismo id.
     * Falla con [edu.uniquindio.co.tribooo.domain.model.AuthException] si el email ya está en uso, entre otros.
     */
    suspend operator fun invoke(
        name: String,
        city: String,
        address: String,
        email: String,
        password: String
    ): Result<User> = authResult {
        val account = authRepository.register(email.trim(), password)

        val user = User(
            id = account.uid,
            name = name.trim(),
            email = account.email.ifBlank { email.trim() },
            city = city,
            // El modelo aún no tiene dirección: se guarda como barrio
            neighborhood = address.trim()
        )
        userRepository.save(user)
        // Firebase deja la sesión abierta al crear la cuenta, pero el flujo de la app pide iniciar sesión después
        authRepository.logout()
        user
    }
}
