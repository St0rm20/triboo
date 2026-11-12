package edu.uniquindio.co.tribooo.data.remote.waitlist

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import edu.uniquindio.co.tribooo.data.remote.pairDocumentId
import edu.uniquindio.co.tribooo.data.remote.toTimestamp
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.model.WaitlistEntry

// Representación de una WaitlistEntry tal como se guarda en Firestore
// (colección "waitlist", documento = "{userId}_{eventId}"). El orden de la fila sale de joinedAt.
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject()
@Keep
data class WaitlistEntryDto(
    val userId: String = "",
    val eventId: String = "",
    val joinedAt: Timestamp = Timestamp.now()
) {
    // Convierte el DTO (datos de Firestore) al modelo de dominio con el usuario y el evento ya resueltos
    fun toDomain(user: User, event: Event): WaitlistEntry = WaitlistEntry(
        user = user,
        event = event,
        joinedAt = joinedAt.toInstant()
    )

    companion object {
        const val COLLECTION = "waitlist"

        fun documentId(userId: String, eventId: String): String = pairDocumentId(userId, eventId)

        // Convierte el modelo de dominio al DTO que se guardará en Firestore
        fun fromDomain(entry: WaitlistEntry): WaitlistEntryDto = WaitlistEntryDto(
            userId = entry.user.id,
            eventId = entry.event.id,
            joinedAt = entry.joinedAt.toTimestamp()
        )
    }
}
