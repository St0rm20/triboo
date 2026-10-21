package edu.uniquindio.co.tribooo.domain.usecase.auth

import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first

class GetCurrentUserUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository
) {
    /** Usuario con sesión iniciada en este momento (null si nadie ha iniciado sesión). */
    suspend operator fun invoke(): User? {
        val session = sessionRepository.session.first() ?: return null
        return userRepository.findById(session.userId)
    }
}
