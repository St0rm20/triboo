package edu.uniquindio.co.tribooo.domain.usecase.filters

import edu.uniquindio.co.tribooo.domain.model.EventFilters
import edu.uniquindio.co.tribooo.domain.repository.EventFilterRepository
import jakarta.inject.Inject

class UpdateEventFiltersUseCase @Inject constructor(
    private val eventFilterRepository: EventFilterRepository
) {
    /** Aplica [transform] a los filtros actuales. */
    operator fun invoke(transform: (EventFilters) -> EventFilters) = eventFilterRepository.update(transform)
}
