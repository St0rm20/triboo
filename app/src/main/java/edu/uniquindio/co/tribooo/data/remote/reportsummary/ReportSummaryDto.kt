package edu.uniquindio.co.tribooo.data.remote.reportsummary

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import edu.uniquindio.co.tribooo.data.remote.toTimestamp
import edu.uniquindio.co.tribooo.domain.model.ReportSummary

// Representación de un ReportSummary tal como se guarda en Firestore (colección "reportSummaries").
// El id del documento es el id del evento: hay un solo resumen por evento y uno nuevo reemplaza al anterior.
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject()
@Keep
data class ReportSummaryDto(
    val eventId: String = "",
    val text: String = "",
    // Reportes que cubre el resumen; si los reportes abiertos ya no son estos, el resumen está desactualizado
    val reportIds: List<String> = emptyList(),
    val date: Timestamp = Timestamp.now()
) {
    // Convierte el DTO (datos de Firestore) al modelo de dominio
    fun toDomain(): ReportSummary = ReportSummary(
        eventId = eventId,
        text = text,
        reportIds = reportIds.toSet(),
        date = date.toInstant()
    )

    companion object {
        const val COLLECTION = "reportSummaries"

        // Convierte el modelo de dominio al DTO que se guardará en Firestore
        fun fromDomain(summary: ReportSummary): ReportSummaryDto = ReportSummaryDto(
            eventId = summary.eventId,
            text = summary.text,
            reportIds = summary.reportIds.sorted(),
            date = summary.date.toTimestamp()
        )
    }
}
