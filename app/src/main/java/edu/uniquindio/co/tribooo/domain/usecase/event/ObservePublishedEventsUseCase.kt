package edu.uniquindio.co.tribooo.domain.usecase.event

import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObservePublishedEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    /** Solo los eventos aprobados por moderación se publican en el feed. */
    operator fun invoke(): Flow<List<Event>> = eventRepository.events.map { events ->
        events.filter { it.status == EventStatus.ACTIVO }
    }
}
