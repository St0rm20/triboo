package edu.uniquindio.co.tribooo.data.remote.interest

import androidx.annotation.Keep
import edu.uniquindio.co.tribooo.data.remote.pairDocumentId
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.Interest
import edu.uniquindio.co.tribooo.domain.model.User

// Representación de un Interest ("me interesa") tal como se guarda en Firestore
// (colección "interests", documento = "{userId}_{eventId}").
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject()
@Keep
data class InterestDto(
    val userId: String = "",
    val eventId: String = ""
) {
    // Convierte el DTO (datos de Firestore) al modelo de dominio con el usuario y el evento ya resueltos
    fun toDomain(user: User, event: Event): Interest = Interest(user = user, event = event)

    companion object {
        const val COLLECTION = "interests"

        fun documentId(userId: String, eventId: String): String = pairDocumentId(userId, eventId)

        // Convierte el modelo de dominio al DTO que se guardará en Firestore
        fun fromDomain(interest: Interest): InterestDto = InterestDto(
            userId = interest.user.id,
            eventId = interest.event.id
        )
    }
}
