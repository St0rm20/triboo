package edu.uniquindio.co.tribooo.domain.usecase.event

import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.ImageEvent
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.model.UserSession
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.ObserveCurrentUserUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class ObserveMyEventsUseCaseTest {

    private class FakeEventRepository(initial: List<Event>) : EventRepository {
        override val events: StateFlow<List<Event>> = MutableStateFlow(initial)
        override val images: StateFlow<List<ImageEvent>> = MutableStateFlow(emptyList())
        override suspend fun save(event: Event, imageUrls: List<String>) = Unit
        override suspend fun findById(id: String): Event? = events.value.firstOrNull { it.id == id }
    }

    private class FakeSessionRepository(private val userId: String?) : SessionRepository {
        override val session: Flow<UserSession?> = flowOf(userId?.let { UserSession(it, UserRole.USER) })
        override suspend fun saveSession(userId: String, role: UserRole) = Unit
        override suspend fun clearSession() = Unit
    }

    private val users = UserRepositoryImpl()
    private val me = runBlocking { users.findById("u-daniela")!! }
    private val other = User("x", "Otra Persona", "x@mail.co", "123456", "Armenia", "Centro")

    private fun event(id: String, organizer: User, status: EventStatus) = Event(
        id = id, title = id, description = "", startDate = Instant.now(), endDate = Instant.now(),
        place = "", neighborhood = "", location = Location(0.0, 0.0), status = status,
        organizer = organizer, category = Category.SOCIAL
    )

    private val repository = FakeEventRepository(
        listOf(
            event("a", me, EventStatus.ACTIVO),
            event("b", me, EventStatus.PENDIENTE),
            event("c", other, EventStatus.ACTIVO),
            event("d", me, EventStatus.RECHAZADO),
            event("e", me, EventStatus.FINALIZADO)
        )
    )

    private fun observe(userId: String?) =
        ObserveMyEventsUseCase(ObserveEventsUseCase(repository), ObserveCurrentUserUseCase(FakeSessionRepository(userId), users))

    @Test
    fun returnsOnlyEventsOfCurrentUserInAnyStatus() = runBlocking {
        assertEquals(listOf("a", "b", "d", "e"), observe(me.id)().first().map { it.id })
    }

    @Test
    fun withoutSessionReturnsNothing() = runBlocking {
        assertEquals(emptyList<Event>(), observe(null)().first())
    }
}
