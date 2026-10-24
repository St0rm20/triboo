package edu.uniquindio.co.tribooo.domain.repository

import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.ImageEvent
import kotlinx.coroutines.flow.StateFlow

interface EventRepository {
    val events: StateFlow<List<Event>>

    /** Galería de los eventos (la portada va en [Event.imageUrl]). */
    val images: StateFlow<List<ImageEvent>>

    /** Guarda el evento con sus imágenes; la primera es la portada. */
    suspend fun save(event: Event, imageUrls: List<String> = emptyList())
    suspend fun findById(id: String): Event?

    /** Elimina el evento y sus imágenes. */
    suspend fun delete(eventId: String)
}
