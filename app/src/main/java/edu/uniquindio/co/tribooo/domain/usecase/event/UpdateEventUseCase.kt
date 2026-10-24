package edu.uniquindio.co.tribooo.domain.usecase.event

import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject

class UpdateEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val getCurrentUser: GetCurrentUserUseCase
) {
    /** Guarda los cambios de [edited] sobre el evento del mismo id; solo si es del usuario actual y sigue PENDIENTE. */
    suspend operator fun invoke(edited: Event, imageUrls: List<String>): RequestResult {
        val user = getCurrentUser() ?: return RequestResult.Failure("Inicia sesión para editar eventos.")
        val stored = eventRepository.findById(edited.id) ?: return RequestResult.Failure("El evento ya no existe.")
        if (stored.organizer.id != user.id) return RequestResult.Failure("Solo puedes editar tus propios eventos.")
        if (stored.status != EventStatus.PENDIENTE) {
            return RequestResult.Failure("Solo se pueden editar eventos en revisión.")
        }

        eventRepository.update(stored.withContentOf(edited, EventStatus.PENDIENTE), imageUrls)
        return RequestResult.Success("Cambios guardados. El evento sigue en revisión.")
    }
}
