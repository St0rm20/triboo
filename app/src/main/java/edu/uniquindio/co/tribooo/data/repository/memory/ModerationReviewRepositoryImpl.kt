package edu.uniquindio.co.tribooo.data.repository.memory

import edu.uniquindio.co.tribooo.domain.model.ModerationReview
import edu.uniquindio.co.tribooo.domain.repository.ModerationReviewRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Revisiones de moderación en memoria que parten de [MockData]; los cambios se pierden al cerrar la app. */
@Singleton
class ModerationReviewRepositoryImpl @Inject constructor() : ModerationReviewRepository {

    private val _reviews = MutableStateFlow(MockData.moderationReviews)
    override val reviews: StateFlow<List<ModerationReview>> = _reviews.asStateFlow()

    override suspend fun save(review: ModerationReview) {
        _reviews.update { it + review }
    }
}
