package edu.uniquindio.co.tribooo.domain.usecase.event

import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.ResourceProvider
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject

class UpdateEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val resources: ResourceProvider
) {
    /** Guarda los cambios de [edited] sobre el evento del mismo id; solo si es del usuario actual y sigue PENDIENTE. */
    suspend operator fun invoke(edited: Event, imageUrls: List<String>): RequestResult {
        val user = getCurrentUser() ?: return RequestResult.Failure(resources.getString(R.string.usecase_update_sign_in))
        val stored = eventRepository.findById(edited.id) ?: return RequestResult.Failure(resources.getString(R.string.usecase_event_not_found))
        if (stored.organizer.id != user.id) return RequestResult.Failure(resources.getString(R.string.usecase_update_not_owner))
        if (stored.status != EventStatus.PENDIENTE) {
            return RequestResult.Failure(resources.getString(R.string.usecase_update_not_pending))
        }

        eventRepository.update(stored.withContentOf(edited, EventStatus.PENDIENTE), imageUrls)
        return RequestResult.Success(resources.getString(R.string.usecase_update_success))
    }
}
