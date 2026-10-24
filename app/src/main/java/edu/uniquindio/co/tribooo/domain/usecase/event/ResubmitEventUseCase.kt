package edu.uniquindio.co.tribooo.domain.usecase.event

import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject
import java.time.Instant

class ResubmitEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val getCurrentUser: GetCurrentUserUseCase
) {
    /**
     * Aplica las correcciones de [edited] y devuelve el evento rechazado a PENDIENTE para que moderación lo revise
     * de nuevo (entra al final de la cola). Solo si es del usuario actual y está RECHAZADO.
     */
    suspend operator fun invoke(edited: Event, imageUrls: List<String>): RequestResult {
        val user = getCurrentUser() ?: return RequestResult.Failure("Inicia sesión para reenviar eventos.")
        val stored = eventRepository.findById(edited.id) ?: return RequestResult.Failure("El evento ya no existe.")
        if (stored.organizer.id != user.id) return RequestResult.Failure("Solo puedes reenviar tus propios eventos.")
        if (stored.status != EventStatus.RECHAZADO) {
            return RequestResult.Failure("Solo se pueden reenviar eventos rechazados.")
        }

        val resubmitted = stored.withContentOf(edited, EventStatus.PENDIENTE).copy(publicationDate = Instant.now())
        eventRepository.update(resubmitted, imageUrls)
        return RequestResult.Success("Evento reenviado a revisión.")
    }
}
