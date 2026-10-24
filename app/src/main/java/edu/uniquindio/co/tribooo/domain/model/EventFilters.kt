package edu.uniquindio.co.tribooo.domain.model

/** Cuándo empieza el evento respecto a hoy. */
enum class WhenFilter {
    ANY,
    WEEK,
    MONTH
}

/** Distancia máxima por defecto (km); también el límite que no cuenta como filtro activo. */
const val DEFAULT_MAX_DISTANCE_KM = 8
const val MIN_DISTANCE_KM = 1
const val MAX_DISTANCE_KM = 15

/** Filtros compartidos por el Feed y Explorar. */
data class EventFilters(
    val query: String = "",
    /** Claves de las categorías elegidas; vacío = todas. */
    val categories: Set<String> = emptySet(),
    val onlyAvailable: Boolean = false,
    val whenFilter: WhenFilter = WhenFilter.ANY,
    val maxDistanceKm: Int = DEFAULT_MAX_DISTANCE_KM
) {
    /** Toma los filtros de la hoja de [draft] y conserva el texto de búsqueda. */
    fun withDraft(draft: EventFilters) = draft.copy(query = query)

    /** Filtros que cuentan en la insignia; el texto de búsqueda no. */
    val activeCount: Int
        get() = categories.size +
            (if (onlyAvailable) 1 else 0) +
            (if (whenFilter != WhenFilter.ANY) 1 else 0) +
            (if (maxDistanceKm != DEFAULT_MAX_DISTANCE_KM) 1 else 0)
}
