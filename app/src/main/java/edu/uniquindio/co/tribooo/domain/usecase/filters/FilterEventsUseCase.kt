package edu.uniquindio.co.tribooo.domain.usecase.filters

import edu.uniquindio.co.tribooo.core.util.APP_ZONE
import edu.uniquindio.co.tribooo.core.util.atAppZone
import edu.uniquindio.co.tribooo.core.util.distanceKmTo
import edu.uniquindio.co.tribooo.core.util.isFull
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventFilters
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.WhenFilter
import jakarta.inject.Inject
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Días hacia adelante que cubren "Esta semana" y "Este mes". */
private const val WEEK_DAYS = 7L
private const val MONTH_DAYS = 30L

class FilterEventsUseCase @Inject constructor() {

    /**
     * Eventos de [events] que cumplen [filters], del más cercano a [location] al más lejano.
     * [attendeesByEvent] trae los asistentes por id de evento; [today] es la fecha de referencia de "Cuándo".
     */
    operator fun invoke(
        events: List<Event>,
        attendeesByEvent: Map<String, Int>,
        location: Location,
        filters: EventFilters,
        today: LocalDate = LocalDate.now(APP_ZONE)
    ): List<Event> {
        val text = filters.query.trim().lowercase()

        return events
            .filter { filters.categories.isEmpty() || it.category.key in filters.categories }
            .filter { text.isEmpty() || "${it.title} ${it.place} ${it.neighborhood}".lowercase().contains(text) }
            .filter { !filters.onlyAvailable || !it.isFull(attendeesByEvent[it.id] ?: 0) }
            .filter { startsWithin(it, filters.whenFilter, today) }
            .filter { location.distanceKmTo(it.location) <= filters.maxDistanceKm }
            .sortedBy { location.distanceKmTo(it.location) }
    }

    /** "Esta semana": empieza de hoy a 7 días; "Este mes": de hoy a 30 días (ambos inclusive). */
    private fun startsWithin(event: Event, filter: WhenFilter, today: LocalDate): Boolean {
        val days = ChronoUnit.DAYS.between(today, event.startDate.atAppZone().toLocalDate())
        return when (filter) {
            WhenFilter.ANY -> true
            WhenFilter.WEEK -> days in 0..WEEK_DAYS
            WhenFilter.MONTH -> days in 0..MONTH_DAYS
        }
    }
}
