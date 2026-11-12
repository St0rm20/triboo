package edu.uniquindio.co.tribooo.core.util

import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.ModerationAction
import edu.uniquindio.co.tribooo.domain.model.ModerationReview
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale

/** Promedio de las calificaciones (estrellas) de los comentarios del evento; null si no hay. */
fun averageRating(eventId: String, comments: List<Comment>): Double? =
    comments.filter { it.event.id == eventId }.mapNotNull { it.rating }.takeIf { it.isNotEmpty() }?.average()

/** "Activo", "En revisión", "Finalizado" o "Rechazado" */
fun EventStatus.myEventsLabel(rp: ResourceProvider): String = rp.getString(
    when (this) {
        EventStatus.ACTIVO -> R.string.my_events_status_active
        EventStatus.PENDIENTE -> R.string.my_events_status_pending
        EventStatus.FINALIZADO -> R.string.my_events_status_finished
        EventStatus.RECHAZADO -> R.string.my_events_status_rejected
    }
)

/**
 * Línea de resumen de la tarjeta según el estado: "87 de 120 confirmados", "Esperando moderación",
 * "34 asistentes · calificación 4.8" o "No se publicó en el feed".
 */
fun myEventSummary(rp: ResourceProvider, event: Event, attendees: Int, rating: Double?): String = when (event.status) {
    EventStatus.ACTIVO ->
        event.cupo?.let { rp.getQuantityString(R.plurals.cupo_confirmed_of, attendees, attendees, it) }
            ?: rp.getQuantityString(R.plurals.my_events_confirmed, attendees, attendees)
    EventStatus.PENDIENTE -> rp.getString(R.string.my_events_summary_pending)
    EventStatus.FINALIZADO -> {
        val people = rp.getQuantityString(R.plurals.common_attendees_count, attendees, attendees)
        if (rating == null) {
            people
        } else {
            rp.getString(R.string.my_events_summary_rated, people, String.format(Locale.US, "%.1f", rating))
        }
    }
    EventStatus.RECHAZADO -> rp.getString(R.string.my_events_summary_rejected)
}

/** Fecha de la revisión más reciente que rechazó el evento; null si moderación no registró ninguna. */
fun latestRejectionDate(eventId: String, reviews: List<ModerationReview>): Instant? =
    reviews.filter { it.event.id == eventId && it.action == ModerationAction.RECHAZAR }.maxOfOrNull { it.date }

/**
 * Texto de contexto temporal del pie de la tarjeta: "publicado hace 3 días", "Enviado hace 2 h",
 * "Finalizado el 18 jul 2026" o "Rechazado el 5 oct 2026". [rejectedAt] es la fecha de la revisión que rechazó
 * el evento; sin ella se usa la de publicación.
 */
fun myEventFooter(rp: ResourceProvider, event: Event, now: Instant = Instant.now(), rejectedAt: Instant? = null): String =
    when (event.status) {
        EventStatus.ACTIVO -> rp.getString(R.string.my_events_footer_published, daysAgoLabel(rp, event.publicationDate, now))
        EventStatus.PENDIENTE -> rp.getString(R.string.my_events_footer_sent, hoursOrDaysAgoLabel(rp, event.publicationDate, now))
        EventStatus.FINALIZADO ->
            rp.getString(R.string.my_events_footer_finished, event.endDate.atAppZone().toLocalDate().dayMonthYearLabel(rp))
        EventStatus.RECHAZADO ->
            rp.getString(
                R.string.my_events_footer_rejected,
                (rejectedAt ?: event.publicationDate).atAppZone().toLocalDate().dayMonthYearLabel(rp)
            )
    }

/** "hoy", "hace 1 día" o "hace 3 días" (por día calendario). */
private fun daysAgoLabel(rp: ResourceProvider, from: Instant, now: Instant): String {
    val days = ChronoUnit.DAYS.between(from.atAppZone().toLocalDate(), now.atAppZone().toLocalDate())
    return if (days <= 0) rp.getString(R.string.time_today) else rp.getQuantityString(R.plurals.time_ago_days, days.toInt(), days)
}

/** "hace un momento", "hace 5 h" o "hace 2 días". */
private fun hoursOrDaysAgoLabel(rp: ResourceProvider, from: Instant, now: Instant): String {
    val hours = Duration.between(from, now).toHours()
    return when {
        hours < 1 -> rp.getString(R.string.time_moment_ago)
        hours < 24 -> rp.getString(R.string.time_ago_hours, hours)
        else -> daysAgoLabel(rp, from, now)
    }
}
