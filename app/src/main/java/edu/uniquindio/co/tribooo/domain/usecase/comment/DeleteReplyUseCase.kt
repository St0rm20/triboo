package edu.uniquindio.co.tribooo.domain.usecase.comment

import edu.uniquindio.co.tribooo.domain.repository.CommentRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject

class DeleteReplyUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    private val getCurrentUser: GetCurrentUserUseCase
) {
    /** Elimina una respuesta propia; retorna false si no existe o es de otra persona. */
    suspend operator fun invoke(replyId: String): Boolean {
        val user = getCurrentUser() ?: return false
        val reply = commentRepository.comments.value.firstOrNull { it.id == replyId } ?: return false
        if (reply.parentComment == null || reply.author.id != user.id) return false

        commentRepository.delete(replyId)
        return true
    }
}
