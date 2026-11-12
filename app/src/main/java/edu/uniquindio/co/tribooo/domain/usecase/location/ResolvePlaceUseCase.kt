package edu.uniquindio.co.tribooo.domain.usecase.location

import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.PlaceSuggestion
import edu.uniquindio.co.tribooo.domain.repository.LocationRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject
import java.text.Normalizer

const val MAX_PLACE_NAME_LENGTH = 80
const val MAX_NEIGHBORHOOD_LENGTH = 60

/** Radio máximo, en grados (~2 km), del desplazamiento que separa los lugares escritos a mano. */
private const val MAX_OFFSET_DEGREES = 0.018
private const val HALF_WORD = 0xFFFF

/** Recorta y colapsa los espacios repetidos. */
fun cleanPlaceText(text: String): String = text.trim().replace(Regex("\\s+"), " ")

/** Minúsculas y sin tildes, para comparar lo que escribe el usuario con los lugares conocidos. */
private fun normalizeForSearch(text: String): String =
    Normalizer.normalize(text, Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "").lowercase()

/** Lugares cuyo nombre o barrio contienen [query]; con [query] vacío, todos. */
fun filterPlaceSuggestions(suggestions: List<PlaceSuggestion>, query: String): List<PlaceSuggestion> {
    val text = normalizeForSearch(cleanPlaceText(query))
    if (text.isEmpty()) return suggestions
    return suggestions.filter { normalizeForSearch(it.name).contains(text) || normalizeForSearch(it.neighborhood).contains(text) }
}

/**
 * Arma el lugar del evento a partir de lo escrito; null si el nombre está vacío. Si coincide con uno de [known]
 * se conserva ese lugar con su ubicación. Si no, el barrio vacío usa [fallbackCity] y la ubicación sale de
 * [baseLocation] con un pequeño desplazamiento que depende solo del nombre.
 */
fun buildCustomPlace(
    name: String,
    neighborhood: String,
    fallbackCity: String,
    baseLocation: Location,
    known: List<PlaceSuggestion> = emptyList()
): PlaceSuggestion? {
    val cleanName = cleanPlaceText(name).take(MAX_PLACE_NAME_LENGTH).trim()
    if (cleanName.isEmpty()) return null
    val cleanNeighborhood = cleanPlaceText(neighborhood).take(MAX_NEIGHBORHOOD_LENGTH).trim().ifEmpty { fallbackCity }

    known.firstOrNull {
        normalizeForSearch(it.name) == normalizeForSearch(cleanName) &&
            normalizeForSearch(it.neighborhood) == normalizeForSearch(cleanNeighborhood)
    }?.let { return it }

    val hash = normalizeForSearch(cleanName).hashCode()
    return PlaceSuggestion(
        name = cleanName,
        neighborhood = cleanNeighborhood,
        detail = cleanNeighborhood,
        location = Location(
            latitude = baseLocation.latitude + offsetOf(hash and HALF_WORD),
            longitude = baseLocation.longitude + offsetOf((hash ushr 16) and HALF_WORD)
        )
    )
}

/** Valor de 0 a 65535 llevado al rango -[MAX_OFFSET_DEGREES]..[MAX_OFFSET_DEGREES]. */
private fun offsetOf(word: Int): Double = (word.toDouble() / HALF_WORD * 2 - 1) * MAX_OFFSET_DEGREES

class ResolvePlaceUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
    private val getCurrentUser: GetCurrentUserUseCase
) {
    /** Lugar para lo escrito en el formulario; null si el nombre está vacío. */
    suspend operator fun invoke(name: String, neighborhood: String): PlaceSuggestion? = buildCustomPlace(
        name = name,
        neighborhood = neighborhood,
        fallbackCity = getCurrentUser()?.city.orEmpty(),
        baseLocation = locationRepository.currentLocation.value,
        known = locationRepository.placeSuggestions
    )
}
