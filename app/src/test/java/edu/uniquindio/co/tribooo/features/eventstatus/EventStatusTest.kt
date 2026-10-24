package edu.uniquindio.co.tribooo.features.eventstatus

import edu.uniquindio.co.tribooo.core.util.finishedFooter
import edu.uniquindio.co.tribooo.core.util.pendingFooter
import edu.uniquindio.co.tribooo.core.util.removeDialogTexts
import edu.uniquindio.co.tribooo.core.util.reviewHoursLeft
import edu.uniquindio.co.tribooo.core.util.reviewsOf
import edu.uniquindio.co.tribooo.core.util.statusParagraph
import edu.uniquindio.co.tribooo.core.util.statusSteps
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class EventStatusTest {

    private val now = Instant.parse("2026-10-23T17:00:00Z")
    private val me = User("me", "Yo", "me@mail.co", "123456", "Armenia", "Centro")
    private val fan = User("fan", "Vecino Uno", "fan@mail.co", "123456", "Armenia", "Centro")

    private fun event(id: String, status: EventStatus, organizer: User = me) = Event(
        id = id, title = id, description = "", startDate = now, endDate = now, place = "", neighborhood = "",
        location = Location(0.0, 0.0), status = status, organizer = organizer, category = Category.SOCIAL
    )

    @Test
    fun hoursLeftOfTheReview() {
        assertEquals(5, reviewHoursLeft(now.minusSeconds(19 * 3600), now))
        assertEquals(24, reviewHoursLeft(now, now))
        assertEquals(1, reviewHoursLeft(now.minusSeconds(23 * 3600 + 60), now)) // 59 min restantes
        assertNull(reviewHoursLeft(now.minusSeconds(24 * 3600), now))
        assertNull(reviewHoursLeft(now.minusSeconds(30 * 3600), now))
    }

    @Test
    fun pendingFooterWithAndWithoutTimeLeft() {
        assertTrue(pendingFooter(now.minusSeconds(19 * 3600), now).endsWith("quedan ~5 h de revisión"))
        assertTrue(pendingFooter(now.minusSeconds(30 * 3600), now).endsWith("la revisión lleva más de 24 h"))
    }

    @Test
    fun textsPerStatus() {
        assertEquals("Moderación no registró un motivo.", statusParagraph(EventStatus.RECHAZADO, null))
        assertEquals("Por una rifa.", statusParagraph(EventStatus.RECHAZADO, "Por una rifa."))
        assertTrue(finishedFooter(Instant.parse("2026-07-18T15:00:00Z")).endsWith("19 jul 2026, 10:00"))
        assertEquals("¿Retirar el envío?", removeDialogTexts(EventStatus.PENDIENTE).title)
        assertEquals("Evento rechazado eliminado.", removeDialogTexts(EventStatus.RECHAZADO).doneMessage)
        assertEquals(3, statusSteps(EventStatus.FINALIZADO, 2).size)
        assertEquals(2, statusSteps(EventStatus.FINALIZADO, 0).size)
    }

    @Test
    fun reviewsAreRatedCommentsWithoutParent() {
        val e = event("e", EventStatus.FINALIZADO)
        val review = Comment("r1", e, fan, "bien", rating = 5)
        val comments = listOf(
            review,
            Comment("r2", e, fan, "sin estrellas"),
            Comment("r1-reply", e, me, "gracias", parentComment = review),
            Comment("other", event("x", EventStatus.FINALIZADO), fan, "otro evento", rating = 3)
        )

        assertEquals(listOf("r1"), reviewsOf("e", comments).map { it.id })
    }

    @Test
    fun stateOnlyShowsOwnNonActiveEventsAndCountsReviewsWithoutReplies() {
        val finished = event("f", EventStatus.FINALIZADO)
        val review = Comment("r1", finished, fan, "bien", date = now, rating = 4)
        val sources = EventStatusSources(
            events = listOf(finished, event("a", EventStatus.ACTIVO), event("o", EventStatus.PENDIENTE, organizer = fan)),
            moderation = emptyList(),
            attendances = emptyList(),
            comments = listOf(review, Comment("r1-r", finished, me, "gracias", date = now, parentComment = review))
        )

        val state = buildEventStatusState("f", me, sources, now)
        assertEquals(1, state.reviews.size)
        assertEquals("4.0", state.ratingLabel)
        assertEquals("gracias", state.reviews.first().reply?.text)

        assertNull(buildEventStatusState("a", me, sources, now).event) // activo
        assertNull(buildEventStatusState("o", me, sources, now).event) // ajeno
        assertNull(buildEventStatusState("zzz", me, sources, now).event) // no existe
        assertNotNull(buildEventStatusState("f", me, sources, now).event)
    }
}
