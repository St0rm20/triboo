package edu.uniquindio.co.tribooo.core.util

import androidx.annotation.StringRes
import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.domain.model.AuthErrorReason
import edu.uniquindio.co.tribooo.domain.model.AuthException
import edu.uniquindio.co.tribooo.domain.model.DataErrorReason
import edu.uniquindio.co.tribooo.domain.model.DataException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Texto para el usuario de un fallo en una petición en línea (red, permisos, datos que ya no existen).
 * [fallback] cubre los fallos sin mensaje propio.
 */
fun requestErrorMessage(rp: ResourceProvider, error: Throwable, @StringRes fallback: Int = R.string.error_generic): String =
    rp.getString(
        when {
            error is DataException -> when (error.reason) {
                DataErrorReason.NETWORK -> R.string.error_network
                DataErrorReason.PERMISSION_DENIED -> R.string.error_permission
                DataErrorReason.NOT_FOUND -> R.string.error_not_found
                DataErrorReason.UNKNOWN -> fallback
            }
            error is AuthException && error.reason == AuthErrorReason.NETWORK -> R.string.error_network
            else -> fallback
        }
    )

/**
 * Ejecuta una petición en línea: [onSuccess] traduce su resultado y cualquier excepción se vuelve un
 * [RequestResult.Failure] con el mensaje genérico que corresponda. La cancelación de la corrutina se propaga.
 */
suspend fun <T> runRequest(
    resources: ResourceProvider,
    @StringRes fallback: Int = R.string.error_generic,
    onSuccess: (T) -> RequestResult,
    block: suspend () -> T
): RequestResult = try {
    onSuccess(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    RequestResult.Failure(requestErrorMessage(resources, e, fallback))
}

/** Mensaje que se muestra al terminar la petición; null mientras sigue en curso. */
fun RequestResult.messageOrNull(): String? = when (this) {
    is RequestResult.Success -> message.ifEmpty { null }
    is RequestResult.Failure -> errorMessage
    RequestResult.Loading -> null
}
