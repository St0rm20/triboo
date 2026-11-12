package edu.uniquindio.co.tribooo.domain.usecase.moderation

import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.ModerationAction
import edu.uniquindio.co.tribooo.domain.model.ModerationCase
import edu.uniquindio.co.tribooo.domain.model.ModerationReview
import edu.uniquindio.co.tribooo.domain.model.ModerationTab
import edu.uniquindio.co.tribooo.domain.model.Report

/** Cifras de moderación; cada una cuenta publicaciones, que son las que aparecen en las pestañas. */
data class ModerationCounts(
    val pending: Int = 0,
    val rejected: Int = 0,
    val reports: Int = 0,
    val resolved: Int = 0
)

/** Publicaciones de la pestaña [tab], ordenadas como en el mockup. */
fun moderationCases(
    tab: ModerationTab,
    events: List<Event>,
    reports: List<Report>,
    reviews: List<ModerationReview>
): List<ModerationCase> {
    val openByEvent = reports.filter { !it.resolved }.groupBy { it.event.id }
    val reviewsByEvent = reviews.groupBy { it.event.id }
    fun latest(eventId: String, action: ModerationAction) =
        reviewsByEvent[eventId].orEmpty().filter { it.action == action }.maxByOrNull { it.date }

    fun caseOf(event: Event) = ModerationCase(
        event = event,
        openReports = openByEvent[event.id].orEmpty().sortedByDescending { it.date },
        rejection = if (event.status == EventStatus.RECHAZADO) latest(event.id, ModerationAction.RECHAZAR) else null,
        resolution = latest(event.id, ModerationAction.RESOLVER)
    )

    return when (tab) {
        // Cola por orden de llegada
        ModerationTab.PENDIENTES -> events.filter { it.status == EventStatus.PENDIENTE }
            .sortedBy { it.publicationDate }
            .map(::caseOf)
        ModerationTab.RECHAZADAS -> events.filter { it.status == EventStatus.RECHAZADO }
            .map(::caseOf)
            .sortedByDescending { it.rejection?.date ?: it.event.publicationDate }
        ModerationTab.REPORTES -> events.filter { it.status == EventStatus.ACTIVO && it.id in openByEvent }
            .map(::caseOf)
            .sortedByDescending { it.openReports.first().date }
        ModerationTab.RESUELTAS -> events.map(::caseOf)
            .filter { it.resolution != null }
            .sortedByDescending { it.resolution!!.date }
    }
}

fun moderationCounts(events: List<Event>, reports: List<Report>, reviews: List<ModerationReview>) = ModerationCounts(
    pending = moderationCases(ModerationTab.PENDIENTES, events, reports, reviews).size,
    rejected = moderationCases(ModerationTab.RECHAZADAS, events, reports, reviews).size,
    reports = moderationCases(ModerationTab.REPORTES, events, reports, reviews).size,
    resolved = moderationCases(ModerationTab.RESUELTAS, events, reports, reviews).size
)
