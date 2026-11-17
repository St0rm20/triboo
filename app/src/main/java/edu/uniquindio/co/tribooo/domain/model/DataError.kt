package edu.uniquindio.co.tribooo.domain.model

/** Causas de fallo al leer o escribir datos remotos, independientes de la base de datos que se use. */
enum class DataErrorReason {
    NETWORK,
    PERMISSION_DENIED,
    NOT_FOUND,
    /** Se superó el límite de peticiones del servicio (p. ej. los modelos gratuitos de OpenRouter). */
    RATE_LIMITED,
    UNKNOWN
}

/** Error que lanzan los repositorios remotos; el dominio no conoce las excepciones de Firestore. */
class DataException(val reason: DataErrorReason, cause: Throwable? = null) : Exception(reason.name, cause)
