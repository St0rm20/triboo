package edu.uniquindio.co.tribooo.domain.repository

import edu.uniquindio.co.tribooo.domain.model.ModerationReview
import kotlinx.coroutines.flow.StateFlow

interface ModerationReviewRepository {
    val reviews: StateFlow<List<ModerationReview>>
    suspend fun save(review: ModerationReview)
}
