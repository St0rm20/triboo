package edu.uniquindio.co.tribooo.domain.usecase.user

import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.Follow
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.usecase.event.testEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReputationCalculatorTest {

    private fun user(id: String) = User(id, "Usuario $id", "$id@mail.co", "Armenia", "Centro")

    private val me = user("me")
    private val org = user("org")

    private fun mine(id: String, status: EventStatus = EventStatus.ACTIVO, verified: Boolean = false): Event =
        testEvent(id, me, status).copy(verified = verified)

    private fun calc(
        events: List<Event> = emptyList(),
        attendances: List<Attendance> = emptyList(),
        comments: List<Comment> = emptyList(),
        follows: List<Follow> = emptyList()
    ) = ReputationCalculator.calculate(me, events, attendances, comments, follows)

    private fun attendancesToOthers(n: Int) = List(n) { Attendance(me, testEvent("o$it", org, EventStatus.ACTIVO)) }
    private fun followers(n: Int) = List(n) { Follow(user("f$it"), me) }
    private fun reviews(event: Event, vararg ratings: Int) =
        ratings.mapIndexed { i, r -> Comment("r$i", event, user("a$i"), "x", rating = r) }

    // Puntos

    @Test
    fun ownActiveOrFinishedEventsGiveTenEach() {
        val events = listOf(mine("a"), mine("b", EventStatus.FINALIZADO), mine("c", EventStatus.PENDIENTE), mine("d", EventStatus.RECHAZADO))
        assertEquals(20, calc(events = events).points)
    }

    @Test
    fun verifiedEventsGiveFiveExtra() {
        assertEquals(15, calc(events = listOf(mine("a", verified = true))).points)
        assertEquals(0, calc(events = listOf(mine("a", EventStatus.PENDIENTE, verified = true))).points)
    }

    @Test
    fun attendancesToOthersGiveTwoButOwnEventsDoNot() {
        val own = mine("own")
        val attendances = attendancesToOthers(3) + Attendance(me, own) + Attendance(org, testEvent("z", org, EventStatus.ACTIVO))
        assertEquals(6, calc(attendances = attendances).points)
    }

    @Test
    fun goodRootReviewsGiveThreeEach() {
        val event = mine("a")
        val root = reviews(event, 5, 4, 3, 1)
        val reply = Comment("reply", event, me, "gracias", rating = 5, parentComment = root.first())
        val own = Comment("self", event, me, "yo", rating = 5)
        val elsewhere = Comment("else", testEvent("e", org, EventStatus.ACTIVO), org, "x", rating = 5)

        assertEquals(10 + 6, calc(events = listOf(event), comments = root + reply + own + elsewhere).points)
    }

    @Test
    fun followersGiveOneEach() {
        assertEquals(4, calc(follows = followers(4) + Follow(me, org)).points)
    }

    @Test
    fun combinedPoints() {
        val event = mine("a", verified = true)
        val result = calc(
            events = listOf(event),
            attendances = attendancesToOthers(2),
            comments = reviews(event, 5, 4),
            follows = followers(3)
        )
        // 10 + 5 + 2*2 + 2*3 + 3
        assertEquals(28, result.points)
        assertEquals(listOf(Badge.FIRST_EVENT, Badge.VERIFIED_HOST), result.badges)
    }

    // Insignias

    @Test
    fun firstEventBadge() {
        assertTrue(Badge.FIRST_EVENT in calc(events = listOf(mine("a", EventStatus.FINALIZADO))).badges)
        assertFalse(Badge.FIRST_EVENT in calc(events = listOf(mine("a", EventStatus.PENDIENTE))).badges)
    }

    @Test
    fun verifiedHostBadge() {
        assertTrue(Badge.VERIFIED_HOST in calc(events = listOf(mine("a", verified = true))).badges)
        assertFalse(Badge.VERIFIED_HOST in calc(events = listOf(mine("a"))).badges)
    }

    @Test
    fun frequentAttendeeBadgeAtTenAttendances() {
        assertTrue(Badge.FREQUENT_ATTENDEE in calc(attendances = attendancesToOthers(10)).badges)
        assertFalse(Badge.FREQUENT_ATTENDEE in calc(attendances = attendancesToOthers(9)).badges)
    }

    @Test
    fun wellRatedBadgeNeedsThreeReviewsAndAverage() {
        val event = mine("a")
        assertTrue(Badge.WELL_RATED in calc(events = listOf(event), comments = reviews(event, 5, 4, 5, 5)).badges) // 4.75
        assertTrue(Badge.WELL_RATED in calc(events = listOf(event), comments = reviews(event, 5, 5, 4, 4)).badges) // 4.5 justo en el umbral
        assertFalse(Badge.WELL_RATED in calc(events = listOf(event), comments = reviews(event, 5, 5, 4, 3)).badges) // 4.25
        assertFalse(Badge.WELL_RATED in calc(events = listOf(event), comments = reviews(event, 5, 5)).badges) // solo 2 reseñas
        assertTrue(Badge.WELL_RATED in calc(events = listOf(event), comments = reviews(event, 5, 5, 5)).badges) // justo 3 reseñas
    }

    @Test
    fun popularBadgeAtTwentyFollowers() {
        assertTrue(Badge.POPULAR in calc(follows = followers(20)).badges)
        assertFalse(Badge.POPULAR in calc(follows = followers(19)).badges)
    }
}
