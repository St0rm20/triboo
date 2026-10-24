package edu.uniquindio.co.tribooo.core.util

import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.ModerationCase
import edu.uniquindio.co.tribooo.domain.model.ModerationReview
import edu.uniquindio.co.tribooo.domain.model.ModerationTab
import edu.uniquindio.co.tribooo.domain.model.Report
import edu.uniquindio.co.tribooo.domain.usecase.moderation.RejectEventUseCase
import java.time.Instant

/** Máximo de causas de reporte que se listan en una tarjeta; el resto se resume en "y N más". */
const val MAX_REPORT_LINES = 3

/** Motivos frecuentes de rechazo: al tocarlos llenan el campo de texto de la hoja. */
val REJECTION_QUICK_REASONS = listOf(
    "Contenido comercial no permitido",
    "Información de contacto o lugar incompleta",
    "Duplica un evento ya publicado",
    "Incumple las normas de convivencia",
    "Datos falsos o engañosos"
)

/** Acciones que puede mostrar la tarjeta de una publicación. */
enum class ModerationCardAction { VERIFY, REJECT, RESOLVE, RESTORE }

fun ModerationTab.title(): String = when (this) {
    ModerationTab.PENDIENTES -> "Pendientes"
    ModerationTab.RECHAZADAS -> "Rechazadas"
    ModerationTab.REPORTES -> "Reportes"
    ModerationTab.RESUELTAS -> "Resueltas"
}

/** "Pendientes" o, si hay casos, "Pendientes · 3". */
fun moderationTabLabel(tab: ModerationTab, count: Int): String =
    if (count > 0) "${tab.title()} · $count" else tab.title()

/** "3 publicaciones por revisar · 2 reportes abiertos" (con singulares). */
fun moderationSummary(pending: Int, reports: Int): String {
    val toReview = if (pending == 1) "1 publicación por revisar" else "$pending publicaciones por revisar"
    val open = if (reports == 1) "1 reporte abierto" else "$reports reportes abiertos"
    return "$toReview · $open"
}

/** Texto del chip de estado de la tarjeta. */
fun moderationStatusLabel(status: EventStatus): String = when (status) {
    EventStatus.PENDIENTE -> "En revisión"
    EventStatus.RECHAZADO -> "Rechazada · fuera del feed"
    EventStatus.ACTIVO -> "Publicada"
    EventStatus.FINALIZADO -> "Finalizada"
}

/** Línea de contexto bajo el nombre del autor, según la pestaña. */
fun moderationContext(case: ModerationCase, tab: ModerationTab, now: Instant = Instant.now()): String = when {
    case.event.status == EventStatus.PENDIENTE -> "enviado ${case.event.publicationDate.agoLabel(now)}"
    tab == ModerationTab.REPORTES && case.openReports.isNotEmpty() ->
        "último reporte ${case.openReports.maxOf { it.date }.agoLabel(now)}"
    else -> "publicado ${case.event.publicationDate.relativeLabel(now)}"
}

/** "Cupo 80" o "Sin límite de cupo". */
fun moderationCupoLabel(cupo: Int?): String = if (cupo == null) "Sin límite de cupo" else "Cupo $cupo"

/** "Reportado por 4 asistentes" o, con un solo reporte, "Reportado por Camilo Arango". */
fun reportedByLabel(reports: List<Report>): String =
    if (reports.size == 1) "Reportado por ${reports.first().reporter.name}" else "Reportado por ${reports.size} asistentes"

/** Hasta [MAX_REPORT_LINES] líneas "<causa> — “<detalle>”" y, si hay más, "y N más". */
fun reportLines(reports: List<Report>): List<String> {
    val lines = reports.take(MAX_REPORT_LINES).map { report ->
        val detail = report.detail?.trim().orEmpty()
        if (detail.isEmpty()) report.reason else "${report.reason} — “$detail”"
    }
    val extra = reports.size - MAX_REPORT_LINES
    return if (extra > 0) lines + "y $extra más" else lines
}

/** "Rechazado el 5 oct 2026 · Mauricio Salazar" */
fun rejectionFooter(review: ModerationReview?): String =
    if (review == null) "Rechazado" else
        "Rechazado el ${review.date.atAppZone().toLocalDate().dayMonthYearLabel()} · ${review.admin.name}"

/** "Caso cerrado por el equipo de moderación · 5 oct 2026" */
fun resolutionFooter(review: ModerationReview): String =
    "Caso cerrado por el equipo de moderación · ${review.date.atAppZone().toLocalDate().dayMonthYearLabel()}"

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
fun moderationEmptyTexts(tab: ModerationTab): Pair<String, String> = when (tab) {
    ModerationTab.PENDIENTES ->
        "Nada por revisar" to "Todas las publicaciones enviadas ya fueron verificadas o rechazadas."
    ModerationTab.REPORTES ->
        "Sin reportes abiertos" to "No hay publicaciones reportadas por la comunidad en este momento."
    ModerationTab.RECHAZADAS ->
        "Ninguna publicación rechazada" to "Las publicaciones que rechaces aparecerán aquí con su motivo."
    ModerationTab.RESUELTAS ->
        "Sin casos resueltos" to "Marca un caso como resuelto y quedará archivado aquí."
}

/** "7/15 mínimo" */
fun reasonCounterLabel(length: Int): String = "$length/${RejectEventUseCase.MIN_REASON_LENGTH} mínimo"

fun verifiedMessage(title: String) = "“$title” fue verificada y ya aparece en el feed público."

fun rejectedMessage(title: String) = "“$title” fue rechazada, salió del feed y el autor recibió el motivo."

fun restoredMessage(title: String) = "“$title” volvió al feed público."

fun resolvedMessage(title: String) = "Caso de “$title” marcado como resuelto."
