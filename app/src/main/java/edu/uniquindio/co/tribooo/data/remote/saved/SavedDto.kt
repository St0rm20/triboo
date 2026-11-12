package edu.uniquindio.co.tribooo.data.remote.saved

import androidx.annotation.Keep
import edu.uniquindio.co.tribooo.data.remote.pairDocumentId
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.Saved
import edu.uniquindio.co.tribooo.domain.model.User

// Representación de un Saved (evento guardado) tal como se guarda en Firestore
// (colección "saved", documento = "{userId}_{eventId}").
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject()
@Keep
data class SavedDto(
    val userId: String = "",
    val eventId: String = ""
) {
    // Convierte el DTO (datos de Firestore) al modelo de dominio con el usuario y el evento ya resueltos
    fun toDomain(user: User, event: Event): Saved = Saved(user = user, event = event)

    companion object {
        const val COLLECTION = "saved"

        fun documentId(userId: String, eventId: String): String = pairDocumentId(userId, eventId)

        // Convierte el modelo de dominio al DTO que se guardará en Firestore
        fun fromDomain(saved: Saved): SavedDto = SavedDto(
            userId = saved.user.id,
            eventId = saved.event.id
        )
    }
}
