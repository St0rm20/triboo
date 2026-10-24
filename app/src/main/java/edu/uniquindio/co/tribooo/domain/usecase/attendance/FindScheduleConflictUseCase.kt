package edu.uniquindio.co.tribooo.domain.usecase.attendance

import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.repository.AttendanceRepository
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject

/** Dos eventos chocan si sus rangos se solapan; si uno termina justo cuando empieza el otro, no. */
fun eventsOverlap(a: Event, b: Event): Boolean = b.startDate < a.endDate && a.startDate < b.endDate

/** De los eventos [confirmed], el primero por hora de inicio que choca con [event] (ignora el mismo y los finalizados). */
fun findScheduleConflict(event: Event, confirmed: List<Event>): Event? =
    confirmed.filter { it.id != event.id && it.status != EventStatus.FINALIZADO && eventsOverlap(event, it) }
        .minByOrNull { it.startDate }

class FindScheduleConflictUseCase @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val eventRepository: EventRepository,
    private val getCurrentUser: GetCurrentUserUseCase
) {
    /** Evento ya confirmado por el usuario actual que se cruza con [event], o null si no hay choque. */
    suspend operator fun invoke(event: Event): Event? {
        val user = getCurrentUser() ?: return null
        val attendedIds = attendanceRepository.attendances.value.filter { it.user.id == user.id }.map { it.event.id }.toSet()
        // Se usa el evento actual del repositorio: el de la asistencia puede tener un estado desactualizado
        val confirmed = eventRepository.events.value.filter { it.id in attendedIds }
        return findScheduleConflict(event, confirmed)
    }
}
