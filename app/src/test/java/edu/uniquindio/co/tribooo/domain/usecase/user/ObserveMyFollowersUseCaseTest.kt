package edu.uniquindio.co.tribooo.domain.usecase.user

import edu.uniquindio.co.tribooo.core.util.followerSubtitle
import edu.uniquindio.co.tribooo.core.util.followersSummary
import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.Follow
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.model.UserSession
import edu.uniquindio.co.tribooo.domain.repository.FollowRepository
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
import org.junit.Test
import java.time.Instant

class ObserveMyFollowersUseCaseTest {

    private class FakeFollowRepository(initial: List<Follow>) : FollowRepository {
        private val _follows = MutableStateFlow(initial)
        override val follows: StateFlow<List<Follow>> = _follows
        override suspend fun save(follow: Follow) = _follows.update { it + follow }
        override suspend fun delete(followerId: String, followedId: String) =
            _follows.update { list -> list.filterNot { it.follower.id == followerId && it.followed.id == followedId } }
    }

    private class FakeSessionRepository(private val userId: String) : SessionRepository {
        override val session: Flow<UserSession?> = flowOf(UserSession(userId, UserRole.USER))
        override suspend fun saveSession(userId: String, role: UserRole) = Unit
        override suspend fun clearSession() = Unit
    }

    private val users = UserRepositoryImpl()
    private val me = runBlocking { users.findById("u-daniela")!! }
    private val session = FakeSessionRepository(me.id)

    private fun person(id: String) = runBlocking { users.findById(id)!! }
    private val maria = person("u-vecino-0") // "María Jaramillo"
    private val camilo = person("u-vecino-1") // "Camilo Arango"
    private val felipe = person("u-vecino-2") // "Felipe López"
    private val ghost = User("ghost", "Fantasma", "g@mail.co", "123456", "Armenia", "Centro")

    private fun observe(follows: List<Follow>, repository: FakeFollowRepository = FakeFollowRepository(follows)) =
        ObserveMyFollowersUseCase(ObserveFollowsUseCase(repository), users, ObserveCurrentUserUseCase(session, users))

    @Test
    fun onlyThoseWhoFollowMeSortedByName() = runBlocking {
        val result = observe(
            listOf(
                Follow(maria, me),
                Follow(camilo, me),
                Follow(me, felipe), // yo lo sigo, él a mí no
                Follow(felipe, camilo) // ajeno
            )
        )().first()

        assertEquals(listOf("Camilo Arango", "María Jaramillo"), result.map { it.user.name })
    }

    @Test
    fun marksMutualFollows() = runBlocking {
        val result = observe(listOf(Follow(maria, me), Follow(camilo, me), Follow(me, camilo)))().first()

        assertEquals(mapOf("Camilo Arango" to true, "María Jaramillo" to false), result.associate { it.user.name to it.followedByMe })
    }

    @Test
    fun dropsFollowersThatNoLongerExist() = runBlocking {
        val result = observe(listOf(Follow(ghost, me), Follow(maria, me)))().first()

        assertEquals(listOf("u-vecino-0"), result.map { it.user.id })
    }

    @Test
    fun togglingKeepsFollowerAndChangesIndicator() = runBlocking {
        val repository = FakeFollowRepository(listOf(Follow(maria, me)))
        val observeFollowers = observe(emptyList(), repository)
        val toggle = ToggleFollowUseCase(repository, GetCurrentUserUseCase(session, users))

        toggle(maria)
        assertEquals(listOf(true), observeFollowers().first().map { it.followedByMe })
        toggle(maria)
        assertEquals(listOf(false), observeFollowers().first().map { it.followedByMe })
    }

    @Test
    fun summaryAndSubtitleTexts() {
        assertEquals("1 persona sigue tus eventos", followersSummary(1))
        assertEquals("3 personas siguen tus eventos", followersSummary(3))

        val user = User(
            "x", "X", "x@mail.co", "123456", "Armenia", "Granada",
            dateRegistered = Instant.parse("2025-03-15T15:00:00Z")
        )
        assertEquals("Granada · te sigue y lo sigues", followerSubtitle(user, followedByMe = true))
        assertEquals("Granada · desde marzo 2025", followerSubtitle(user, followedByMe = false))
    }
}
