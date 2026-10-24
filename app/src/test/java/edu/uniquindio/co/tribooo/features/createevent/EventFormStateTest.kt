package edu.uniquindio.co.tribooo.features.createevent

import edu.uniquindio.co.tribooo.core.util.APP_ZONE
import edu.uniquindio.co.tribooo.data.repository.memory.MockData
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Contribution
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.PlaceSuggestion
import edu.uniquindio.co.tribooo.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class EventFormStateTest {

    private val now = LocalDateTime.of(2026, 10, 24, 15, 0).atZone(APP_ZONE).toInstant()
    private val me = User("me", "Yo", "me@mail.co", "123456", "Armenia", "Centro")
    private val suggestions = MockData.placeSuggestions

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int = 0): Instant =
        LocalDateTime.of(year, month, day, hour, minute).atZone(APP_ZONE).toInstant()

    private fun event(
        start: Instant,
        end: Instant,
        multidia: Boolean = false,
        cupo: Int? = null,
        contribution: Contribution? = null,
        place: String = "Lugar propio",
        neighborhood: String = "Barrio propio"
    ) = Event(
        id = "e1", title = "Jornada", description = "Texto", startDate = start, endDate = end, multidia = multidia,
        place = place, neighborhood = neighborhood, location = Location(4.5, -75.6), cupo = cupo,
        contribution = contribution, status = EventStatus.PENDIENTE, organizer = me, category = Category.SOCIAL
    )

    @Test
    fun futureEventKeepsItsDates() {
        val state = event(at(2026, 11, 5, 18), at(2026, 11, 5, 20, 30))
            .toFormState(EventFormMode.EDIT, emptyList(), suggestions, now)

        assertEquals(EventFormMode.EDIT, state.mode)
        assertEquals(LocalDate.of(2026, 11, 5), state.startDate)
        assertEquals(LocalTime.of(18, 0), state.startTime)
        assertEquals(LocalDate.of(2026, 11, 5), state.endDate)
        assertEquals(LocalTime.of(20, 30), state.endTime)
        assertEquals("Jornada", state.title)
        assertEquals("Texto", state.description)
        assertEquals(Category.SOCIAL, state.category)
    }

    @Test
    fun pastEventShiftsToTomorrowKeepingTimeAndDuration() {
        val state = event(at(2026, 9, 1, 18), at(2026, 9, 1, 20, 30))
            .toFormState(EventFormMode.RESUBMIT, emptyList(), suggestions, now)

        assertEquals(LocalDate.of(2026, 10, 25), state.startDate)
        assertEquals(LocalTime.of(18, 0), state.startTime)
        assertEquals(LocalTime.of(20, 30), state.endTime)
        assertEquals(2 * 60 + 30L, java.time.Duration.between(state.start, state.end).toMinutes())
    }

    @Test
    fun duplicateShiftsEvenFutureEvents() {
        val state = event(at(2026, 11, 5, 18), at(2026, 11, 5, 20))
            .toFormState(EventFormMode.DUPLICATE, emptyList(), suggestions, now)

        assertEquals(LocalDate.of(2026, 10, 25), state.startDate)
        assertEquals(LocalTime.of(18, 0), state.startTime)
    }

    @Test
    fun multiDayShiftKeepsTheNumberOfDays() {
        val state = event(at(2026, 9, 10, 9), at(2026, 9, 13, 17), multidia = true)
            .toFormState(EventFormMode.DUPLICATE, emptyList(), suggestions, now)

        assertTrue(state.multiDay)
        assertEquals(LocalDate.of(2026, 10, 25), state.startDate)
        assertEquals(LocalDate.of(2026, 10, 28), state.endDate)
        assertEquals(LocalTime.of(9, 0), state.startTime)
        assertEquals(LocalTime.of(17, 0), state.endTime)
        assertTrue(state.isRangeValid)
    }

    @Test
    fun optionalCupoAndContribution() {
        val plain = event(at(2026, 11, 5, 18), at(2026, 11, 5, 20))
            .toFormState(EventFormMode.EDIT, emptyList(), suggestions, now)
        assertFalse(plain.cupoEnabled)
        assertFalse(plain.contributionEnabled)

        val full = event(at(2026, 11, 5, 18), at(2026, 11, 5, 20), cupo = 25, contribution = Contribution(5000.0))
            .toFormState(EventFormMode.EDIT, emptyList(), suggestions, now)
        assertTrue(full.cupoEnabled)
        assertEquals(25, full.cupo)
        assertTrue(full.contributionEnabled)
        assertEquals(5000L, full.contributionAmount)
        assertEquals("", full.contributionCause)
    }

    @Test
    fun declaredCauseIsPreloadedWhenEditingAndDuplicating() {
        val original = event(at(2026, 11, 5, 18), at(2026, 11, 5, 20), contribution = Contribution(5000.0, "Mercados para el barrio"))

        EventFormMode.entries.forEach { mode ->
            assertEquals("Mercados para el barrio", original.toFormState(mode, emptyList(), suggestions, now).contributionCause)
        }
    }

    @Test
    fun placeComesFromMatchingSuggestionOrIsRebuilt() {
        val known = suggestions.first()
        val matched = event(at(2026, 11, 5, 18), at(2026, 11, 5, 20), place = known.name, neighborhood = known.neighborhood)
            .toFormState(EventFormMode.EDIT, emptyList(), suggestions, now)
        assertSame(known, matched.place)

        val rebuilt = event(at(2026, 11, 5, 18), at(2026, 11, 5, 20))
            .toFormState(EventFormMode.EDIT, listOf("a.jpg", "b.jpg"), suggestions, now)
        assertEquals(PlaceSuggestion("Lugar propio", "Barrio propio", "Barrio propio", Location(4.5, -75.6)), rebuilt.place)
        assertEquals(listOf("a.jpg", "b.jpg"), rebuilt.imageUris)
    }
}
