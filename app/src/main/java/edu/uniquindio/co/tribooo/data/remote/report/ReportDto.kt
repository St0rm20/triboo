package edu.uniquindio.co.tribooo.data.remote.report

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import edu.uniquindio.co.tribooo.data.remote.toTimestamp
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.Report
import edu.uniquindio.co.tribooo.domain.model.User

// Representación de un Report tal como se guarda en Firestore (colección "reports").
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject()
@Keep
data class ReportDto(
    val id: String = "",
    val eventId: String = "",
    val reporterId: String = "",
    // Una de las claves de ReportReasons (p. ej. "false_info"); el texto sale de los recursos
    val reason: String = "",
    val detail: String? = null,
    val date: Timestamp = Timestamp.now(),
    val resolved: Boolean = false
) {
    // Convierte el DTO (datos de Firestore) al modelo de dominio con el evento y quien reporta ya resueltos
    fun toDomain(event: Event, reporter: User): Report = Report(
        id = id,
        event = event,
        reporter = reporter,
        reason = reason,
        detail = detail,
        date = date.toInstant(),
        resolved = resolved
    )

    companion object {
        const val COLLECTION = "reports"

        // Convierte el modelo de dominio al DTO que se guardará en Firestore
        fun fromDomain(report: Report): ReportDto = ReportDto(
            id = report.id,
            eventId = report.event.id,
            reporterId = report.reporter.id,
            reason = report.reason,
            detail = report.detail,
            date = report.date.toTimestamp(),
            resolved = report.resolved
        )
    }
}
