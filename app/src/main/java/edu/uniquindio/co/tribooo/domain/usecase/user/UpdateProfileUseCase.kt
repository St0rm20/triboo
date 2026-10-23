package edu.uniquindio.co.tribooo.domain.usecase.user

import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
import jakarta.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /** Actualiza los datos personales editables del perfil; la biografía vacía se guarda como null. */
    suspend operator fun invoke(user: User, name: String, city: String, neighborhood: String, bio: String) {
        userRepository.update(
            user.copy(
                name = name.trim(),
                city = city,
                neighborhood = neighborhood.trim(),
                bio = bio.trim().ifEmpty { null }
            )
        )
    }
}
