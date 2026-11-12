package edu.uniquindio.co.tribooo.domain.usecase.comment

import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.Notification
import edu.uniquindio.co.tribooo.domain.model.NotificationType
import edu.uniquindio.co.tribooo.domain.repository.CommentRepository
import edu.uniquindio.co.tribooo.domain.repository.NotificationRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject
import java.util.UUID

class AddCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    private val notificationRepository: NotificationRepository,
    private val getCurrentUser: GetCurrentUserUseCase
) {
    /** Publica el comentario del usuario actual y avisa al organizador (salvo que sea el propio autor); retorna false si no se pudo publicar. */
    suspend operator fun invoke(event: Event, text: String, rating: Int?): Boolean {
        val user = getCurrentUser() ?: return false
        val content = text.trim()
        if (content.isEmpty()) return false

        val comment = Comment(
            id = UUID.randomUUID().toString(),
            event = event,
            author = user,
            text = content,
            rating = rating?.takeIf { it in 1..5 }
        )
        commentRepository.save(comment)
        if (event.organizer.id != user.id) {
            notificationRepository.save(
                Notification(
                    id = UUID.randomUUID().toString(),
                    destinationUser = event.organizer,
                    type = NotificationType.COMMENT,
                    reference = comment.id
                )
            )
        }
        return true
    }
}
