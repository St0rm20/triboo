package edu.uniquindio.co.tribooo.domain.usecase.user

import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class ObserveUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /** Datos vigentes de un usuario; emite null si no existe. */
    operator fun invoke(userId: String): Flow<User?> =
        userRepository.users.map { users -> users.firstOrNull { it.id == userId } }.distinctUntilChanged()
}
