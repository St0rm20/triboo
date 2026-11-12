package edu.uniquindio.co.tribooo.domain.usecase.waitlist

import edu.uniquindio.co.tribooo.core.util.isFull
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.WaitlistEntry
import edu.uniquindio.co.tribooo.domain.repository.AttendanceRepository
import edu.uniquindio.co.tribooo.domain.repository.WaitlistRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject

class JoinWaitlistUseCase @Inject constructor(
    private val waitlistRepository: WaitlistRepository,
    private val attendanceRepository: AttendanceRepository,
    private val getCurrentUser: GetCurrentUserUseCase
) {
    /**
     * Anota al usuario actual en la lista de espera de [event] y devuelve su puesto.
     * Devuelve null si no corresponde: el cupo no está lleno, ya asiste, ya está en la lista o el evento es suyo.
     */
    suspend operator fun invoke(event: Event): Int? {
        val user = getCurrentUser() ?: return null
        val attendees = attendanceRepository.attendances.value.filter { it.event.id == event.id }

        val allowed = event.isFull(attendees.size) &&
            attendees.none { it.user.id == user.id } &&
            waitlistPosition(waitlistRepository.entries.value, user.id, event.id) == null &&
            event.organizer.id != user.id
        if (!allowed) return null

        waitlistRepository.save(WaitlistEntry(user = user, event = event))
        return waitlistPosition(waitlistRepository.entries.value, user.id, event.id)
    }
}
