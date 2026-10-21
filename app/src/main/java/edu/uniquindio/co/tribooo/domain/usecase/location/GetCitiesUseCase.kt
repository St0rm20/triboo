package edu.uniquindio.co.tribooo.domain.usecase.location

import edu.uniquindio.co.tribooo.domain.repository.LocationRepository
import jakarta.inject.Inject

class GetCitiesUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    operator fun invoke(): List<String> = locationRepository.cities
}
