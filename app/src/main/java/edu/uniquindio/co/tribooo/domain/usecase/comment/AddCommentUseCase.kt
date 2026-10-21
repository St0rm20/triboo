package edu.uniquindio.co.tribooo.domain.usecase.comment

import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.repository.CommentRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject
import java.util.UUID

class AddCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    private val getCurrentUser: GetCurrentUserUseCase
) {
    /** Publica el comentario del usuario actual; retorna false si no se pudo publicar. */
    suspend operator fun invoke(event: Event, text: String, rating: Int?): Boolean {
        val user = getCurrentUser() ?: return false
        val content = text.trim()
        if (content.isEmpty()) return false

        commentRepository.save(
            Comment(
                id = UUID.randomUUID().toString(),
                event = event,
                author = user,
                text = content,
                rating = rating?.takeIf { it in 1..5 }
            )
        )
        return true
    }
}
