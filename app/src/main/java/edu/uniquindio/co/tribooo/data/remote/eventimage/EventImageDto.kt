package edu.uniquindio.co.tribooo.data.remote.eventimage

import androidx.annotation.Keep
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.ImageEvent

// Representación de un ImageEvent tal como se guarda en Firestore (colección "eventImages").
// Son las imágenes extra de la galería del evento; la portada va en EventDto.imageUrl
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject()
@Keep
data class EventImageDto(
    val id: String = "",
    val eventId: String = "",
    val url: String = "",
    // Posición en la galería, empezando en 0
    val order: Int = 0
) {
    // Convierte el DTO (datos de Firestore) al modelo de dominio con el evento ya resuelto
    fun toDomain(event: Event): ImageEvent = ImageEvent(
        id = id,
        event = event,
        url = url,
        order = order
    )

    companion object {
        const val COLLECTION = "eventImages"

        // Convierte el modelo de dominio al DTO que se guardará en Firestore
        fun fromDomain(image: ImageEvent): EventImageDto = EventImageDto(
            id = image.id,
            eventId = image.event.id,
            url = image.url,
            order = image.order
        )
    }
}
