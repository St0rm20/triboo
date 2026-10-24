package edu.uniquindio.co.tribooo.domain.usecase.comment

import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.repository.CommentRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject
import java.util.UUID

class ReplyToReviewUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    private val getCurrentUser: GetCurrentUserUseCase
) {
    /**
     * Publica la respuesta del organizador a una reseña. Solo puede haber una por reseña:
     * si ya existe, se reemplaza su texto. Retorna false si no se pudo publicar.
     */
    suspend operator fun invoke(review: Comment, text: String): Boolean {
        val user = getCurrentUser() ?: return false
        if (review.event.organizer.id != user.id) return false
        val content = text.trim()
        if (content.isEmpty()) return false

        val existing = commentRepository.comments.value.firstOrNull {
            it.parentComment?.id == review.id && it.author.id == user.id
        }
        commentRepository.save(
            existing?.copy(text = content) ?: Comment(
                id = UUID.randomUUID().toString(),
                event = review.event,
                author = user,
                text = content,
                parentComment = review
            )
        )
        return true
    }
}
