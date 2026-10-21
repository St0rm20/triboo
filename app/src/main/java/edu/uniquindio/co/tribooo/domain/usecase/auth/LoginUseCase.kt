package edu.uniquindio.co.tribooo.domain.usecase.auth

import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
import jakarta.inject.Inject

class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository
) {
    /** Inicia sesión y la guarda en el dispositivo si las credenciales coinciden; retorna null si no. */
    suspend operator fun invoke(email: String, password: String): User? {
        val user = userRepository.login(email.trim(), password) ?: return null
        sessionRepository.saveSession(userId = user.id, role = user.role)
        return user
    }
}
