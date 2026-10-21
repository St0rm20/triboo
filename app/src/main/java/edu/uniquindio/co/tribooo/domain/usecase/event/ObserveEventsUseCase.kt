package edu.uniquindio.co.tribooo.domain.usecase.event

import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class ObserveEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    // El operador invoke permite ejecutar el caso de uso como una función: observeEventsUseCase()
    operator fun invoke(): StateFlow<List<Event>> = eventRepository.events
}
