package edu.uniquindio.co.tribooo.domain.usecase.user

import edu.uniquindio.co.tribooo.data.repository.memory.CommentRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.FollowRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.SavedEventRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.AuthErrorReason
import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.Follow
import edu.uniquindio.co.tribooo.domain.model.Saved
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.model.UserSession
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.TestWorld
import edu.uniquindio.co.tribooo.domain.usecase.event.testEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteAccountUseCaseTest {

    /** Sesión que recuerda si se cerró. */
    private class RecordingSession(userId: String, role: UserRole = UserRole.USER) : SessionRepository {
        var cleared = false
        override val session: Flow<UserSession?> = flowOf(UserSession(userId, role))
        override suspend fun saveSession(userId: String, role: UserRole) = Unit
        override suspend fun clearSession() { cleared = true }
    }

    private val world = TestWorld()
    private val saved = SavedEventRepositoryImpl()
    private val follows = FollowRepositoryImpl()
    private val comments = CommentRepositoryImpl()

    private fun useCase(session: RecordingSession) = DeleteAccountUseCase(
        world.users, world.events, world.attendances, saved, world.waitlist, follows, comments, session,
        GetCurrentUserUseCase(session, world.users), world.auth
    )

    @Test
    fun deletesEverythingOfARegularUserAndClosesSession() = runBlocking {
        val others = world.publish("others", 10, 12)
        val mine = testEvent("mine", world.me, EventStatus.ACTIVO) // activo: DeleteEventUseCase no lo permitiría
        world.events.save(mine)
        world.attendances.save(Attendance(world.me, others))
        saved.save(Saved(world.me, others))
        follows.save(Follow(world.me, world.other))
        follows.save(Follow(world.other, world.me))
        comments.save(Comment("c1", others, world.me, "hola"))
        comments.save(Comment("c2", others, world.other, "otra persona"))
        val session = RecordingSession(world.me.id)

        assertTrue(useCase(session)())

        assertTrue(world.attendances.attendances.value.none { it.user.id == world.me.id })
        assertTrue(saved.saved.value.none { it.user.id == world.me.id })
        assertTrue(follows.follows.value.none { it.follower.id == world.me.id || it.followed.id == world.me.id })
        assertTrue(comments.comments.value.none { it.author.id == world.me.id })
        assertNotNull(comments.comments.value.firstOrNull { it.id == "c2" })
        assertTrue(world.events.events.value.none { it.organizer.id == world.me.id })
        assertNotNull(world.events.findById("others"))
        assertNull(world.users.findById(world.me.id))
        assertEquals(listOf(world.me.id), world.auth.deletedAccounts)
        assertTrue(session.cleared)
    }

    @Test
    fun whenFirebaseRefusesToDeleteTheAccountNothingIsDeleted() = runBlocking {
        val others = world.publish("others", 10, 12)
        world.attendances.save(Attendance(world.me, others))
        world.auth.failNextWith = AuthErrorReason.RECENT_LOGIN_REQUIRED
        val session = RecordingSession(world.me.id)

        assertFalse(useCase(session)())

        assertNotNull(world.users.findById(world.me.id))
        assertEquals(1, world.attendances.attendances.value.size)
        assertFalse(session.cleared)
    }

    @Test
    fun adminIsRejectedAndNothingIsDeleted() = runBlocking {
        val admin = world.users.users.value.first { it.role == UserRole.ADMIN }
        val adminWorld = TestWorld(admin.id)
        val event = adminWorld.publish("e", 10, 12)
        adminWorld.attendances.save(Attendance(admin, event))
        val session = RecordingSession(admin.id, UserRole.ADMIN)
        val delete = DeleteAccountUseCase(
            adminWorld.users, adminWorld.events, adminWorld.attendances, saved, adminWorld.waitlist, follows, comments, session,
            GetCurrentUserUseCase(session, adminWorld.users), adminWorld.auth
        )

        assertFalse(delete())

        assertNotNull(adminWorld.users.findById(admin.id))
        assertEquals(1, adminWorld.attendances.attendances.value.size)
        assertFalse(session.cleared)
        assertTrue(adminWorld.auth.deletedAccounts.isEmpty())
    }

    @Test
    fun withoutSessionReturnsFalse() = runBlocking {
        val session = object : SessionRepository {
            override val session: Flow<UserSession?> = flowOf(null)
            override suspend fun saveSession(userId: String, role: UserRole) = Unit
            override suspend fun clearSession() = Unit
        }
        val delete = DeleteAccountUseCase(
            world.users, world.events, world.attendances, saved, world.waitlist, follows, comments, session,
            GetCurrentUserUseCase(session, world.users), world.auth
        )

        assertFalse(delete())
        assertNotNull(world.users.findById(world.me.id))
    }
}
