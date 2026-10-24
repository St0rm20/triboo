package edu.uniquindio.co.tribooo.domain.usecase.auth

import edu.uniquindio.co.tribooo.core.util.FormTexts
import edu.uniquindio.co.tribooo.core.util.RequestResult
import jakarta.inject.Inject
import kotlinx.coroutines.delay

class RequestPasswordRecoveryUseCase @Inject constructor() {
    /**
     * Pide el código de recuperación para [email]. Mientras no hay servidor lo simula, como el resto
     * de la app: tarda un instante y siempre responde que el código fue enviado.
     */
    suspend operator fun invoke(email: String): RequestResult {
        delay(SIMULATED_SEND_MILLIS)
        return RequestResult.Success(FormTexts.recoverySent(email))
    }

    private companion object {
        const val SIMULATED_SEND_MILLIS = 900L
    }
}
