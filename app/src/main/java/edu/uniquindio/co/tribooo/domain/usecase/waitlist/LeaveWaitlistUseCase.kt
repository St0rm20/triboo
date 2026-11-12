package edu.uniquindio.co.tribooo.domain.usecase.waitlist

import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.repository.WaitlistRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject

class LeaveWaitlistUseCase @Inject constructor(
    private val waitlistRepository: WaitlistRepository,
    private val getCurrentUser: GetCurrentUserUseCase
) {
    /** Saca al usuario actual de la lista de espera de [event]. */
    suspend operator fun invoke(event: Event) {
        val user = getCurrentUser() ?: return
        waitlistRepository.delete(userId = user.id, eventId = event.id)
    }
}
