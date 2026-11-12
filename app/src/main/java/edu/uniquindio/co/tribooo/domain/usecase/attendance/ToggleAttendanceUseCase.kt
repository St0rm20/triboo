package edu.uniquindio.co.tribooo.domain.usecase.attendance

import edu.uniquindio.co.tribooo.core.util.isFull
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.repository.AttendanceRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.waitlist.PromoteFromWaitlistUseCase
import jakarta.inject.Inject

class ToggleAttendanceUseCase @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val promoteFromWaitlist: PromoteFromWaitlistUseCase
) {
    /** Confirma o cancela la asistencia del usuario actual al evento; al cancelar, el cupo liberado pasa a la lista de espera. */
    suspend operator fun invoke(event: Event) {
        val user = getCurrentUser() ?: return
        val attendees = attendanceRepository.attendances.value.filter { it.event.id == event.id }

        if (attendees.any { it.user.id == user.id }) {
            attendanceRepository.delete(userId = user.id, eventId = event.id)
            promoteFromWaitlist(event)
        } else if (!event.isFull(attendees.size)) {
            attendanceRepository.save(Attendance(user = user, event = event))
        }
    }
}
