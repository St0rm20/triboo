package edu.uniquindio.co.tribooo.domain.usecase.filters

import edu.uniquindio.co.tribooo.domain.model.EventFilters
import edu.uniquindio.co.tribooo.domain.repository.EventFilterRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class ObserveEventFiltersUseCase @Inject constructor(
    private val eventFilterRepository: EventFilterRepository
) {
    operator fun invoke(): StateFlow<EventFilters> = eventFilterRepository.filters
}
