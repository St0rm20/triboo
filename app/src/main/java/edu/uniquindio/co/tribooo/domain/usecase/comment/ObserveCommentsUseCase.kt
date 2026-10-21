package edu.uniquindio.co.tribooo.domain.usecase.comment

import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.repository.CommentRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class ObserveCommentsUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    operator fun invoke(): StateFlow<List<Comment>> = commentRepository.comments
}
