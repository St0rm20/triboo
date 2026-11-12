package edu.uniquindio.co.tribooo.domain.usecase.location

import edu.uniquindio.co.tribooo.core.util.distanceKmTo
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.repository.LocationRepository
import jakarta.inject.Inject

/** Cambio mínimo, en metros, para aceptar una nueva ubicación y no recalcular todo con cada lectura del GPS. */
const val MIN_LOCATION_CHANGE_METERS = 50.0

class UpdateCurrentLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    /** Actualiza la ubicación actual si se movió al menos [MIN_LOCATION_CHANGE_METERS]; retorna true si la cambió. */
    suspend operator fun invoke(location: Location): Boolean {
        val movedMeters = locationRepository.currentLocation.value.distanceKmTo(location) * 1000
        if (movedMeters < MIN_LOCATION_CHANGE_METERS) return false
        locationRepository.updateCurrentLocation(location)
        return true
    }
}
