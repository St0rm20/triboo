package edu.uniquindio.co.tribooo.domain.usecase.auth

import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.AuthRepository
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
import jakarta.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository
) {
    /**
     * Valida las credenciales con Firebase Authentication y guarda la sesión en el dispositivo.
     * Falla con [edu.uniquindio.co.tribooo.domain.model.AuthException] si no coinciden o no hay red.
     */
    suspend operator fun invoke(email: String, password: String): Result<User> = authResult {
        val account = authRepository.login(email.trim(), password)
        val user = userRepository.findOrCreateProfile(account)
        sessionRepository.saveSession(userId = user.id, role = user.role)
        user
    }
}
