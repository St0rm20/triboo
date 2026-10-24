package edu.uniquindio.co.tribooo.domain.usecase.comment

import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.ImageEvent
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.model.UserSession
import edu.uniquindio.co.tribooo.domain.repository.CommentRepository
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.DeleteEventUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ReviewRepliesAndDeleteEventTest {

    private class FakeCommentRepository(initial: List<Comment>) : CommentRepository {
        private val _comments = MutableStateFlow(initial)
        override val comments: StateFlow<List<Comment>> = _comments
        override suspend fun save(comment: Comment) = _comments.update { current ->
            if (current.any { it.id == comment.id }) current.map { if (it.id == comment.id) comment else it } else current + comment
        }
        override suspend fun delete(commentId: String) = _comments.update { list -> list.filterNot { it.id == commentId } }
    }

    private class FakeEventRepository(initial: List<Event>) : EventRepository {
        private val _events = MutableStateFlow(initial)
        override val events: StateFlow<List<Event>> = _events
        override val images: StateFlow<List<ImageEvent>> = MutableStateFlow(emptyList())
        override suspend fun save(event: Event, imageUrls: List<String>) = _events.update { it + event }
        override suspend fun findById(id: String): Event? = _events.value.firstOrNull { it.id == id }
        override suspend fun delete(eventId: String) = _events.update { list -> list.filterNot { it.id == eventId } }
    }

    private class FakeSessionRepository(private val userId: String) : SessionRepository {
        override val session: Flow<UserSession?> = flowOf(UserSession(userId, UserRole.USER))
        override suspend fun saveSession(userId: String, role: UserRole) = Unit
        override suspend fun clearSession() = Unit
    }

    private val users = UserRepositoryImpl()
    private val me = runBlocking { users.findById("u-daniela")!! }
    private val other = User("x", "Otra Persona", "x@mail.co", "123456", "Armenia", "Centro")
    private val getCurrentUser = GetCurrentUserUseCase(FakeSessionRepository(me.id), users)

    private fun event(id: String, status: EventStatus, organizer: User = me) = Event(
        id = id, title = id, description = "", startDate = Instant.now(), endDate = Instant.now(),
        place = "", neighborhood = "", location = Location(0.0, 0.0), status = status,
        organizer = organizer, category = Category.SOCIAL
    )

    private val finished = event("f", EventStatus.FINALIZADO)
    private val review = Comment("r1", finished, other, "bien", rating = 5)

    private fun repliesTo(repository: FakeCommentRepository) =
        repository.comments.value.filter { it.parentComment?.id == review.id }

    @Test
    fun firstReplyIsCreatedAndSecondReplacesIt() = runBlocking {
        val repository = FakeCommentRepository(listOf(review))
        val reply = ReplyToReviewUseCase(repository, getCurrentUser)

        assertTrue(reply(review, "  Gracias  "))
        assertEquals(listOf("Gracias"), repliesTo(repository).map { it.text })
        assertEquals(null, repliesTo(repository).single().rating)

        assertTrue(reply(review, "Nos vemos pronto"))
        assertEquals(listOf("Nos vemos pronto"), repliesTo(repository).map { it.text })
    }

    @Test
    fun blankReplyOrReplyFromNonOrganizerIsRejected() = runBlocking {
        val repository = FakeCommentRepository(listOf(review))
        val reply = ReplyToReviewUseCase(repository, getCurrentUser)
        val foreignReview = Comment("r2", event("e2", EventStatus.FINALIZADO, organizer = other), other, "x", rating = 4)

        assertFalse(reply(review, "   "))
        assertFalse(reply(foreignReview, "Gracias"))
        assertEquals(emptyList<Comment>(), repliesTo(repository))
    }

    @Test
    fun deleteReplyRemovesOnlyAReplyOfTheCurrentUser() = runBlocking {
        val mine = Comment("mine", finished, me, "Gracias", parentComment = review)
        val theirs = Comment("theirs", finished, other, "Hola", parentComment = review)
        val repository = FakeCommentRepository(listOf(review, mine, theirs))
        val delete = DeleteReplyUseCase(repository, getCurrentUser)

        assertTrue(delete("mine"))
        assertFalse(delete("theirs"))
        assertFalse(delete("r1")) // una reseña no es una respuesta
        assertEquals(listOf("r1", "theirs"), repository.comments.value.map { it.id })
    }

    @Test
    fun deleteEventAllowsOnlyOwnPendingOrRejected() = runBlocking {
        val repository = FakeEventRepository(
            listOf(
                event("p", EventStatus.PENDIENTE),
                event("r", EventStatus.RECHAZADO),
                event("a", EventStatus.ACTIVO),
                finished,
                event("o", EventStatus.PENDIENTE, organizer = other)
            )
        )
        val delete = DeleteEventUseCase(repository, getCurrentUser)

        assertTrue(delete("p"))
        assertTrue(delete("r"))
        assertFalse(delete("a"))
        assertFalse(delete("f"))
        assertFalse(delete("o"))
        assertFalse(delete("missing"))
        assertEquals(listOf("a", "f", "o"), repository.events.value.map { it.id })
    }
}
