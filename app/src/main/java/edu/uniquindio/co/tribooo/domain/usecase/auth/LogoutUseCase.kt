package edu.uniquindio.co.tribooo.domain.usecase.auth

import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import jakarta.inject.Inject

class LogoutUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    /** Cierra la sesión borrando los datos guardados en el dispositivo. */
    suspend operator fun invoke() {
        sessionRepository.clearSession()
    }
}
