package edu.uniquindio.co.tribooo.data.remote.notification

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import edu.uniquindio.co.tribooo.data.remote.toTimestamp
import edu.uniquindio.co.tribooo.domain.model.Notification
import edu.uniquindio.co.tribooo.domain.model.NotificationType
import edu.uniquindio.co.tribooo.domain.model.User

// Representación de una Notification tal como se guarda en Firestore (colección "notifications").
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject()
@Keep
data class NotificationDto(
    val id: String = "",
    val destinationUserId: String = "",
    val type: String = NotificationType.COMMENT.name,
    // Id del elemento al que apunta según el tipo: comentario (COMMENT), seguidor (FOLLOWER)
    // o evento (APPROVAL / REJECTION)
    val reference: String? = null,
    val read: Boolean = false,
    val date: Timestamp = Timestamp.now()
) {
    // Convierte el DTO (datos de Firestore) al modelo de dominio con el destinatario ya resuelto.
    // Retorna null si el tipo es desconocido: una notificación sin tipo no se puede mostrar
    fun toDomain(destinationUser: User): Notification? {
        val notificationType = runCatching { NotificationType.valueOf(type) }.getOrNull() ?: return null
        return Notification(
            id = id,
            destinationUser = destinationUser,
            type = notificationType,
            reference = reference,
            read = read,
            date = date.toInstant()
        )
    }

    companion object {
        const val COLLECTION = "notifications"

        // Convierte el modelo de dominio al DTO que se guardará en Firestore
        fun fromDomain(notification: Notification): NotificationDto = NotificationDto(
            id = notification.id,
            destinationUserId = notification.destinationUser.id,
            type = notification.type.name,
            reference = notification.reference,
            read = notification.read,
            date = notification.date.toTimestamp()
        )
    }
}
