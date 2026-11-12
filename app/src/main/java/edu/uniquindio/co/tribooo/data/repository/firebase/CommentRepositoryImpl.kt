package edu.uniquindio.co.tribooo.data.repository.firebase

import com.google.firebase.firestore.FirebaseFirestore
import edu.uniquindio.co.tribooo.data.remote.STOP_TIMEOUT_MILLIS
import edu.uniquindio.co.tribooo.data.remote.comment.CommentDto
import edu.uniquindio.co.tribooo.data.remote.firestoreCall
import edu.uniquindio.co.tribooo.data.remote.listenInto
import edu.uniquindio.co.tribooo.di.ApplicationScope
import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.repository.CommentRepository
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await

/** Comentarios, reseñas y respuestas del organizador (colección "comments"). */
@Singleton
class CommentRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
    userRepository: UserRepository,
    eventRepository: EventRepository,
    @ApplicationScope scope: CoroutineScope
) : CommentRepository {

    private val collection = firestore.collection(CommentDto.COLLECTION)

    // Documentos tal como los entrega el listener
    private val dtos = MutableStateFlow<List<CommentDto>>(emptyList())

    override val comments: StateFlow<List<Comment>> = combine(dtos, userRepository.users, eventRepository.events) { dtos, users, events ->
        val usersById = users.associateBy { it.id }
        val eventsById = events.associateBy { it.id }
        fun CommentDto.resolve(parent: Comment?): Comment? {
            val event = eventsById[eventId] ?: return null
            val author = usersById[authorId] ?: return null
            return toDomain(event, author, parent)
        }

        // Primero los comentarios raíz; las respuestas cuelgan de una reseña ya resuelta
        val (roots, replies) = dtos.partition { it.parentCommentId == null }
        val resolvedRoots = roots.mapNotNull { it.resolve(parent = null) }
        val rootsById = resolvedRoots.associateBy { it.id }
        resolvedRoots + replies.mapNotNull { reply -> rootsById[reply.parentCommentId]?.let { reply.resolve(it) } }
    }.stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    init {
        collection.listenInto(dtos, CommentDto::class.java) { dto, id -> dto.copy(id = id) }
    }

    // set() reemplaza el documento si ya existe un comentario con el mismo id
    override suspend fun save(comment: Comment) = firestoreCall {
        collection.document(comment.id).set(CommentDto.fromDomain(comment)).await()
        Unit
    }

    override suspend fun delete(commentId: String) = firestoreCall {
        collection.document(commentId).delete().await()
        Unit
    }
}
