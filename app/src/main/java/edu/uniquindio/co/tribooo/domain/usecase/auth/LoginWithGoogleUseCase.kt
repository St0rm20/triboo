package edu.uniquindio.co.tribooo.domain.usecase.auth

import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.AuthRepository
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
import jakarta.inject.Inject

/**
 * "Continuar con mi cuenta Google": recibe el ID token que entrega Credential Manager, inicia sesión en
 * Firebase Authentication y guarda la sesión igual que [LoginUseCase]. La primera vez crea el perfil.
 */
class LoginWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(idToken: String): Result<User> = authResult {
        val account = authRepository.loginWithGoogle(idToken)
        val user = userRepository.findOrCreateProfile(account)
        sessionRepository.saveSession(userId = user.id, role = user.role)
        user
    }
}
