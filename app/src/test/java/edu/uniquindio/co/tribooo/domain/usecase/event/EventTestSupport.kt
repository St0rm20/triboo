package edu.uniquindio.co.tribooo.domain.usecase.event

import edu.uniquindio.co.tribooo.data.repository.memory.EventRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.model.UserSession
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.Instant

/** Sesión fija para probar casos de uso que dependen del usuario actual. */
class FakeSessionRepository(private val userId: String?) : SessionRepository {
    override val session: Flow<UserSession?> = flowOf(userId?.let { UserSession(it, UserRole.USER) })
    override suspend fun saveSession(userId: String, role: UserRole) = Unit
    override suspend fun clearSession() = Unit
}

fun currentUserUseCase(userId: String?) = GetCurrentUserUseCase(FakeSessionRepository(userId), UserRepositoryImpl())

/** Repositorio real en memoria pero sin los eventos de [edu.uniquindio.co.tribooo.data.repository.memory.MockData]. */
suspend fun emptyEventRepository(): EventRepositoryImpl {
    val repository = EventRepositoryImpl()
    repository.events.value.forEach { repository.delete(it.id) }
    return repository
}

fun testEvent(
    id: String,
    organizer: User,
    status: EventStatus,
    start: Instant = Instant.parse("2099-01-10T23:00:00Z"),
    end: Instant = start.plusSeconds(2 * 3600),
    title: String = id
) = Event(
    id = id, title = title, description = "desc", startDate = start, endDate = end,
    place = "Parque", neighborhood = "Centro", location = Location(4.5339, -75.6811), status = status,
    organizer = organizer, category = Category.SOCIAL
)
