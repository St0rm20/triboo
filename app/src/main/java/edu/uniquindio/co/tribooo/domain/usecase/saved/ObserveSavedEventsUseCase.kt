package edu.uniquindio.co.tribooo.domain.usecase.saved

import edu.uniquindio.co.tribooo.domain.model.Saved
import edu.uniquindio.co.tribooo.domain.repository.SavedEventRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class ObserveSavedEventsUseCase @Inject constructor(
    private val savedEventRepository: SavedEventRepository
) {
    operator fun invoke(): StateFlow<List<Saved>> = savedEventRepository.saved
}
