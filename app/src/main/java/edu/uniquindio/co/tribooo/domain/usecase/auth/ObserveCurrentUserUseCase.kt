package edu.uniquindio.co.tribooo.domain.usecase.auth

import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class ObserveCurrentUserUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository
) {
    /**
     * Usuario de la sesión guardada. Emite null si no hay sesión o si el usuario ya no existe
     * (los usuarios viven en memoria: los registrados se pierden al cerrar la app).
     */
    operator fun invoke(): Flow<User?> = sessionRepository.session
        .map { session -> session?.let { userRepository.findById(it.userId) } }
        .distinctUntilChanged()
}
