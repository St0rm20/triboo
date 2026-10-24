package edu.uniquindio.co.tribooo.core.util

import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale

/** Promedio de las calificaciones (estrellas) de los comentarios del evento; null si no hay. */
fun averageRating(eventId: String, comments: List<Comment>): Double? =
    comments.filter { it.event.id == eventId }.mapNotNull { it.rating }.takeIf { it.isNotEmpty() }?.average()

/** "Activo", "En revisión", "Finalizado" o "Rechazado" */
fun EventStatus.myEventsLabel(): String = when (this) {
    EventStatus.ACTIVO -> "Activo"
    EventStatus.PENDIENTE -> "En revisión"
    EventStatus.FINALIZADO -> "Finalizado"
    EventStatus.RECHAZADO -> "Rechazado"
}

/**
 * Línea de resumen de la tarjeta según el estado: "87 de 120 confirmados", "Esperando moderación",
 * "34 asistentes · calificación 4.8" o "No se publicó en el feed".
 */
fun myEventSummary(event: Event, attendees: Int, rating: Double?): String = when (event.status) {
    EventStatus.ACTIVO -> event.cupo?.let { "$attendees de $it confirmados" } ?: "$attendees confirmados"
    EventStatus.PENDIENTE -> "Esperando moderación"
    EventStatus.FINALIZADO -> {
        val people = if (attendees == 1) "1 asistente" else "$attendees asistentes"
        if (rating == null) people else "$people · calificación ${String.format(Locale.US, "%.1f", rating)}"
    }
    EventStatus.RECHAZADO -> "No se publicó en el feed"
}

/**
 * Texto de contexto temporal del pie de la tarjeta: "publicado hace 3 días", "Enviado hace 2 h",
 * "Finalizado el 18 jul 2026" o "Rechazado el 5 oct 2026".
 */
fun myEventFooter(event: Event, now: Instant = Instant.now()): String = when (event.status) {
    EventStatus.ACTIVO -> "publicado ${daysAgoLabel(event.publicationDate, now)}"
    EventStatus.PENDIENTE -> "Enviado ${hoursOrDaysAgoLabel(event.publicationDate, now)}"
    EventStatus.FINALIZADO -> "Finalizado el ${event.endDate.atAppZone().toLocalDate().dayMonthYearLabel()}"
    // Aún no hay repositorio de moderación: se usa la fecha de publicación
    EventStatus.RECHAZADO -> "Rechazado el ${event.publicationDate.atAppZone().toLocalDate().dayMonthYearLabel()}"
}

/** "hoy", "hace 1 día" o "hace 3 días" (por día calendario). */
private fun daysAgoLabel(from: Instant, now: Instant): String {
    val days = ChronoUnit.DAYS.between(from.atAppZone().toLocalDate(), now.atAppZone().toLocalDate())
    return when {
        days <= 0 -> "hoy"
        days == 1L -> "hace 1 día"
        else -> "hace $days días"
    }
}

/** "hace un momento", "hace 5 h" o "hace 2 días". */
private fun hoursOrDaysAgoLabel(from: Instant, now: Instant): String {
    val hours = Duration.between(from, now).toHours()
    return when {
        hours < 1 -> "hace un momento"
        hours < 24 -> "hace $hours h"
        else -> daysAgoLabel(from, now)
    }
}
