package edu.uniquindio.co.tribooo.features.profile

import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.Follow
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class ProfileStatsTest {

    private fun user(id: String) = User(id, "Usuario $id", "$id@mail.co", "123456", "Armenia", "Centro")

    private fun event(id: String, organizer: User, status: EventStatus = EventStatus.ACTIVO) = Event(
        id = id, title = id, description = "", startDate = Instant.now(), endDate = Instant.now(),
        place = "", neighborhood = "", location = Location(0.0, 0.0), status = status,
        organizer = organizer, category = Category.SOCIAL
    )

    private val me = user("me")
    private val other = user("other")

    @Test
    fun countsOwnActivityAndAveragesRatings() {
        val mine = event("1", me)
        val theirs = event("2", other)
        val stats = calculateProfileStats(
            user = me,
            events = listOf(mine, theirs, event("3", me, EventStatus.PENDIENTE)),
            attendances = listOf(Attendance(me, theirs), Attendance(other, mine)),
            follows = listOf(Follow(other, me), Follow(me, other)),
            saved = emptyList(),
            comments = listOf(
                Comment("c1", mine, other, "ok", rating = 4),
                Comment("c2", mine, other, "genial", rating = 5),
                Comment("c3", theirs, me, "sin calificar", rating = 1)
            )
        )

        assertEquals(1, stats.followers)
        assertEquals(1, stats.attendances)
        assertEquals(2, stats.created)
        assertEquals(1, stats.pendingReview)
        assertEquals("4.5", stats.ratingLabel)
    }

    @Test
    fun withoutRatingsShowsDash() {
        val stats = calculateProfileStats(me, emptyList(), emptyList(), emptyList(), emptyList(), emptyList())

        assertNull(stats.rating)
        assertEquals("—", stats.ratingLabel)
    }
}
