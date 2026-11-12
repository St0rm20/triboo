package edu.uniquindio.co.tribooo.domain.usecase.waitlist

import edu.uniquindio.co.tribooo.domain.model.WaitlistEntry
import edu.uniquindio.co.tribooo.domain.repository.WaitlistRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveWaitlistUseCase @Inject constructor(
    private val waitlistRepository: WaitlistRepository
) {
    operator fun invoke(): Flow<List<WaitlistEntry>> = waitlistRepository.entries
}
