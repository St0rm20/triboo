package edu.uniquindio.co.tribooo.core.util

import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.ModerationCase
import edu.uniquindio.co.tribooo.domain.model.ModerationReview
import edu.uniquindio.co.tribooo.domain.model.ModerationTab
import edu.uniquindio.co.tribooo.domain.model.Report
import java.time.Instant

/** Máximo de causas de reporte que se listan en una tarjeta; el resto se resume en "y N más". */
const val MAX_REPORT_LINES = 3

/** Motivos frecuentes de rechazo: al tocarlos llenan el campo de texto de la hoja. */
val REJECTION_QUICK_REASONS = listOf(
    R.string.reject_quick_commercial,
    R.string.reject_quick_incomplete_info,
    R.string.reject_quick_duplicate,
    R.string.reject_quick_conduct,
    R.string.reject_quick_false_data
)

/** Acciones que puede mostrar la tarjeta de una publicación. */
enum class ModerationCardAction { VERIFY, REJECT, RESOLVE, RESTORE }

fun ModerationTab.title(rp: ResourceProvider): String = rp.getString(
    when (this) {
        ModerationTab.PENDIENTES -> R.string.moderation_tab_pending
        ModerationTab.RECHAZADAS -> R.string.moderation_tab_rejected
        ModerationTab.REPORTES -> R.string.moderation_tab_reports
        ModerationTab.RESUELTAS -> R.string.moderation_tab_resolved
    }
)

/** "Pendientes" o, si hay casos, "Pendientes · 3". */
fun moderationTabLabel(rp: ResourceProvider, tab: ModerationTab, count: Int): String =
    if (count > 0) rp.getString(R.string.common_dot_join, tab.title(rp), count) else tab.title(rp)

/** "3 publicaciones por revisar · 2 reportes abiertos" (con singulares). */
fun moderationSummary(rp: ResourceProvider, pending: Int, reports: Int): String = rp.getString(
    R.string.common_dot_join,
    rp.getQuantityString(R.plurals.moderation_summary_to_review, pending, pending),
    rp.getQuantityString(R.plurals.moderation_summary_open_reports, reports, reports)
)

/** Texto del chip de estado de la tarjeta. */
fun moderationStatusLabel(rp: ResourceProvider, status: EventStatus): String = rp.getString(
    when (status) {
        EventStatus.PENDIENTE -> R.string.moderation_status_pending
        EventStatus.RECHAZADO -> R.string.moderation_status_rejected
        EventStatus.ACTIVO -> R.string.moderation_status_published
        EventStatus.FINALIZADO -> R.string.moderation_status_finished
    }
)

/** Línea de contexto bajo el nombre del autor, según la pestaña. */
fun moderationContext(rp: ResourceProvider, case: ModerationCase, tab: ModerationTab, now: Instant = Instant.now()): String = when {
    case.event.status == EventStatus.PENDIENTE ->
        rp.getString(R.string.moderation_context_sent, case.event.publicationDate.agoLabel(rp, now))
    tab == ModerationTab.REPORTES && case.openReports.isNotEmpty() ->
        rp.getString(R.string.moderation_context_last_report, case.openReports.maxOf { it.date }.agoLabel(rp, now))
    else -> rp.getString(R.string.moderation_context_published, case.event.publicationDate.relativeLabel(rp, now))
}

/** "Cupo 80" o "Sin límite de cupo". */
fun moderationCupoLabel(rp: ResourceProvider, cupo: Int?): String =
    if (cupo == null) rp.getString(R.string.cupo_unlimited) else rp.getString(R.string.moderation_cupo, cupo)

/** "Reportado por 4 asistentes" o, con un solo reporte, "Reportado por Camilo Arango". */
fun reportedByLabel(rp: ResourceProvider, reports: List<Report>): String =
    if (reports.size == 1) {
        rp.getString(R.string.moderation_reported_by_user, reports.first().reporter.name)
    } else {
        rp.getString(R.string.moderation_reported_by_count, reports.size)
    }

/** Hasta [MAX_REPORT_LINES] líneas "<causa> — “<detalle>”" y, si hay más, "y N más". */
fun reportLines(rp: ResourceProvider, reports: List<Report>): List<String> {
    val lines = reports.take(MAX_REPORT_LINES).map { report ->
        val detail = report.detail?.trim().orEmpty()
        val reason = reportReasonLabel(rp, report.reason)
        if (detail.isEmpty()) reason else rp.getString(R.string.moderation_report_line, reason, detail)
    }
    val extra = reports.size - MAX_REPORT_LINES
    return if (extra > 0) lines + rp.getString(R.string.moderation_report_more, extra) else lines
}

/** "Rechazado el 5 oct 2026 · Mauricio Salazar" */
fun rejectionFooter(rp: ResourceProvider, review: ModerationReview?): String =
    if (review == null) {
        rp.getString(R.string.moderation_rejection_footer_unknown)
    } else {
        rp.getString(
            R.string.moderation_rejection_footer,
            review.date.atAppZone().toLocalDate().dayMonthYearLabel(rp),
            review.admin.name
        )
    }

/** "Caso cerrado por el equipo de moderación · 5 oct 2026" */
fun resolutionFooter(rp: ResourceProvider, review: ModerationReview): String =
    rp.getString(R.string.moderation_resolution_footer, review.date.atAppZone().toLocalDate().dayMonthYearLabel(rp))

/** Botones de la tarjeta según el estado de la publicación y la pestaña. */
fun moderationActions(status: EventStatus, tab: ModerationTab): List<ModerationCardAction> = when (status) {
    EventStatus.PENDIENTE -> listOf(ModerationCardAction.VERIFY, ModerationCardAction.REJECT)
    EventStatus.ACTIVO ->
        if (tab == ModerationTab.REPORTES) listOf(ModerationCardAction.REJECT, ModerationCardAction.RESOLVE)
        else listOf(ModerationCardAction.REJECT)
    EventStatus.RECHAZADO -> listOf(ModerationCardAction.RESTORE)
    EventStatus.FINALIZADO -> emptyList()
}

/** Título y texto del estado vacío de cada pestaña. */
fun moderationEmptyTexts(rp: ResourceProvider, tab: ModerationTab): Pair<String, String> = when (tab) {
    ModerationTab.PENDIENTES ->
        rp.getString(R.string.moderation_empty_pending_title) to rp.getString(R.string.moderation_empty_pending_text)
    ModerationTab.REPORTES ->
        rp.getString(R.string.moderation_empty_reports_title) to rp.getString(R.string.moderation_empty_reports_text)
    ModerationTab.RECHAZADAS ->
        rp.getString(R.string.moderation_empty_rejected_title) to rp.getString(R.string.moderation_empty_rejected_text)
    ModerationTab.RESUELTAS ->
        rp.getString(R.string.moderation_empty_resolved_title) to rp.getString(R.string.moderation_empty_resolved_text)
}

fun verifiedMessage(rp: ResourceProvider, title: String) = rp.getString(R.string.moderation_verified_message, title)

fun rejectedMessage(rp: ResourceProvider, title: String) = rp.getString(R.string.moderation_rejected_message, title)

fun restoredMessage(rp: ResourceProvider, title: String) = rp.getString(R.string.moderation_restored_message, title)

fun resolvedMessage(rp: ResourceProvider, title: String) = rp.getString(R.string.moderation_resolved_message, title)
