package edu.uniquindio.co.tribooo.data.remote.attendance

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import edu.uniquindio.co.tribooo.data.remote.pairDocumentId
import edu.uniquindio.co.tribooo.data.remote.toTimestamp
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.User

// Representación de una Attendance tal como se guarda en Firestore
// (colección "attendances", documento = "{userId}_{eventId}" para que no haya asistencias repetidas).
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject()
@Keep
data class AttendanceDto(
    val userId: String = "",
    val eventId: String = "",
    val confirmationDate: Timestamp = Timestamp.now()
) {
    // Convierte el DTO (datos de Firestore) al modelo de dominio con el usuario y el evento ya resueltos
    fun toDomain(user: User, event: Event): Attendance = Attendance(
        user = user,
        event = event,
        confirmationDate = confirmationDate.toInstant()
    )

    companion object {
        const val COLLECTION = "attendances"

        fun documentId(userId: String, eventId: String): String = pairDocumentId(userId, eventId)

        // Convierte el modelo de dominio al DTO que se guardará en Firestore
        fun fromDomain(attendance: Attendance): AttendanceDto = AttendanceDto(
            userId = attendance.user.id,
            eventId = attendance.event.id,
            confirmationDate = attendance.confirmationDate.toTimestamp()
        )
    }
}
