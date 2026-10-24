package edu.uniquindio.co.tribooo.domain.usecase.auth

import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
import jakarta.inject.Inject
import java.util.UUID

class RegisterUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /** Crea la cuenta; retorna null si ya existe una cuenta con ese email. */
    suspend operator fun invoke(
        name: String,
        city: String,
        address: String,
        email: String,
        password: String
    ): User? {
        if (userRepository.findByEmail(email.trim()) != null) return null

        val user = User(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            email = email.trim(),
            password = password,
            city = city,
            // El modelo aún no tiene dirección: se guarda como barrio
            neighborhood = address.trim()
        )
        userRepository.save(user)
        return user
    }
}
