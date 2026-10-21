package edu.uniquindio.co.tribooo.domain.usecase.saved

import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.Saved
import edu.uniquindio.co.tribooo.domain.repository.SavedEventRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject

class ToggleSavedEventUseCase @Inject constructor(
    private val savedEventRepository: SavedEventRepository,
    private val getCurrentUser: GetCurrentUserUseCase
) {
    /** Guarda o quita el evento de los guardados del usuario actual. */
    suspend operator fun invoke(event: Event) {
        val user = getCurrentUser() ?: return
        val isSaved = savedEventRepository.saved.value.any { it.user.id == user.id && it.event.id == event.id }

        if (isSaved) {
            savedEventRepository.delete(userId = user.id, eventId = event.id)
        } else {
            savedEventRepository.save(Saved(user = user, event = event))
        }
    }
}
