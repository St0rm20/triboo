package edu.uniquindio.co.tribooo.data

import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.ImageEvent
import edu.uniquindio.co.tribooo.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Repositorio en memoria que parte de [MockData].
 * Los cambios (eventos creados, asistencias) se pierden al cerrar la app.
 */
object EventRepository {

    private val _events = MutableStateFlow(MockData.events)
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _attendances = MutableStateFlow(MockData.attendances)
    val attendances: StateFlow<List<Attendance>> = _attendances.asStateFlow()

    private val _images = MutableStateFlow<List<ImageEvent>>(emptyList())
    val images: StateFlow<List<ImageEvent>> = _images.asStateFlow()

    fun newEventId(): String = UUID.randomUUID().toString()

    /** Agrega un evento con sus imágenes (la primera es la portada). */
    fun addEvent(event: Event, imageUrls: List<String> = emptyList()) {
        _events.update { it + event }
        _images.update { current ->
            current + imageUrls.mapIndexed { index, url ->
                ImageEvent(id = UUID.randomUUID().toString(), event = event, url = url, order = index)
            }
        }
    }

    /** Confirma o cancela la asistencia del usuario al evento. */
    fun toggleAttendance(user: User, event: Event) {
        _attendances.update { current ->
            if (current.any { it.user.id == user.id && it.event.id == event.id }) {
                current.filterNot { it.user.id == user.id && it.event.id == event.id }
            } else {
                current + Attendance(user = user, event = event)
            }
        }
    }
}
