package edu.uniquindio.co.tribooo.domain.usecase.notification

import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.Notification
import edu.uniquindio.co.tribooo.domain.model.NotificationType
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.model.UserSession
import edu.uniquindio.co.tribooo.domain.repository.NotificationRepository
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.auth.ObserveCurrentUserUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class NotificationUseCasesTest {

    private class FakeNotificationRepository(initial: List<Notification>) : NotificationRepository {
        private val _notifications = MutableStateFlow(initial)
        override val notifications: StateFlow<List<Notification>> = _notifications

        override suspend fun save(notification: Notification) = _notifications.update { it + notification }

        override suspend fun markAsRead(id: String) =
            _notifications.update { list -> list.map { if (it.id == id) it.copy(read = true) else it } }

        override suspend fun markAllAsRead(userId: String) =
            _notifications.update { list -> list.map { if (it.destinationUser.id == userId) it.copy(read = true) else it } }
    }

    private class FakeSessionRepository(private val userId: String) : SessionRepository {
        override val session: Flow<UserSession?> = flowOf(UserSession(userId, UserRole.USER))
        override suspend fun saveSession(userId: String, role: UserRole) = Unit
        override suspend fun clearSession() = Unit
    }

    private val users = UserRepositoryImpl()
    private val me = runBlocking { users.findById("u-daniela")!! }
    private val other = User("x", "Otra Persona", "x@mail.co", "123456", "Armenia", "Centro")
    private val session = FakeSessionRepository(me.id)

    private fun notification(id: String, to: User, read: Boolean, minutesAgo: Long) = Notification(
        id = id, destinationUser = to, type = NotificationType.FOLLOWER, reference = "x", read = read,
        date = Instant.parse("2026-10-23T12:00:00Z").minusSeconds(minutesAgo * 60)
    )

    private val repository = FakeNotificationRepository(
        listOf(
            notification("old", me, read = true, minutesAgo = 300),
            notification("new", me, read = false, minutesAgo = 5),
            notification("mid", me, read = false, minutesAgo = 60),
            notification("foreign", other, read = false, minutesAgo = 1)
        )
    )
    private val observe = ObserveNotificationsUseCase(repository, ObserveCurrentUserUseCase(session, users))
    private val observeUnread = ObserveUnreadNotificationsCountUseCase(observe)

    @Test
    fun observesOnlyCurrentUserNewestFirst() = runBlocking {
        assertEquals(listOf("new", "mid", "old"), observe().first().map { it.id })
    }

    @Test
    fun countsUnreadOfCurrentUserOnly() = runBlocking {
        assertEquals(2, observeUnread().first())
    }

    @Test
    fun markingOneReadLowersTheCount() = runBlocking {
        MarkNotificationReadUseCase(repository)("new")
        assertEquals(1, observeUnread().first())
    }

    @Test
    fun markAllReadLeavesNothingPendingAndKeepsOthers() = runBlocking {
        MarkAllNotificationsReadUseCase(repository, GetCurrentUserUseCase(session, users))()

        assertEquals(0, observeUnread().first())
        assertTrue(observe().first().all { it.read })
        assertEquals(false, repository.notifications.value.first { it.id == "foreign" }.read)
    }
}
