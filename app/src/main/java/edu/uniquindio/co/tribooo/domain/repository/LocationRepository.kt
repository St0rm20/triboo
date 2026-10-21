package edu.uniquindio.co.tribooo.domain.repository

import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.PlaceSuggestion
import kotlinx.coroutines.flow.StateFlow

interface LocationRepository {
    /** Ubicación actual del usuario; se usa para calcular distancias. */
    val currentLocation: StateFlow<Location>

    /** Ciudades disponibles en el registro. */
    val cities: List<String>

    /** Lugares sugeridos en el selector de ubicación al crear un evento. */
    val placeSuggestions: List<PlaceSuggestion>
}
