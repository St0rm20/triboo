package edu.uniquindio.co.tribooo.domain.usecase.event

import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import jakarta.inject.Inject

class GetEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: String): Event? = eventRepository.findById(eventId)
}
