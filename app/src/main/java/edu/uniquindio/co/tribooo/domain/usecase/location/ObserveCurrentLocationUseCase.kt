package edu.uniquindio.co.tribooo.domain.usecase.location

import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.repository.LocationRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class ObserveCurrentLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    operator fun invoke(): StateFlow<Location> = locationRepository.currentLocation
}
