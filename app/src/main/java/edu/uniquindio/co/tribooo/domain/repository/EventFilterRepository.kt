package edu.uniquindio.co.tribooo.domain.repository

import edu.uniquindio.co.tribooo.domain.model.EventFilters
import kotlinx.coroutines.flow.StateFlow

/** Filtros de eventos compartidos: se conservan al pasar del Feed a Explorar y viceversa. */
interface EventFilterRepository {
    val filters: StateFlow<EventFilters>

    fun update(transform: (EventFilters) -> EventFilters)

    /** Vuelve a los filtros por defecto (también borra el texto de búsqueda). */
    fun clear()
}
