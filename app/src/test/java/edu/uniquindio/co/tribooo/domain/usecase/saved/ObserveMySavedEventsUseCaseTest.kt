package edu.uniquindio.co.tribooo.domain.usecase.saved

import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.ImageEvent
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.Saved
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.model.UserSession
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.SavedEventRepository
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.auth.ObserveCurrentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.ObserveEventsUseCase
import edu.uniquindio.co.tribooo.features.saved.savedSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class ObserveMySavedEventsUseCaseTest {

    private class FakeEventRepository(initial: List<Event>) : EventRepository {
        override val events: StateFlow<List<Event>> = MutableStateFlow(initial)
        override val images: StateFlow<List<ImageEvent>> = MutableStateFlow(emptyList())
        override suspend fun save(event: Event, imageUrls: List<String>) = Unit
        override suspend fun findById(id: String): Event? = events.value.firstOrNull { it.id == id }
        override suspend fun update(event: Event, imageUrls: List<String>?) = Unit
        override suspend fun delete(eventId: String) = Unit
    }

    private class FakeSavedRepository(initial: List<Saved>) : SavedEventRepository {
        private val _saved = MutableStateFlow(initial)
        override val saved: StateFlow<List<Saved>> = _saved
        override suspend fun save(saved: Saved) = _saved.update { it + saved }
        override suspend fun delete(userId: String, eventId: String) =
            _saved.update { list -> list.filterNot { it.user.id == userId && it.event.id == eventId } }
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

    private fun event(id: String, startDay: Int, status: EventStatus = EventStatus.ACTIVO, title: String = id) = Event(
        id = id, title = title, description = "", startDate = Instant.parse("2026-11-%02dT12:00:00Z".format(startDay)),
        endDate = Instant.parse("2026-11-%02dT14:00:00Z".format(startDay)), place = "", neighborhood = "",
        location = Location(0.0, 0.0), status = status, organizer = other, category = Category.SOCIAL
    )

    private val late = event("late", 20)
    private val early = event("early", 5)
    private val finished = event("finished", 10, EventStatus.FINALIZADO)
    private val foreign = event("foreign", 8)

    private fun observe(saved: List<Saved>, events: List<Event>): ObserveMySavedEventsUseCase {
        val savedRepo = FakeSavedRepository(saved)
        return ObserveMySavedEventsUseCase(
            ObserveSavedEventsUseCase(savedRepo),
            ObserveEventsUseCase(FakeEventRepository(events)),
            ObserveCurrentUserUseCase(session, users)
        )
    }

    @Test
    fun onlyMineSortedByStartAndWithoutStatusFilter() = runBlocking {
        val result = observe(
            saved = listOf(Saved(me, late), Saved(other, foreign), Saved(me, finished), Saved(me, early)),
            events = listOf(late, early, finished, foreign)
        )().first()

        assertEquals(listOf("early", "finished", "late"), result.map { it.id })
    }

    @Test
    fun showsCurrentVersionAndDropsMissingEvents() = runBlocking {
        val stale = event("early", 5, title = "Título viejo")
        val updated = event("early", 5, title = "Título nuevo")

        val result = observe(
            saved = listOf(Saved(me, stale), Saved(me, late)),
            events = listOf(updated)
        )().first()

        assertEquals(listOf("Título nuevo"), result.map { it.title })
    }

    @Test
    fun toggleRemovesAndRestoresSavedEvent() = runBlocking {
        val savedRepo = FakeSavedRepository(listOf(Saved(me, early)))
        val observeMine = ObserveMySavedEventsUseCase(
            ObserveSavedEventsUseCase(savedRepo),
            ObserveEventsUseCase(FakeEventRepository(listOf(early))),
            ObserveCurrentUserUseCase(session, users)
        )
        val toggle = ToggleSavedEventUseCase(savedRepo, GetCurrentUserUseCase(session, users))

        toggle(early)
        assertEquals(emptyList<Event>(), observeMine().first())
        toggle(early)
        assertEquals(listOf("early"), observeMine().first().map { it.id })
    }

    @Test
    fun summaryTexts() {
        assertEquals("Tu lista para decidir después", savedSummary(0))
        assertEquals("1 evento guardado", savedSummary(1))
        assertEquals("3 eventos guardados", savedSummary(3))
    }
}
