package edu.uniquindio.co.tribooo.domain.model

import java.time.Instant

/** Lo que el asistente le sugiere al moderador sobre un evento en revisión. */
enum class VerdictRecommendation {
    APROBAR,
    RECHAZAR
}

/** Respuesta del asistente ya interpretada; [recommendation] es null si no siguió el formato pedido. */
data class VerdictSuggestion(
    val recommendation: VerdictRecommendation?,
    val explanation: String
)

/**
 * Veredicto del asistente guardado para un evento (uno por evento). Solo sirve mientras el evento
 * siga teniendo el mismo contenido: [eventFingerprint] es la huella del contenido con el que se generó.
 */
data class EventVerdict(
    val eventId: String,
    val recommendation: VerdictRecommendation?,
    val explanation: String,
    val eventFingerprint: String,
    val date: Instant = Instant.now()
)
