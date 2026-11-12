package edu.uniquindio.co.tribooo.data.repository.memory

import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.PlaceSuggestion
import edu.uniquindio.co.tribooo.domain.repository.LocationRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Ubicación del usuario: parte del centro de Armenia (respaldo sin permiso) y se actualiza con la que reporta el mapa. */
@Singleton
class LocationRepositoryImpl @Inject constructor() : LocationRepository {

    private val _currentLocation = MutableStateFlow(MockData.currentLocation)
    override val currentLocation: StateFlow<Location> = _currentLocation.asStateFlow()

    override val cities: List<String> = MockData.cities

    override val placeSuggestions: List<PlaceSuggestion> = MockData.placeSuggestions

    override suspend fun updateCurrentLocation(location: Location) {
        _currentLocation.value = location
    }
}
