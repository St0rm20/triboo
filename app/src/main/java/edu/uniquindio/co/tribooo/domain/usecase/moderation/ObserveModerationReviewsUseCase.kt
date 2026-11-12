package edu.uniquindio.co.tribooo.domain.usecase.moderation

import edu.uniquindio.co.tribooo.domain.model.ModerationReview
import edu.uniquindio.co.tribooo.domain.repository.ModerationReviewRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class ObserveModerationReviewsUseCase @Inject constructor(
    private val moderationReviewRepository: ModerationReviewRepository
) {
    operator fun invoke(): StateFlow<List<ModerationReview>> = moderationReviewRepository.reviews
}
