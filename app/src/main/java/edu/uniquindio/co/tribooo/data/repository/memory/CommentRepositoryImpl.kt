package edu.uniquindio.co.tribooo.data.repository.memory

import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.repository.CommentRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class CommentRepositoryImpl @Inject constructor() : CommentRepository {

    private val _comments = MutableStateFlow(MockData.comments)
    override val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    override suspend fun save(comment: Comment) {
        _comments.update { current ->
            if (current.any { it.id == comment.id }) {
                current.map { if (it.id == comment.id) comment else it }
            } else {
                current + comment
            }
        }
    }

    override suspend fun delete(commentId: String) {
        _comments.update { current -> current.filterNot { it.id == commentId } }
    }
}
