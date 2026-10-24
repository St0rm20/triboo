package edu.uniquindio.co.tribooo.domain.usecase.filters

import edu.uniquindio.co.tribooo.core.util.APP_ZONE
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventFilters
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.model.WhenFilter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class FilterEventsUseCaseTest {

    private val filter = FilterEventsUseCase()
    private val organizer = User("o", "Org", "o@mail.co", "123456", "Armenia", "Centro")
    private val today = LocalDate.of(2026, 10, 21)
    private val here = Location(4.5339, -75.6811)

    /** Cada 0.01° de latitud son ~1,1 km. */
    private fun event(
        id: String,
        daysAhead: Long = 1,
        category: Category = Category.SOCIAL,
        title: String = "Evento $id",
        place: String = "Plaza",
        neighborhood: String = "Centro",
        cupo: Int? = null,
        km: Double = 1.0
    ) = Event(
        id = id, title = title, description = "",
        startDate = today.plusDays(daysAhead).atTime(LocalTime.of(18, 0)).atZone(APP_ZONE).toInstant(),
        endDate = today.plusDays(daysAhead).atTime(LocalTime.of(20, 0)).atZone(APP_ZONE).toInstant(),
        place = place, neighborhood = neighborhood,
        location = Location(here.latitude + km / 111.0, here.longitude),
        cupo = cupo, organizer = organizer, category = category
    )

    private fun run(events: List<Event>, filters: EventFilters = EventFilters(), attendees: Map<String, Int> = emptyMap()) =
        filter(events, attendees, here, filters, today).map { it.id }

    @Test
    fun noFiltersKeepsEverythingSortedByDistance() {
        val events = listOf(event("lejos", km = 6.0), event("cerca", km = 0.5), event("medio", km = 3.0))

        assertEquals(listOf("cerca", "medio", "lejos"), run(events))
    }

    @Test
    fun categoriesFilterByKey() {
        val events = listOf(event("a", category = Category.DEPORTES), event("b", category = Category.CULTURA))

        assertEquals(listOf("a"), run(events, EventFilters(categories = setOf("deportes"))))
        assertEquals(setOf("a", "b"), run(events, EventFilters(categories = setOf("deportes", "cultura"))).toSet())
    }

    @Test
    fun queryMatchesTitlePlaceAndNeighborhoodIgnoringCase() {
        val events = listOf(
            event("a", title = "Ciclopaseo"),
            event("b", place = "Parque de la Vida"),
            event("c", neighborhood = "Granada")
        )

        assertEquals(listOf("a"), run(events, EventFilters(query = " CICLO ")))
        assertEquals(listOf("b"), run(events, EventFilters(query = "parque")))
        assertEquals(listOf("c"), run(events, EventFilters(query = "granada")))
    }

    @Test
    fun onlyAvailableHidesFullEventsOnlyWhenActive() {
        val full = event("lleno", cupo = 10)
        val free = event("libre", cupo = 10)
        val open = event("sin-cupo")
        val attendees = mapOf("lleno" to 10, "libre" to 9, "sin-cupo" to 500)

        assertEquals(3, run(listOf(full, free, open), attendees = attendees).size)
        assertEquals(
            setOf("libre", "sin-cupo"),
            run(listOf(full, free, open), EventFilters(onlyAvailable = true), attendees).toSet()
        )
    }

    @Test
    fun weekIncludesDayZeroToSevenAndMonthZeroToThirty() {
        val events = listOf(
            event("ayer", daysAhead = -1), event("hoy", daysAhead = 0), event("d7", daysAhead = 7),
            event("d8", daysAhead = 8), event("d30", daysAhead = 30), event("d31", daysAhead = 31)
        )

        assertEquals(setOf("hoy", "d7"), run(events, EventFilters(whenFilter = WhenFilter.WEEK)).toSet())
        assertEquals(setOf("hoy", "d7", "d8", "d30"), run(events, EventFilters(whenFilter = WhenFilter.MONTH)).toSet())
        assertEquals(6, run(events, EventFilters(whenFilter = WhenFilter.ANY)).size)
    }

    @Test
    fun distanceKeepsEventsWithinMaxKm() {
        val events = listOf(event("a", km = 2.0), event("b", km = 7.5), event("c", km = 9.0))

        assertEquals(listOf("a", "b"), run(events))
        assertEquals(listOf("a"), run(events, EventFilters(maxDistanceKm = 3)))
        assertEquals(listOf("a", "b", "c"), run(events, EventFilters(maxDistanceKm = 15)))
    }

    @Test
    fun filtersCombine() {
        val events = listOf(
            event("ok", category = Category.DEPORTES, daysAhead = 3, cupo = 5, km = 2.0, title = "Ciclopaseo"),
            event("lleno", category = Category.DEPORTES, daysAhead = 3, cupo = 5, km = 2.0, title = "Ciclopaseo 2"),
            event("tarde", category = Category.DEPORTES, daysAhead = 20, cupo = 5, km = 2.0, title = "Ciclopaseo 3"),
            event("otra", category = Category.CULTURA, daysAhead = 3, km = 2.0, title = "Ciclopaseo 4")
        )
        val filters = EventFilters(
            query = "ciclo", categories = setOf("deportes"), onlyAvailable = true,
            whenFilter = WhenFilter.WEEK, maxDistanceKm = 5
        )

        assertEquals(listOf("ok"), run(events, filters, mapOf("lleno" to 5)))
    }
}
