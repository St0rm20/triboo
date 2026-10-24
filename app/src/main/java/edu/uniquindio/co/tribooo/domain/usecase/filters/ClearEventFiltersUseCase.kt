package edu.uniquindio.co.tribooo.domain.usecase.filters

import edu.uniquindio.co.tribooo.domain.repository.EventFilterRepository
import jakarta.inject.Inject

class ClearEventFiltersUseCase @Inject constructor(
    private val eventFilterRepository: EventFilterRepository
) {
    operator fun invoke() = eventFilterRepository.clear()
}
