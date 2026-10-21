package edu.uniquindio.co.tribooo.domain.usecase.interest

import edu.uniquindio.co.tribooo.domain.model.Interest
import edu.uniquindio.co.tribooo.domain.repository.InterestRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class ObserveInterestsUseCase @Inject constructor(
    private val interestRepository: InterestRepository
) {
    operator fun invoke(): StateFlow<List<Interest>> = interestRepository.interests
}
