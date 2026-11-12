package edu.uniquindio.co.tribooo.domain.usecase.waitlist

import edu.uniquindio.co.tribooo.core.util.isFull
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.repository.AttendanceRepository
import edu.uniquindio.co.tribooo.domain.repository.WaitlistRepository
import jakarta.inject.Inject

class PromoteFromWaitlistUseCase @Inject constructor(
    private val waitlistRepository: WaitlistRepository,
    private val attendanceRepository: AttendanceRepository
) {
    /** Mientras [event] tenga cupo libre, la primera persona de su lista de espera pasa a asistir. Sin cupo no hay lista. */
    suspend operator fun invoke(event: Event) {
        if (event.cupo == null) return
        while (true) {
            val attendees = attendanceRepository.attendances.value.count { it.event.id == event.id }
            if (event.isFull(attendees)) return
            val next = waitlistRepository.entries.value.queueOf(event.id).firstOrNull() ?: return
            waitlistRepository.delete(userId = next.user.id, eventId = event.id)
            attendanceRepository.save(Attendance(user = next.user, event = event))
        }
    }
}
