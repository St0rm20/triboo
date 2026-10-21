package edu.uniquindio.co.tribooo.domain.usecase.interest

import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.Interest
import edu.uniquindio.co.tribooo.domain.repository.InterestRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject

class ToggleInterestUseCase @Inject constructor(
    private val interestRepository: InterestRepository,
    private val getCurrentUser: GetCurrentUserUseCase
) {
    /** Marca o desmarca el evento como "Me interesa" para el usuario actual. */
    suspend operator fun invoke(event: Event) {
        val user = getCurrentUser() ?: return
        val isInterested = interestRepository.interests.value.any { it.user.id == user.id && it.event.id == event.id }

        if (isInterested) {
            interestRepository.delete(userId = user.id, eventId = event.id)
        } else {
            interestRepository.save(Interest(user = user, event = event))
        }
    }
}
