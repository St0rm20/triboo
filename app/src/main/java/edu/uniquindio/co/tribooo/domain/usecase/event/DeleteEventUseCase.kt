package edu.uniquindio.co.tribooo.domain.usecase.event

import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.image.DeleteImagesUseCase
import jakarta.inject.Inject

class DeleteEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val deleteImages: DeleteImagesUseCase
) {
    /**
     * Elimina el evento si es del usuario actual y está pendiente o rechazado
     * (los publicados y los finalizados no se pueden borrar). Retorna false si no se pudo.
     * Sus imágenes se borran del alojamiento después del evento: si eso falla, el evento igual queda borrado.
     */
    suspend operator fun invoke(eventId: String): Boolean {
        val user = getCurrentUser() ?: return false
        val event = eventRepository.findById(eventId) ?: return false
        if (event.organizer.id != user.id) return false
        if (event.status != EventStatus.PENDIENTE && event.status != EventStatus.RECHAZADO) return false

        val imageUrls = eventRepository.findImageUrls(eventId)
        eventRepository.delete(eventId)
        deleteImages(imageUrls)
        return true
    }
}
