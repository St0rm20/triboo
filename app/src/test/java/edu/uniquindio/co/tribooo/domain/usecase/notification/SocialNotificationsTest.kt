package edu.uniquindio.co.tribooo.domain.usecase.notification

import edu.uniquindio.co.tribooo.data.repository.memory.CommentRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.FollowRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.NotificationRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.NotificationType
import edu.uniquindio.co.tribooo.domain.usecase.comment.AddCommentUseCase
import edu.uniquindio.co.tribooo.domain.usecase.comment.ReplyToReviewUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.TestWorld
import edu.uniquindio.co.tribooo.domain.usecase.event.testEvent
import edu.uniquindio.co.tribooo.domain.usecase.user.ToggleFollowUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SocialNotificationsTest {

    private val world = TestWorld()
    private val notifications = NotificationRepositoryImpl()
    private val comments = CommentRepositoryImpl()
    private val follows = FollowRepositoryImpl()

    /** Solo las notificaciones creadas durante la prueba (excluye los datos de ejemplo). */
    private val baseline = notifications.notifications.value.map { it.id }.toSet()
    private fun created() = notifications.notifications.value.filter { it.id !in baseline }

    @Test
    fun commentingOthersEventNotifiesOrganizer() = runBlocking {
        val event = world.publish("e1", 10, 12)
        assertTrue(AddCommentUseCase(comments, notifications, world.getCurrentUser)(event, "Qué buen plan", 5))

        val n = created().single()
        assertEquals(NotificationType.COMMENT, n.type)
        assertEquals(event.organizer.id, n.destinationUser.id)
        assertEquals(comments.comments.value.single { it.event.id == "e1" }.id, n.reference)
    }

    @Test
    fun commentingOwnEventDoesNotNotify() = runBlocking {
        val event = testEvent("own", world.me, EventStatus.ACTIVO)
        assertTrue(AddCommentUseCase(comments, notifications, world.getCurrentUser)(event, "Nota propia", null))

        assertTrue(created().isEmpty())
    }

    @Test
    fun replyingNotifiesReviewAuthorButEditingDoesNot() = runBlocking {
        val event = testEvent("mine", world.me, EventStatus.FINALIZADO)
        val review = Comment("r1", event, world.other, "bien", rating = 5)
        comments.save(review)
        val reply = ReplyToReviewUseCase(comments, notifications, world.getCurrentUser)

        assertTrue(reply(review, "Gracias"))
        val n = created().single()
        assertEquals(NotificationType.COMMENT, n.type)
        assertEquals(world.other.id, n.destinationUser.id)
        assertEquals(comments.comments.value.single { it.parentComment?.id == "r1" }.id, n.reference)

        assertTrue(reply(review, "Gracias de nuevo"))
        assertEquals(1, created().size)
    }

    @Test
    fun followingNotifiesButUnfollowingDoesNot() = runBlocking {
        val toggle = ToggleFollowUseCase(follows, notifications, world.getCurrentUser)

        toggle(world.other)
        val n = created().single()
        assertEquals(NotificationType.FOLLOWER, n.type)
        assertEquals(world.other.id, n.destinationUser.id)
        assertEquals(world.me.id, n.reference)

        toggle(world.other) // deja de seguir
        assertEquals(1, created().size)
    }
}
