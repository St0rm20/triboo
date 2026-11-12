package edu.uniquindio.co.tribooo.domain.usecase.filters

import edu.uniquindio.co.tribooo.data.repository.memory.EventFilterRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.EventFilters
import edu.uniquindio.co.tribooo.domain.model.WhenFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class EventFiltersTest {

    @Test
    fun activeCountAddsCategoriesAndEachChangedFilterButNotTheQuery() {
        assertEquals(0, EventFilters(query = "algo").activeCount)
        assertEquals(2, EventFilters(categories = setOf("a", "b")).activeCount)
        assertEquals(
            1 + 1 + 1 + 1,
            EventFilters(categories = setOf("a"), onlyAvailable = true, whenFilter = WhenFilter.WEEK, maxDistanceKm = 3).activeCount
        )
        // 8 km es el valor por defecto: no cuenta
        assertEquals(0, EventFilters(maxDistanceKm = 8).activeCount)
    }

    @Test
    fun withDraftKeepsTheCurrentQuery() {
        val current = EventFilters(query = "parque")
        val draft = EventFilters(query = "otro", categories = setOf("cultura"), maxDistanceKm = 4)

        assertEquals(EventFilters(query = "parque", categories = setOf("cultura"), maxDistanceKm = 4), current.withDraft(draft))
    }

    @Test
    fun repositoryKeepsFiltersUntilCleared() {
        val repository = EventFilterRepositoryImpl()
        val update = UpdateEventFiltersUseCase(repository)
        val observe = ObserveEventFiltersUseCase(repository)

        update { it.copy(query = "x", onlyAvailable = true) }
        assertEquals(EventFilters(query = "x", onlyAvailable = true), observe().value)

        ClearEventFiltersUseCase(repository)()
        assertEquals(EventFilters(), observe().value)
    }
}
