package edu.uniquindio.co.tribooo.core.util

import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import java.time.Duration
import java.time.Instant

/** Horas que tarda moderación en revisar un evento. */
const val REVIEW_HOURS = 24L

/** Título de la barra superior del detalle por estado. */
fun statusScreenTitle(rp: ResourceProvider, status: EventStatus): String = rp.getString(
    when (status) {
        EventStatus.RECHAZADO -> R.string.status_screen_title_rejected
        EventStatus.PENDIENTE -> R.string.status_screen_title_pending
        else -> R.string.status_screen_title_finished
    }
)

/** Titular del banner. */
fun statusBannerTitle(rp: ResourceProvider, status: EventStatus): String = rp.getString(
    when (status) {
        EventStatus.RECHAZADO -> R.string.status_banner_title_rejected
        EventStatus.PENDIENTE -> R.string.status_banner_title_pending
        else -> R.string.status_banner_title_finished
    }
)

/** Texto del banner. */
fun statusBannerText(rp: ResourceProvider, status: EventStatus): String = rp.getString(
    when (status) {
        EventStatus.RECHAZADO -> R.string.status_banner_text_rejected
        EventStatus.PENDIENTE -> R.string.status_banner_text_pending
        else -> R.string.status_banner_text_finished
    }
)

/** Etiqueta (pill) del banner. */
fun statusPillLabel(rp: ResourceProvider, status: EventStatus): String = rp.getString(
    when (status) {
        EventStatus.RECHAZADO -> R.string.status_pill_rejected
        EventStatus.PENDIENTE -> R.string.status_pill_pending
        else -> R.string.status_pill_finished
    }
)

/** Título de la sección principal. */
fun statusSectionTitle(rp: ResourceProvider, status: EventStatus): String = rp.getString(
    when (status) {
        EventStatus.RECHAZADO -> R.string.status_section_title_rejected
        EventStatus.PENDIENTE -> R.string.status_section_title_pending
        else -> R.string.status_section_title_finished
    }
)

/** Línea de política de la tarjeta principal. */
fun statusPolicyLine(rp: ResourceProvider, status: EventStatus): String = when (status) {
    EventStatus.RECHAZADO -> rp.getString(R.string.status_policy_rejected)
    EventStatus.PENDIENTE -> rp.getString(R.string.status_policy_pending, REVIEW_HOURS)
    else -> rp.getString(R.string.status_policy_finished)
}

/** Párrafo de la tarjeta principal; en un rechazo es el [reason] de moderación. */
fun statusParagraph(rp: ResourceProvider, status: EventStatus, reason: String?): String = when (status) {
    EventStatus.RECHAZADO -> reason?.takeIf { it.isNotBlank() } ?: rp.getString(R.string.status_paragraph_no_reason)
    EventStatus.PENDIENTE -> rp.getString(R.string.status_paragraph_pending)
    else -> rp.getString(R.string.status_paragraph_finished)
}

/** Horas que quedan de la revisión de 24 h (redondeadas hacia arriba); null si ya pasaron. */
fun reviewHoursLeft(sentAt: Instant, now: Instant = Instant.now()): Int? {
    val minutesLeft = Duration.between(now, sentAt.plus(Duration.ofHours(REVIEW_HOURS))).toMinutes()
    if (minutesLeft <= 0) return null
    return ((minutesLeft + 59) / 60).toInt().coerceAtMost(REVIEW_HOURS.toInt())
}

/** "Enviado el 18 jul 2026, 14:30 · quedan ~5 h de revisión" o "… · la revisión lleva más de 24 h". */
fun pendingFooter(rp: ResourceProvider, sentAt: Instant, now: Instant = Instant.now()): String {
    val left = reviewHoursLeft(sentAt, now)
    return if (left == null) {
        rp.getString(R.string.status_pending_footer_overdue, sentAt.dayMonthYearTimeLabel(rp), REVIEW_HOURS)
    } else {
        rp.getString(R.string.status_pending_footer_left, sentAt.dayMonthYearTimeLabel(rp), left)
    }
}

/** "Revisado por Mauricio Salazar · 18 jul 2026, 14:30" */
fun rejectedFooter(rp: ResourceProvider, adminName: String?, reviewedAt: Instant?): String = when {
    adminName != null && reviewedAt != null ->
        rp.getString(R.string.status_rejected_footer, adminName, reviewedAt.dayMonthYearTimeLabel(rp))
    else -> rp.getString(R.string.status_rejected_footer_unknown)
}

/** "Cierre automático 24 h después del fin · 19 jul 2026, 14:30" */
fun finishedFooter(rp: ResourceProvider, endDate: Instant): String = rp.getString(
    R.string.status_finished_footer,
    REVIEW_HOURS,
    endDate.plus(Duration.ofHours(REVIEW_HOURS)).dayMonthYearTimeLabel(rp)
)

/** Texto de contexto temporal junto a la etiqueta del banner. */
fun statusContextLabel(
    rp: ResourceProvider,
    status: EventStatus,
    sentAt: Instant,
    endDate: Instant,
    rejectedAt: Instant?,
    now: Instant = Instant.now()
): String = when (status) {
    EventStatus.RECHAZADO ->
        rp.getString(R.string.status_context_rejected, (rejectedAt ?: sentAt).atAppZone().toLocalDate().dayMonthYearLabel(rp))
    EventStatus.PENDIENTE -> rp.getString(R.string.status_context_pending, sentAt.agoLabel(rp, now))
    else -> rp.getString(R.string.status_context_finished, endDate.atAppZone().toLocalDate().dayMonthYearLabel(rp))
}

/** Paso sugerido en la sección "Qué puedes hacer" / "Mientras esperas". */
data class StatusStep(val title: String, val description: String)

/** Título de la sección de pasos. */
fun statusStepsTitle(rp: ResourceProvider, status: EventStatus): String =
    rp.getString(if (status == EventStatus.PENDIENTE) R.string.status_steps_title_pending else R.string.status_steps_title_default)

/** Pasos del estado; en un evento finalizado, el de reseñas solo aparece si hay [reviewsCount]. */
fun statusSteps(rp: ResourceProvider, status: EventStatus, reviewsCount: Int): List<StatusStep> = when (status) {
    EventStatus.RECHAZADO -> listOf(
        StatusStep(
            rp.getString(R.string.status_step_rejected_fix_title),
            rp.getString(R.string.status_step_rejected_fix_description)
        ),
        StatusStep(
            rp.getString(R.string.status_step_rejected_resubmit_title),
            rp.getString(R.string.status_step_rejected_resubmit_description, REVIEW_HOURS)
        )
    )
    EventStatus.PENDIENTE -> listOf(
        StatusStep(
            rp.getString(R.string.status_step_pending_edit_title),
            rp.getString(R.string.status_step_pending_edit_description)
        ),
        StatusStep(
            rp.getString(R.string.status_step_pending_notify_title),
            rp.getString(R.string.status_step_pending_notify_description)
        )
    )
    else -> listOfNotNull(
        StatusStep(
            rp.getString(R.string.status_step_finished_duplicate_title),
            rp.getString(R.string.status_step_finished_duplicate_description)
        ),
        StatusStep(
            rp.getString(R.string.status_step_finished_download_title),
            rp.getString(R.string.status_step_finished_download_description)
        ),
        if (reviewsCount > 0) {
            StatusStep(
                rp.getString(R.string.status_step_finished_reviews_title),
                rp.getQuantityString(R.plurals.status_step_finished_reviews_description, reviewsCount, reviewsCount)
            )
        } else {
            null
        }
    )
}

/** Textos de la confirmación para eliminar (rechazado) o retirar (pendiente) el evento. */
data class RemoveDialogTexts(val title: String, val message: String, val confirmLabel: String, val doneMessage: String)

fun removeDialogTexts(rp: ResourceProvider, status: EventStatus): RemoveDialogTexts =
    if (status == EventStatus.PENDIENTE) {
        RemoveDialogTexts(
            title = rp.getString(R.string.status_remove_pending_title),
            message = rp.getString(R.string.status_remove_pending_message),
            confirmLabel = rp.getString(R.string.status_remove_pending_confirm),
            doneMessage = rp.getString(R.string.status_remove_pending_done)
        )
    } else {
        RemoveDialogTexts(
            title = rp.getString(R.string.status_remove_rejected_title),
            message = rp.getString(R.string.status_remove_rejected_message),
            confirmLabel = rp.getString(R.string.status_remove_rejected_confirm),
            doneMessage = rp.getString(R.string.status_remove_rejected_done)
        )
    }

/** Reseñas de un evento: comentarios con calificación y sin padre (las respuestas no cuentan). */
fun reviewsOf(eventId: String, comments: List<Comment>): List<Comment> =
    comments.filter { it.event.id == eventId && it.rating != null && it.parentComment == null }
