package edu.uniquindio.co.tribooo.domain.usecase.event

import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.ResourceProvider
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject
import java.time.Instant

class ResubmitEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val resources: ResourceProvider
) {
    /**
     * Aplica las correcciones de [edited] y devuelve el evento rechazado a PENDIENTE para que moderación lo revise
     * de nuevo (entra al final de la cola). Solo si es del usuario actual y está RECHAZADO.
     */
    suspend operator fun invoke(edited: Event, imageUrls: List<String>): RequestResult {
        val user = getCurrentUser() ?: return RequestResult.Failure(resources.getString(R.string.usecase_resubmit_sign_in))
        val stored = eventRepository.findById(edited.id) ?: return RequestResult.Failure(resources.getString(R.string.usecase_event_not_found))
        if (stored.organizer.id != user.id) return RequestResult.Failure(resources.getString(R.string.usecase_resubmit_not_owner))
        if (stored.status != EventStatus.RECHAZADO) {
            return RequestResult.Failure(resources.getString(R.string.usecase_resubmit_not_rejected))
        }

        val resubmitted = stored.withContentOf(edited, EventStatus.PENDIENTE).copy(publicationDate = Instant.now())
        eventRepository.update(resubmitted, imageUrls)
        return RequestResult.Success(resources.getString(R.string.usecase_resubmit_success))
    }
}
