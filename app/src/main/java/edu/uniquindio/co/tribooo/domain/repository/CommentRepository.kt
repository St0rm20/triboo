package edu.uniquindio.co.tribooo.domain.repository

import edu.uniquindio.co.tribooo.domain.model.Comment
import kotlinx.coroutines.flow.StateFlow

interface CommentRepository {
    val comments: StateFlow<List<Comment>>

    /** Guarda el comentario; si ya existe uno con el mismo id, lo reemplaza. */
    suspend fun save(comment: Comment)

    suspend fun delete(commentId: String)
}
