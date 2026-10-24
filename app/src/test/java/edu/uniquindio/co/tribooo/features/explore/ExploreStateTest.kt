package edu.uniquindio.co.tribooo.features.explore

import edu.uniquindio.co.tribooo.core.component.toCardItems
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ExploreStateTest {

    private val me = User("me", "Ana Gómez", "a@mail.co", "123456", "Armenia", "Centro")
    private val other = User("x", "Otro", "o@mail.co", "123456", "Armenia", "Centro")
    private val here = Location(4.5, -75.6)

    private fun event(id: String, lat: Double, cupo: Int? = null) = Event(
        id = id, title = id, description = "", startDate = Instant.now(), endDate = Instant.now(),
        place = "", neighborhood = "", location = Location(lat, -75.6), cupo = cupo,
        organizer = other, category = Category.SOCIAL
    )

    @Test
    fun cardItemsKeepOrderAndCarryAttendanceAndDistance() {
        val near = event("cerca", 4.501, cupo = 1)
        val far = event("lejos", 4.6)
        val items = toCardItems(listOf(near, far), listOf(Attendance(me, near), Attendance(other, near)), "me", here)

        assertEquals(listOf("cerca", "lejos"), items.map { it.event.id })
        assertEquals(2, items[0].attendeesCount)
        assertTrue(items[0].isAttending)
        assertTrue(items[0].isFull)
        assertFalse(items[1].isAttending)
        assertTrue(items[0].distanceKm < items[1].distanceKm)
    }

    @Test
    fun selectionStartsOnTheClosestEvent() {
        val items = toCardItems(listOf(event("cerca", 4.501), event("lejos", 4.6)), emptyList(), "me", here)

        assertEquals("cerca", resolveSelected(items, null)?.event?.id)
    }

    @Test
    fun selectionFollowsTheChosenEventWhileItPassesTheFilters() {
        val items = toCardItems(listOf(event("cerca", 4.501), event("lejos", 4.6)), emptyList(), "me", here)

        assertEquals("lejos", resolveSelected(items, "lejos")?.event?.id)
    }

    @Test
    fun selectionFallsBackToTheFirstWhenTheEventLeavesTheFilter() {
        val remaining = toCardItems(listOf(event("cerca", 4.501)), emptyList(), "me", here)

        assertEquals("cerca", resolveSelected(remaining, "lejos")?.event?.id)
        assertNull(resolveSelected(emptyList(), "lejos"))
    }

    @Test
    fun localStateStartsOnTheMap() {
        assertEquals(ExploreView.MAP, ExploreLocal().view)
        assertEquals(ExploreView.LIST, ExploreLocal().copy(view = ExploreView.LIST).view)
    }
}
