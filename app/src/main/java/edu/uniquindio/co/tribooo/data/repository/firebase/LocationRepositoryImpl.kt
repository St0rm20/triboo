package edu.uniquindio.co.tribooo.data.repository.firebase

import com.google.firebase.firestore.FirebaseFirestore
import edu.uniquindio.co.tribooo.data.remote.city.CityDto
import edu.uniquindio.co.tribooo.data.remote.listenInto
import edu.uniquindio.co.tribooo.data.remote.placesuggestion.PlaceSuggestionDto
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.PlaceSuggestion
import edu.uniquindio.co.tribooo.domain.repository.LocationRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Centro de Armenia: ubicación de respaldo mientras el mapa no reporta la real (o no hay permiso). */
private val DEFAULT_LOCATION = Location(latitude = 4.5339, longitude = -75.6811)

/**
 * Catálogos de ciudades y lugares sugeridos (colecciones "cities" y "placeSuggestions").
 * La ubicación actual es del dispositivo: no se guarda en Firestore.
 */
@Singleton
class LocationRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore
) : LocationRepository {

    private val _currentLocation = MutableStateFlow(DEFAULT_LOCATION)
    override val currentLocation: StateFlow<Location> = _currentLocation.asStateFlow()

    private val cityDtos = MutableStateFlow<List<CityDto>>(emptyList())
    private val placeDtos = MutableStateFlow<List<PlaceSuggestionDto>>(emptyList())

    // Se leen de lo que ya trajo el listener; vacíos hasta que llega la primera respuesta
    override val cities: List<String>
        get() = cityDtos.value.map { it.toDomain() }.sorted()

    override val placeSuggestions: List<PlaceSuggestion>
        get() = placeDtos.value.map { it.toDomain() }

    init {
        firestore.collection(CityDto.COLLECTION).listenInto(cityDtos, CityDto::class.java) { dto, id -> dto.copy(id = id) }
        firestore.collection(PlaceSuggestionDto.COLLECTION)
            .listenInto(placeDtos, PlaceSuggestionDto::class.java) { dto, id -> dto.copy(id = id) }
    }

    override suspend fun updateCurrentLocation(location: Location) {
        _currentLocation.value = location
    }
}
