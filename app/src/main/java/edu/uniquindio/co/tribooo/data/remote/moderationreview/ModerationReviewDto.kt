package edu.uniquindio.co.tribooo.data.remote.moderationreview

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import edu.uniquindio.co.tribooo.data.remote.toTimestamp
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.ModerationAction
import edu.uniquindio.co.tribooo.domain.model.ModerationReview
import edu.uniquindio.co.tribooo.domain.model.User

// Representación de una ModerationReview tal como se guarda en Firestore (colección "moderationReviews").
// Es un historial: los documentos solo se crean, nunca se editan.
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject()
@Keep
data class ModerationReviewDto(
    val id: String = "",
    val eventId: String = "",
    val adminId: String = "",
    val action: String = "",
    // Motivo escrito por el moderador (obligatorio al rechazar)
    val reason: String? = null,
    val date: Timestamp = Timestamp.now()
) {
    // Convierte el DTO (datos de Firestore) al modelo de dominio con el evento y el administrador ya resueltos.
    // Retorna null si la acción es desconocida: no hay una acción por defecto que sea segura
    fun toDomain(event: Event, admin: User): ModerationReview? {
        val moderationAction = runCatching { ModerationAction.valueOf(action) }.getOrNull() ?: return null
        return ModerationReview(
            id = id,
            event = event,
            admin = admin,
            action = moderationAction,
            reason = reason,
            date = date.toInstant()
        )
    }

    companion object {
        const val COLLECTION = "moderationReviews"

        // Convierte el modelo de dominio al DTO que se guardará en Firestore
        fun fromDomain(review: ModerationReview): ModerationReviewDto = ModerationReviewDto(
            id = review.id,
            eventId = review.event.id,
            adminId = review.admin.id,
            action = review.action.name,
            reason = review.reason,
            date = review.date.toTimestamp()
        )
    }
}
