package edu.uniquindio.co.tribooo.domain.usecase.location

import edu.uniquindio.co.tribooo.domain.model.PlaceSuggestion
import edu.uniquindio.co.tribooo.domain.repository.LocationRepository
import jakarta.inject.Inject

class GetPlaceSuggestionsUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    operator fun invoke(): List<PlaceSuggestion> = locationRepository.placeSuggestions
}
