package edu.uniquindio.co.tribooo.domain.usecase.comment

import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.Notification
import edu.uniquindio.co.tribooo.domain.model.NotificationType
import edu.uniquindio.co.tribooo.domain.repository.CommentRepository
import edu.uniquindio.co.tribooo.domain.repository.NotificationRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject
import java.util.UUID

class ReplyToReviewUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    private val notificationRepository: NotificationRepository,
    private val getCurrentUser: GetCurrentUserUseCase
) {
    /**
     * Publica la respuesta del organizador a una reseña. Solo puede haber una por reseña:
     * si ya existe, se reemplaza su texto. Al crear una nueva se avisa al autor de la reseña. Retorna false si no se pudo publicar.
     */
    suspend operator fun invoke(review: Comment, text: String): Boolean {
        val user = getCurrentUser() ?: return false
        if (review.event.organizer.id != user.id) return false
        val content = text.trim()
        if (content.isEmpty()) return false

        val existing = commentRepository.comments.value.firstOrNull {
            it.parentComment?.id == review.id && it.author.id == user.id
        }
        if (existing != null) {
            commentRepository.save(existing.copy(text = content))
            return true
        }
        val reply = Comment(
            id = UUID.randomUUID().toString(),
            event = review.event,
            author = user,
            text = content,
            parentComment = review
        )
        commentRepository.save(reply)
        if (review.author.id != user.id) {
            notificationRepository.save(
                Notification(
                    id = UUID.randomUUID().toString(),
                    destinationUser = review.author,
                    type = NotificationType.COMMENT,
                    reference = reply.id
                )
            )
        }
        return true
    }
}
