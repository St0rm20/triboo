package edu.uniquindio.co.tribooo.data.remote.comment

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import edu.uniquindio.co.tribooo.data.remote.toTimestamp
import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.User

// Representación de un Comment tal como se guarda en Firestore (colección "comments").
// Una reseña lleva rating; una respuesta del organizador lleva parentCommentId (id de la reseña que responde).
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject()
@Keep
data class CommentDto(
    val id: String = "",
    val eventId: String = "",
    val authorId: String = "",
    val text: String = "",
    val date: Timestamp = Timestamp.now(),
    // Calificación de 1 a 5; null si es un comentario sin calificación o una respuesta
    val rating: Int? = null,
    // null si es un comentario de primer nivel
    val parentCommentId: String? = null
) {
    // Convierte el DTO (datos de Firestore) al modelo de dominio con el evento, el autor y el comentario padre ya resueltos
    fun toDomain(event: Event, author: User, parentComment: Comment? = null): Comment = Comment(
        id = id,
        event = event,
        author = author,
        text = text,
        date = date.toInstant(),
        rating = rating,
        parentComment = parentComment
    )

    companion object {
        const val COLLECTION = "comments"

        // Convierte el modelo de dominio al DTO que se guardará en Firestore
        fun fromDomain(comment: Comment): CommentDto = CommentDto(
            id = comment.id,
            eventId = comment.event.id,
            authorId = comment.author.id,
            text = comment.text,
            date = comment.date.toTimestamp(),
            rating = comment.rating,
            parentCommentId = comment.parentComment?.id
        )
    }
}
