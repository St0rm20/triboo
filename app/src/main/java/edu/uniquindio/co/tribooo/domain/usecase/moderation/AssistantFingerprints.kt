package edu.uniquindio.co.tribooo.domain.usecase.moderation

import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventVerdict
import edu.uniquindio.co.tribooo.domain.model.Report
import edu.uniquindio.co.tribooo.domain.model.ReportSummary
import java.security.MessageDigest

/**
 * Huella (SHA-256) del contenido del evento que evalúa el asistente. Cambia si el organizador edita
 * cualquiera de esos datos, así que sirve para saber si un veredicto guardado sigue vigente.
 */
fun eventFingerprint(event: Event): String {
    val content = listOf(
        event.title,
        event.description,
        event.category.id,
        event.startDate.toString(),
        event.endDate.toString(),
        event.place,
        event.neighborhood,
        event.cupo?.toString().orEmpty(),
        event.contribution?.amount?.toString().orEmpty(),
        event.contribution?.cause.orEmpty()
    ).joinToString(separator = "\u0000")
    return MessageDigest.getInstance("SHA-256").digest(content.toByteArray())
        .joinToString("") { "%02x".format(it) }
}

/** Huella de un grupo de reportes: sus ids ordenados. */
fun reportsFingerprint(reports: List<Report>): String = reports.map { it.id }.sorted().joinToString(",")

/** El veredicto se generó con el contenido actual del evento. */
fun EventVerdict.isCurrentFor(event: Event): Boolean = eventId == event.id && eventFingerprint == eventFingerprint(event)

/** El resumen cubre exactamente los reportes abiertos (ni le faltan nuevos ni sobran resueltos). */
fun ReportSummary.isCurrentFor(openReports: List<Report>): Boolean = reportIds == openReports.map { it.id }.toSet()
