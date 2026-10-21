package edu.uniquindio.co.tribooo.domain.usecase.attendance

import edu.uniquindio.co.tribooo.core.util.isFull
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.repository.AttendanceRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject

class ToggleAttendanceUseCase @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val getCurrentUser: GetCurrentUserUseCase
) {
    /** Confirma o cancela la asistencia del usuario actual al evento. */
    suspend operator fun invoke(event: Event) {
        val user = getCurrentUser() ?: return
        val attendees = attendanceRepository.attendances.value.filter { it.event.id == event.id }

        if (attendees.any { it.user.id == user.id }) {
            attendanceRepository.delete(userId = user.id, eventId = event.id)
        } else if (!event.isFull(attendees.size)) {
            // Sin lista de espera en el modelo: con el cupo lleno solo se permite cancelar
            attendanceRepository.save(Attendance(user = user, event = event))
        }
    }
}
