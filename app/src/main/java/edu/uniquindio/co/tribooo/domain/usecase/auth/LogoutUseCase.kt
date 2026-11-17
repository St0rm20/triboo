package edu.uniquindio.co.tribooo.domain.usecase.auth

import edu.uniquindio.co.tribooo.domain.repository.AuthRepository
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import jakarta.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) {
    /** Cierra la sesión en Firebase Authentication y borra los datos guardados en el dispositivo. */
    suspend operator fun invoke() {
        authRepository.logout()
        sessionRepository.clearSession()
    }
}
