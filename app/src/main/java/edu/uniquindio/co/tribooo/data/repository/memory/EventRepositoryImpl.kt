package edu.uniquindio.co.tribooo.data.repository.memory

import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.ImageEvent
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/** Eventos en memoria que parten de [MockData]; los cambios se pierden al cerrar la app. */
@Singleton
class EventRepositoryImpl @Inject constructor() : EventRepository {

    private val _events = MutableStateFlow(MockData.events)
    override val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _images = MutableStateFlow(MockData.images)
    override val images: StateFlow<List<ImageEvent>> = _images.asStateFlow()

    override suspend fun save(event: Event, imageUrls: List<String>) {
        _events.update { it + event }
        _images.update { current ->
            current + imageUrls.mapIndexed { index, url ->
                ImageEvent(id = UUID.randomUUID().toString(), event = event, url = url, order = index)
            }
        }
    }

    override suspend fun findById(id: String): Event? {
        return _events.value.firstOrNull { it.id == id }
    }

    override suspend fun update(event: Event) {
        _events.update { current -> current.map { if (it.id == event.id) event else it } }
        _images.update { current -> current.map { if (it.event.id == event.id) it.copy(event = event) else it } }
    }

    override suspend fun delete(eventId: String) {
        _events.update { current -> current.filterNot { it.id == eventId } }
        _images.update { current -> current.filterNot { it.event.id == eventId } }
    }
}
