package edu.uniquindio.co.tribooo.data.remote.eventverdict

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import edu.uniquindio.co.tribooo.data.remote.toTimestamp
import edu.uniquindio.co.tribooo.domain.model.EventVerdict
import edu.uniquindio.co.tribooo.domain.model.VerdictRecommendation

// Representación de un EventVerdict tal como se guarda en Firestore (colección "eventVerdicts").
// El id del documento es el id del evento: hay un solo veredicto por evento y uno nuevo reemplaza al anterior.
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject()
@Keep
data class EventVerdictDto(
    val eventId: String = "",
    // "APROBAR", "RECHAZAR" o null si el modelo no siguió el formato pedido
    val recommendation: String? = null,
    val explanation: String = "",
    // SHA-256 del contenido del evento evaluado; si no coincide con el actual, el veredicto está desactualizado
    val eventFingerprint: String = "",
    val date: Timestamp = Timestamp.now()
) {
    // Convierte el DTO (datos de Firestore) al modelo de dominio; una recomendación desconocida queda en null
    fun toDomain(): EventVerdict = EventVerdict(
        eventId = eventId,
        recommendation = recommendation?.let { runCatching { VerdictRecommendation.valueOf(it) }.getOrNull() },
        explanation = explanation,
        eventFingerprint = eventFingerprint,
        date = date.toInstant()
    )

    companion object {
        const val COLLECTION = "eventVerdicts"

        // Convierte el modelo de dominio al DTO que se guardará en Firestore
        fun fromDomain(verdict: EventVerdict): EventVerdictDto = EventVerdictDto(
            eventId = verdict.eventId,
            recommendation = verdict.recommendation?.name,
            explanation = verdict.explanation,
            eventFingerprint = verdict.eventFingerprint,
            date = verdict.date.toTimestamp()
        )
    }
}
