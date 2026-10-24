package edu.uniquindio.co.tribooo.core.util

import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import java.time.Duration
import java.time.Instant

/** Horas que tarda moderación en revisar un evento. */
const val REVIEW_HOURS = 24L

/** Título de la barra superior del detalle por estado. */
fun statusScreenTitle(status: EventStatus): String = when (status) {
    EventStatus.RECHAZADO -> "Evento rechazado"
    EventStatus.PENDIENTE -> "Evento en revisión"
    else -> "Evento finalizado"
}

/** Titular del banner. */
fun statusBannerTitle(status: EventStatus): String = when (status) {
    EventStatus.RECHAZADO -> "Este evento fue rechazado"
    EventStatus.PENDIENTE -> "Este evento está en revisión"
    else -> "Este evento ya finalizó"
}

/** Texto del banner. */
fun statusBannerText(status: EventStatus): String = when (status) {
    EventStatus.RECHAZADO ->
        "No se publicó en el feed y nadie pudo confirmar asistencia. Puedes corregirlo y enviarlo de nuevo."
    EventStatus.PENDIENTE ->
        "Moderación lo está revisando. Todavía no es visible en el feed ni en el mapa."
    else -> "Quedó archivado en tu historial: no acepta nuevas confirmaciones ni aparece en el feed."
}

/** Etiqueta (pill) del banner. */
fun statusPillLabel(status: EventStatus): String = when (status) {
    EventStatus.RECHAZADO -> "No visible para la comunidad"
    EventStatus.PENDIENTE -> "Pendiente de aprobación"
    else -> "Archivado"
}

/** Título de la sección principal. */
fun statusSectionTitle(status: EventStatus): String = when (status) {
    EventStatus.RECHAZADO -> "Motivo del rechazo"
    EventStatus.PENDIENTE -> "Estado de la revisión"
    else -> "Cierre del evento"
}

/** Línea de política de la tarjeta principal. */
fun statusPolicyLine(status: EventStatus): String = when (status) {
    EventStatus.RECHAZADO -> "Rechazado por moderación"
    EventStatus.PENDIENTE -> "Revisión · respuesta en menos de $REVIEW_HOURS h"
    else -> "Evento cerrado · archivado automáticamente"
}

/** Párrafo de la tarjeta principal; en un rechazo es el [reason] de moderación. */
fun statusParagraph(status: EventStatus, reason: String?): String = when (status) {
    EventStatus.RECHAZADO -> reason?.takeIf { it.isNotBlank() } ?: "Moderación no registró un motivo."
    EventStatus.PENDIENTE ->
        "Moderación está verificando la información del evento. Mientras la revisión siga abierta, el evento no " +
            "aparece en el feed ni en el mapa, y nadie puede confirmar asistencia."
    else ->
        "El evento ya ocurrió: no acepta nuevas confirmaciones ni comentarios, y salió del feed. Queda en tu " +
            "historial como organizador y suma a tu reputación en la comunidad."
}

/** Horas que quedan de la revisión de 24 h (redondeadas hacia arriba); null si ya pasaron. */
fun reviewHoursLeft(sentAt: Instant, now: Instant = Instant.now()): Int? {
    val minutesLeft = Duration.between(now, sentAt.plus(Duration.ofHours(REVIEW_HOURS))).toMinutes()
    if (minutesLeft <= 0) return null
    return ((minutesLeft + 59) / 60).toInt().coerceAtMost(REVIEW_HOURS.toInt())
}

/** "Enviado el 18 jul 2026, 14:30 · quedan ~5 h de revisión" o "… · la revisión lleva más de 24 h". */
fun pendingFooter(sentAt: Instant, now: Instant = Instant.now()): String {
    val left = reviewHoursLeft(sentAt, now)
    val tail = if (left == null) "la revisión lleva más de $REVIEW_HOURS h" else "quedan ~$left h de revisión"
    return "Enviado el ${sentAt.dayMonthYearTimeLabel()} · $tail"
}

/** "Revisado por Mauricio Salazar · 18 jul 2026, 14:30" */
fun rejectedFooter(adminName: String?, reviewedAt: Instant?): String = when {
    adminName != null && reviewedAt != null -> "Revisado por $adminName · ${reviewedAt.dayMonthYearTimeLabel()}"
    else -> "Sin datos de la revisión"
}

/** "Cierre automático 24 h después del fin · 19 jul 2026, 14:30" */
fun finishedFooter(endDate: Instant): String =
    "Cierre automático $REVIEW_HOURS h después del fin · ${endDate.plus(Duration.ofHours(REVIEW_HOURS)).dayMonthYearTimeLabel()}"

/** Texto de contexto temporal junto a la etiqueta del banner. */
fun statusContextLabel(status: EventStatus, sentAt: Instant, endDate: Instant, rejectedAt: Instant?, now: Instant = Instant.now()): String =
    when (status) {
        EventStatus.RECHAZADO ->
            "Rechazado el ${(rejectedAt ?: sentAt).atAppZone().toLocalDate().dayMonthYearLabel()}"
        EventStatus.PENDIENTE -> "Enviado ${sentAt.agoLabel(now)}"
        else -> "Finalizado el ${endDate.atAppZone().toLocalDate().dayMonthYearLabel()}"
    }

/** Paso sugerido en la sección "Qué puedes hacer" / "Mientras esperas". */
data class StatusStep(val title: String, val description: String)

/** Título de la sección de pasos. */
fun statusStepsTitle(status: EventStatus): String =
    if (status == EventStatus.PENDIENTE) "Mientras esperas" else "Qué puedes hacer"

/** Pasos del estado; en un evento finalizado, el de reseñas solo aparece si hay [reviewsCount]. */
fun statusSteps(status: EventStatus, reviewsCount: Int): List<StatusStep> = when (status) {
    EventStatus.RECHAZADO -> listOf(
        StatusStep(
            "Corrige lo señalado por moderación",
            "Ajusta el evento según el motivo del rechazo; no pierdes el contenido ya escrito."
        ),
        StatusStep("Reenvía el evento a moderación", "Una nueva revisión tarda menos de $REVIEW_HOURS h.")
    )
    EventStatus.PENDIENTE -> listOf(
        StatusStep(
            "Puedes seguir editando el borrador",
            "Los cambios se guardan y entran a la misma revisión, sin volver a la cola."
        ),
        StatusStep(
            "Te avisamos al aprobarse",
            "Recibirás una notificación y el evento se publicará automáticamente."
        )
    )
    else -> listOfNotNull(
        StatusStep(
            "Duplícalo para una nueva fecha",
            "Se copian descripción, cupo y ubicación; solo cambias las fechas."
        ),
        StatusStep(
            "Descarga la lista de asistencia",
            "Incluye confirmados, asistentes reales y calificaciones recibidas."
        ),
        if (reviewsCount > 0) {
            StatusStep("Responde las reseñas", "Contesta arriba las $reviewsCount reseñas que dejaron los asistentes.")
        } else {
            null
        }
    )
}

/** Textos de la confirmación para eliminar (rechazado) o retirar (pendiente) el evento. */
data class RemoveDialogTexts(val title: String, val message: String, val confirmLabel: String, val doneMessage: String)

fun removeDialogTexts(status: EventStatus): RemoveDialogTexts =
    if (status == EventStatus.PENDIENTE) {
        RemoveDialogTexts(
            title = "¿Retirar el envío?",
            message = "El evento saldrá de la cola de revisión y se eliminará. No se puede deshacer.",
            confirmLabel = "Retirar",
            doneMessage = "Envío retirado de revisión."
        )
    } else {
        RemoveDialogTexts(
            title = "¿Eliminar el evento?",
            message = "Se eliminará el evento rechazado y su contenido. No se puede deshacer.",
            confirmLabel = "Eliminar",
            doneMessage = "Evento rechazado eliminado."
        )
    }

/** Reseñas de un evento: comentarios con calificación y sin padre (las respuestas no cuentan). */
fun reviewsOf(eventId: String, comments: List<Comment>): List<Comment> =
    comments.filter { it.event.id == eventId && it.rating != null && it.parentComment == null }
