package edu.uniquindio.co.tribooo.domain.usecase.auth

import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.ResourceProvider
import edu.uniquindio.co.tribooo.core.util.authErrorMessage
import edu.uniquindio.co.tribooo.domain.repository.AuthRepository
import jakarta.inject.Inject

class RequestPasswordRecoveryUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val resources: ResourceProvider
) {
    /**
     * Pide a Firebase Authentication que envíe a [email] el enlace para restablecer la contraseña.
     * Con la protección contra enumeración de correos activa, Firebase responde igual exista o no la cuenta.
     */
    suspend operator fun invoke(email: String): RequestResult {
        val trimmed = email.trim()
        return authResult { authRepository.sendPasswordResetEmail(trimmed) }.fold(
            onSuccess = { RequestResult.Success(resources.getString(R.string.form_recovery_sent, trimmed)) },
            onFailure = { RequestResult.Failure(authErrorMessage(resources, it)) }
        )
    }
}
