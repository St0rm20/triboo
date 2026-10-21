package edu.uniquindio.co.tribooo.domain.usecase.event

import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import jakarta.inject.Inject

class CreateEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    /** Todo evento nuevo pasa por moderación antes de aparecer en el feed. */
    suspend operator fun invoke(event: Event, imageUrls: List<String>) {
        eventRepository.save(event.copy(status = EventStatus.PENDIENTE), imageUrls)
    }
}
