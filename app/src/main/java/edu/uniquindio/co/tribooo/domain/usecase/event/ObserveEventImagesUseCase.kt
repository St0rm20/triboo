package edu.uniquindio.co.tribooo.domain.usecase.event

import edu.uniquindio.co.tribooo.domain.model.ImageEvent
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class ObserveEventImagesUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(): StateFlow<List<ImageEvent>> = eventRepository.images
}
