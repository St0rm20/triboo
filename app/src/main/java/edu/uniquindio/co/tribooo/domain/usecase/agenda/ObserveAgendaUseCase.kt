package edu.uniquindio.co.tribooo.domain.usecase.agenda

import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.AttendanceRepository
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.ObserveCurrentUserUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Eventos que alimentan la agenda: los publicados y, entre ellos o no, los que el usuario confirmó. */
data class AgendaData(
    val published: List<Event> = emptyList(),
    val confirmed: List<Event> = emptyList()
)

/** Arma la agenda de [user]: publicados (ACTIVO) y confirmados que aún no han finalizado. */
fun agendaData(user: User?, attendances: List<Attendance>, events: List<Event>): AgendaData {
    // El estado se lee de la lista de eventos: la copia guardada en la asistencia puede estar vieja
    val byId = events.associateBy { it.id }
    val confirmed = attendances
        .filter { it.user.id == user?.id }
        .mapNotNull { byId[it.event.id] }
        .filter { it.status != EventStatus.FINALIZADO }
    return AgendaData(published = events.filter { it.status == EventStatus.ACTIVO }, confirmed = confirmed)
}

class ObserveAgendaUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val attendanceRepository: AttendanceRepository,
    private val observeCurrentUser: ObserveCurrentUserUseCase
) {
    operator fun invoke(): Flow<AgendaData> = combine(
        observeCurrentUser(), attendanceRepository.attendances, eventRepository.events, ::agendaData
    )
}
