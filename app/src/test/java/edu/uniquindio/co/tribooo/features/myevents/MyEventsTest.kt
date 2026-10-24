package edu.uniquindio.co.tribooo.features.myevents

import edu.uniquindio.co.tribooo.core.util.averageRating
import edu.uniquindio.co.tribooo.core.util.latestRejectionDate
import edu.uniquindio.co.tribooo.core.util.myEventFooter
import edu.uniquindio.co.tribooo.core.util.myEventSummary
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.ModerationAction
import edu.uniquindio.co.tribooo.domain.model.ModerationReview
import edu.uniquindio.co.tribooo.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class MyEventsTest {

    private val now = Instant.parse("2026-10-23T17:00:00Z")
    private val me = User("me", "Yo", "me@mail.co", "123456", "Armenia", "Centro")
    private val fan = User("fan", "Vecino", "fan@mail.co", "123456", "Armenia", "Centro")

    private fun event(
        id: String,
        status: EventStatus,
        cupo: Int? = null,
        start: String = "2026-11-01T12:00:00Z",
        published: String = "2026-10-20T17:00:00Z",
        end: String = start
    ) = Event(
        id = id, title = id, description = "", startDate = Instant.parse(start), endDate = Instant.parse(end),
        place = "", neighborhood = "", location = Location(0.0, 0.0), cupo = cupo, status = status,
        publicationDate = Instant.parse(published), organizer = me, category = Category.SOCIAL
    )

    @Test
    fun tabShowsOnlyItsStatusNewestFirst() {
        val events = listOf(
            event("old", EventStatus.ACTIVO, start = "2026-11-01T12:00:00Z"),
            event("pending", EventStatus.PENDIENTE),
            event("new", EventStatus.ACTIVO, start = "2026-12-01T12:00:00Z")
        )

        val items = buildMyEventItems(events, MyEventsTab.ACTIVOS, emptyList(), emptyList(), emptyList(), now)

        assertEquals(listOf("new", "old"), items.map { it.event.id })
        assertEquals(emptyList<MyEventItem>(), buildMyEventItems(events, MyEventsTab.RECHAZADOS, emptyList(), emptyList(), emptyList(), now))
    }

    @Test
    fun itemCountsAttendeesOfItsOwnEvent() {
        val a = event("a", EventStatus.ACTIVO, cupo = 10)
        val b = event("b", EventStatus.ACTIVO)
        val attendances = listOf(Attendance(fan, a), Attendance(me, a), Attendance(fan, b))

        val items = buildMyEventItems(listOf(a, b), MyEventsTab.ACTIVOS, attendances, emptyList(), emptyList(), now)

        assertEquals("1 confirmados", items.first { it.event.id == "b" }.summary)
        assertEquals("2 de 10 confirmados", items.first { it.event.id == "a" }.summary)
    }

    @Test
    fun activeSummaryWithAndWithoutCupo() {
        assertEquals("87 de 120 confirmados", myEventSummary(event("a", EventStatus.ACTIVO, cupo = 120), 87, null))
        assertEquals("118 confirmados", myEventSummary(event("a", EventStatus.ACTIVO), 118, null))
    }

    @Test
    fun summaryForOtherStatuses() {
        assertEquals("Esperando moderación", myEventSummary(event("p", EventStatus.PENDIENTE), 0, null))
        assertEquals("No se publicó en el feed", myEventSummary(event("r", EventStatus.RECHAZADO), 0, null))
        assertEquals("34 asistentes · calificación 4.8", myEventSummary(event("f", EventStatus.FINALIZADO), 34, 4.8))
        assertEquals("34 asistentes", myEventSummary(event("f", EventStatus.FINALIZADO), 34, null))
    }

    @Test
    fun ratingIsAverageOfStarsOfThatEvent() {
        val a = event("a", EventStatus.FINALIZADO)
        val b = event("b", EventStatus.FINALIZADO)
        val comments = listOf(
            Comment("1", a, fan, "bien", rating = 5),
            Comment("2", a, fan, "ok", rating = 4),
            Comment("3", a, fan, "sin estrellas"),
            Comment("4", b, fan, "otro evento", rating = 1)
        )

        assertEquals(4.5, averageRating("a", comments)!!, 0.0001)
        assertNull(averageRating("c", comments))
    }

    @Test
    fun footerTextsPerStatus() {
        assertEquals("publicado hace 3 días", myEventFooter(event("a", EventStatus.ACTIVO), now))
        assertEquals(
            "Enviado hace 5 h",
            myEventFooter(event("p", EventStatus.PENDIENTE, published = "2026-10-23T12:00:00Z"), now)
        )
        assertEquals(
            "Enviado hace 2 días",
            myEventFooter(event("p", EventStatus.PENDIENTE, published = "2026-10-21T12:00:00Z"), now)
        )
        assertEquals(
            "Finalizado el 18 jul 2026",
            myEventFooter(event("f", EventStatus.FINALIZADO, start = "2026-07-18T15:00:00Z"), now)
        )
        assertEquals("Rechazado el 20 oct 2026", myEventFooter(event("r", EventStatus.RECHAZADO), now))
    }

    private fun review(eventId: String, action: ModerationAction, date: String, status: EventStatus = EventStatus.RECHAZADO) =
        ModerationReview("mr-$eventId-$date", event(eventId, status), me, action, date = Instant.parse(date))

    @Test
    fun latestRejectionDateIsTheMostRecentRejectionOfThatEvent() {
        val reviews = listOf(
            review("r", ModerationAction.RECHAZAR, "2026-10-10T12:00:00Z"),
            review("r", ModerationAction.RECHAZAR, "2026-10-15T12:00:00Z"),
            review("r", ModerationAction.VERIFICAR, "2026-10-20T12:00:00Z"),
            review("otro", ModerationAction.RECHAZAR, "2026-10-22T12:00:00Z")
        )

        assertEquals(Instant.parse("2026-10-15T12:00:00Z"), latestRejectionDate("r", reviews))
        assertNull(latestRejectionDate("r", reviews.filter { it.action != ModerationAction.RECHAZAR }))
        assertNull(latestRejectionDate("sin-revision", reviews))
        assertNull(latestRejectionDate("r", emptyList()))
    }

    @Test
    fun rejectedFooterUsesTheReviewDateAndFallsBackToPublication() {
        val rejected = event("r", EventStatus.RECHAZADO)
        assertEquals("Rechazado el 22 oct 2026", myEventFooter(rejected, now, rejectedAt = Instant.parse("2026-10-22T15:00:00Z")))
        assertEquals("Rechazado el 20 oct 2026", myEventFooter(rejected, now, rejectedAt = null))
    }

    @Test
    fun rejectedItemShowsTheReviewDateButOtherStatusesIgnoreReviews() {
        val rejected = event("r", EventStatus.RECHAZADO)
        val active = event("a", EventStatus.ACTIVO)
        val reviews = listOf(
            review("r", ModerationAction.RECHAZAR, "2026-10-22T15:00:00Z"),
            review("a", ModerationAction.RECHAZAR, "2026-10-22T15:00:00Z", status = EventStatus.ACTIVO)
        )

        val rejectedItems = buildMyEventItems(listOf(rejected, active), MyEventsTab.RECHAZADOS, emptyList(), emptyList(), reviews, now)
        val activeItems = buildMyEventItems(listOf(rejected, active), MyEventsTab.ACTIVOS, emptyList(), emptyList(), reviews, now)

        assertEquals("Rechazado el 22 oct 2026", rejectedItems.single().footer)
        assertEquals("publicado hace 3 días", activeItems.single().footer)
    }
}
