package edu.uniquindio.co.tribooo.features.userprofile

import edu.uniquindio.co.tribooo.core.util.CommunityRole
import edu.uniquindio.co.tribooo.core.util.communityRole
import edu.uniquindio.co.tribooo.core.util.countOrDash
import edu.uniquindio.co.tribooo.core.util.ratingOrDash
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.Follow
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class UserProfileTest {

    private fun user(id: String, role: UserRole = UserRole.USER) =
        User(id, "Usuario $id", "$id@mail.co", "123456", "Armenia", "Centro", role = role)

    private val me = user("me")
    private val org = user("org")
    private val fan = user("fan")

    private fun event(id: String, organizer: User, status: EventStatus, day: Int = 10) = Event(
        id = id, title = id, description = "", startDate = Instant.parse("2026-11-%02dT12:00:00Z".format(day)),
        endDate = Instant.parse("2026-11-%02dT14:00:00Z".format(day)), place = "", neighborhood = "",
        location = Location(0.0, 0.0), status = status, organizer = organizer, category = Category.SOCIAL
    )

    private fun sources(
        events: List<Event> = emptyList(),
        attendances: List<Attendance> = emptyList(),
        follows: List<Follow> = emptyList(),
        comments: List<Comment> = emptyList()
    ) = UserProfileSources(events, attendances, follows, comments)

    @Test
    fun countsFollowersAndAttendancesOfThatUserOnly() {
        val e = event("e", org, EventStatus.ACTIVO)
        val state = buildUserProfileState(
            user = org, currentUser = me,
            sources = sources(
                events = listOf(e),
                attendances = listOf(Attendance(org, e), Attendance(fan, e), Attendance(org, event("x", fan, EventStatus.ACTIVO))),
                follows = listOf(Follow(me, org), Follow(fan, org), Follow(org, fan))
            )
        )

        assertEquals(2, state.followers)
        assertEquals(2, state.attendances)
        assertTrue(state.isFollowing)
    }

    @Test
    fun createdEventsIgnorePendingAndRejected() {
        val state = buildUserProfileState(
            org, me,
            sources(
                events = listOf(
                    event("a", org, EventStatus.ACTIVO),
                    event("b", org, EventStatus.FINALIZADO),
                    event("c", org, EventStatus.PENDIENTE),
                    event("d", org, EventStatus.RECHAZADO),
                    event("e", fan, EventStatus.ACTIVO)
                )
            )
        )

        assertEquals(2, state.createdEvents)
        assertEquals(CommunityRole.ORGANIZER, state.role)
    }

    @Test
    fun dashWhenNoEventsNorRatings() {
        val state = buildUserProfileState(org, me, sources())

        assertEquals("—", state.createdEventsLabel)
        assertEquals("—", state.ratingLabel)
        assertNull(state.rating)
    }

    @Test
    fun ratingIsAverageOverPublicEventsWithOneDecimal() {
        val pending = event("p", org, EventStatus.PENDIENTE)
        val a = event("a", org, EventStatus.ACTIVO)
        val state = buildUserProfileState(
            org, me,
            sources(
                events = listOf(a, pending),
                comments = listOf(
                    Comment("1", a, fan, "x", rating = 5),
                    Comment("2", a, me, "x", rating = 4),
                    Comment("3", a, me, "sin estrellas"),
                    Comment("4", pending, fan, "x", rating = 1)
                )
            )
        )

        assertEquals(4.5, state.rating!!, 0.0001)
        assertEquals("4.5", state.ratingLabel)
        assertEquals("4.7", ratingOrDash(4.666))
        assertEquals("—", countOrDash(0))
        assertEquals("3", countOrDash(3))
    }

    @Test
    fun roleRuleThresholds() {
        assertEquals(CommunityRole.NEIGHBOR, communityRole(UserRole.USER, 0))
        assertEquals(CommunityRole.ORGANIZER, communityRole(UserRole.USER, 1))
        assertEquals(CommunityRole.ORGANIZER, communityRole(UserRole.USER, 9))
        assertEquals(CommunityRole.LEADER, communityRole(UserRole.USER, 10))
        assertEquals(CommunityRole.ADMIN, communityRole(UserRole.ADMIN, 0))
        assertEquals(CommunityRole.ADMIN, communityRole(UserRole.ADMIN, 25))
    }

    @Test
    fun eventListIsOnlyActiveSortedByStart() {
        val state = buildUserProfileState(
            org, me,
            sources(
                events = listOf(
                    event("late", org, EventStatus.ACTIVO, day = 20),
                    event("done", org, EventStatus.FINALIZADO, day = 1),
                    event("early", org, EventStatus.ACTIVO, day = 5),
                    event("pending", org, EventStatus.PENDIENTE, day = 2)
                )
            )
        )

        assertEquals(listOf("early", "late"), state.events.map { it.id })
    }

    @Test
    fun ownProfileHasNoFollowNorReportButtons() {
        val own = buildUserProfileState(me, me, sources())
        val other = buildUserProfileState(org, me, sources())
        val missing = buildUserProfileState(null, me, sources())

        assertTrue(own.isCurrentUser)
        assertFalse(own.canInteract)
        assertTrue(other.canInteract)
        assertFalse(missing.canInteract)
        assertFalse(missing.isLoading)
        assertNull(missing.user)
    }
}
