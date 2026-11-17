package edu.uniquindio.co.tribooo.domain.usecase.auth

import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.AuthAccount
import edu.uniquindio.co.tribooo.domain.model.AuthErrorReason
import edu.uniquindio.co.tribooo.domain.model.AuthException
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.model.UserSession
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginWithGoogleUseCaseTest {

    private class RecordingSession : SessionRepository {
        val current = MutableStateFlow<UserSession?>(null)
        override val session: Flow<UserSession?> = current
        override suspend fun saveSession(userId: String, role: UserRole) {
            current.value = UserSession(userId, role)
        }
        override suspend fun clearSession() {
            current.value = null
        }
    }

    private val auth = FakeAuthRepository()
    private val users = UserRepositoryImpl()
    private val session = RecordingSession()
    private val loginWithGoogle = LoginWithGoogleUseCase(auth, users, session)

    @Test
    fun existingProfileIsUsedAndTheSessionIsSaved() = runBlocking {
        auth.googleAccounts["token-daniela"] = AuthAccount(uid = "u-daniela", email = "daniela.restrepo@triboo.co")

        val user = loginWithGoogle("token-daniela").getOrThrow()

        assertEquals("Daniela Restrepo", user.name)
        assertEquals(UserSession("u-daniela", UserRole.USER), session.current.value)
    }

    @Test
    fun firstGoogleLoginCreatesTheProfileFromTheGoogleAccount() = runBlocking {
        auth.googleAccounts["token-new"] = AuthAccount(
            uid = "g-1", email = "nuevo@gmail.com", displayName = "Nuevo Vecino", photoUrl = "https://foto"
        )

        val user = loginWithGoogle("token-new").getOrThrow()

        assertEquals("Nuevo Vecino", user.name)
        assertEquals("https://foto", user.photo)
        assertEquals(user, users.findById("g-1"))
        assertEquals(UserSession("g-1", UserRole.USER), session.current.value)
    }

    @Test
    fun rejectedTokenFailsWithoutSession() = runBlocking {
        val result = loginWithGoogle("token-invalido")

        assertTrue(result.isFailure)
        assertEquals(AuthErrorReason.INVALID_CREDENTIALS, (result.exceptionOrNull() as AuthException).reason)
        assertNull(session.current.value)
    }
}
