package edu.uniquindio.co.tribooo.data.remote.event

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import edu.uniquindio.co.tribooo.data.remote.contribution.ContributionDto
import edu.uniquindio.co.tribooo.data.remote.toGeoPoint
import edu.uniquindio.co.tribooo.data.remote.toLocation
import edu.uniquindio.co.tribooo.data.remote.toTimestamp
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.User

// Representación de un Event tal como se guarda en Firestore (colección "events").
// El organizador y la categoría se guardan por id: el repositorio los resuelve antes de llamar a toDomain().
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject()
@Keep
data class EventDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    val multidia: Boolean = false,
    val place: String = "",
    val neighborhood: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    // null = sin límite de cupos
    val cupo: Int? = null,
    // null = evento gratuito
    val contribution: ContributionDto? = null,
    val status: String = EventStatus.PENDIENTE.name,
    val verified: Boolean = false,
    val publicationDate: Timestamp = Timestamp.now(),
    val organizerId: String = "",
    val categoryId: String = "",
    // Portada del evento; el resto de imágenes vive en la colección "eventImages"
    val imageUrl: String? = null
) {
    // Convierte el DTO (datos de Firestore) al modelo de dominio con el organizador y la categoría ya resueltos
    fun toDomain(organizer: User, category: Category): Event = Event(
        id = id,
        title = title,
        description = description,
        startDate = startDate.toInstant(),
        endDate = endDate.toInstant(),
        multidia = multidia,
        place = place,
        neighborhood = neighborhood,
        location = location.toLocation(),
        cupo = cupo,
        contribution = contribution?.toDomain(),
        // Si llega un estado desconocido, el evento queda pendiente (no se publica sin moderación)
        status = runCatching { EventStatus.valueOf(status) }.getOrDefault(EventStatus.PENDIENTE),
        verified = verified,
        publicationDate = publicationDate.toInstant(),
        organizer = organizer,
        category = category,
        imageUrl = imageUrl
    )

    companion object {
        const val COLLECTION = "events"

        // Convierte el modelo de dominio al DTO que se guardará en Firestore
        fun fromDomain(event: Event): EventDto = EventDto(
            id = event.id,
            title = event.title,
            description = event.description,
            startDate = event.startDate.toTimestamp(),
            endDate = event.endDate.toTimestamp(),
            multidia = event.multidia,
            place = event.place,
            neighborhood = event.neighborhood,
            location = event.location.toGeoPoint(),
            cupo = event.cupo,
            contribution = event.contribution?.let(ContributionDto::fromDomain),
            status = event.status.name,
            verified = event.verified,
            publicationDate = event.publicationDate.toTimestamp(),
            organizerId = event.organizer.id,
            categoryId = event.category.id,
            imageUrl = event.imageUrl
        )
    }
}
