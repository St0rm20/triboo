package edu.uniquindio.co.tribooo.domain.usecase.user

import edu.uniquindio.co.tribooo.domain.model.Follow
import edu.uniquindio.co.tribooo.domain.repository.FollowRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class ObserveFollowsUseCase @Inject constructor(
    private val followRepository: FollowRepository
) {
    operator fun invoke(): StateFlow<List<Follow>> = followRepository.follows
}
