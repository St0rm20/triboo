package edu.uniquindio.co.tribooo.core.util

import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.ModerationCase
import edu.uniquindio.co.tribooo.domain.model.ModerationTab
import edu.uniquindio.co.tribooo.domain.model.Report
import edu.uniquindio.co.tribooo.domain.model.ReportReasons
import edu.uniquindio.co.tribooo.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class ModerationLabelsTest {

    private val user = User("u", "Camilo Arango", "c@mail.co", "123456", "Armenia", "Centro")
    private val event = Event(
        id = "e", title = "Evento", description = "", startDate = Instant.now(), endDate = Instant.now(),
        place = "", neighborhood = "", location = Location(0.0, 0.0), organizer = user, category = Category.SOCIAL
    )

    private fun report(reason: String, detail: String? = null) = Report("r", event, user, reason, detail)

    @Test
    fun summaryUsesSingularAndPlural() {
        assertEquals("1 publicación por revisar · 1 reporte abierto", moderationSummary(1, 1))
        assertEquals("3 publicaciones por revisar · 0 reportes abiertos", moderationSummary(3, 0))
    }

    @Test
    fun tabLabelShowsCountOnlyIfPositive() {
        assertEquals("Pendientes · 4", moderationTabLabel(ModerationTab.PENDIENTES, 4))
        assertEquals("Resueltas", moderationTabLabel(ModerationTab.RESUELTAS, 0))
    }

    @Test
    fun reportedByNamesTheOnlyReporterOrCounts() {
        assertEquals("Reportado por Camilo Arango", reportedByLabel(listOf(report(ReportReasons.FALSE_INFO))))
        assertEquals("Reportado por 2 asistentes", reportedByLabel(listOf(report("a"), report("b"))))
    }

    @Test
    fun reportLinesShowDetailAndSummarizeTheRest() {
        val reports = listOf(
            report(ReportReasons.FALSE_INFO, "No era así"),
            report(ReportReasons.SPAM_OR_DUPLICATE),
            report(ReportReasons.CHARGES_OR_ADS, "  "),
            report("cuarta"),
            report("quinta")
        )

        assertEquals(
            listOf("Información falsa o engañosa — “No era así”", "Es spam o está duplicado", "Cobro, reventa o publicidad", "y 2 más"),
            reportLines(reports)
        )
    }

    @Test
    fun actionsDependOnStatusAndTab() {
        assertEquals(
            listOf(ModerationCardAction.VERIFY, ModerationCardAction.REJECT),
            moderationActions(EventStatus.PENDIENTE, ModerationTab.PENDIENTES)
        )
        assertEquals(
            listOf(ModerationCardAction.REJECT, ModerationCardAction.RESOLVE),
            moderationActions(EventStatus.ACTIVO, ModerationTab.REPORTES)
        )
        assertEquals(listOf(ModerationCardAction.REJECT), moderationActions(EventStatus.ACTIVO, ModerationTab.RESUELTAS))
        assertEquals(listOf(ModerationCardAction.RESTORE), moderationActions(EventStatus.RECHAZADO, ModerationTab.RECHAZADAS))
    }

    @Test
    fun contextLineDependsOnState() {
        val now = Instant.now()
        val pending = ModerationCase(event.copy(status = EventStatus.PENDIENTE, publicationDate = now.minusSeconds(5 * 3600)))
        val reported = ModerationCase(
            event.copy(status = EventStatus.ACTIVO),
            openReports = listOf(Report("r", event, user, "x", date = now.minusSeconds(40 * 60)))
        )

        assertEquals("enviado hace 5 h", moderationContext(pending, ModerationTab.PENDIENTES, now))
        assertEquals("último reporte hace 40 min", moderationContext(reported, ModerationTab.REPORTES, now))
    }
}
