package edu.uniquindio.co.tribooo.domain.usecase.auth

import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
import jakarta.inject.Inject

/** Cuenta de usuario normal sembrada (Daniela Restrepo) con la que entra el inicio de sesión simulado. */
internal const val SIMULATED_GOOGLE_USER_ID = "u-daniela"

/**
 * SIMULACIÓN para la entrega: no hay OAuth ni dependencias de Google. "Continuar con mi cuenta Google"
 * inicia sesión directamente con la cuenta sembrada, guardando la sesión igual que [LoginUseCase].
 */
class LoginWithGoogleUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(): User? {
        val user = userRepository.findById(SIMULATED_GOOGLE_USER_ID) ?: return null
        sessionRepository.saveSession(userId = user.id, role = user.role)
        return user
    }
}
