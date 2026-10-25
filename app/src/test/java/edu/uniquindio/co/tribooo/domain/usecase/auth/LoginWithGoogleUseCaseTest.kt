package edu.uniquindio.co.tribooo.domain.usecase.auth

import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.model.UserSession
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
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

    @Test
    fun savesTheSessionOfTheSeededUserAccount() = runBlocking {
        val session = RecordingSession()

        val user = LoginWithGoogleUseCase(UserRepositoryImpl(), session)()

        assertEquals("Daniela Restrepo", user?.name)
        assertEquals(UserSession("u-daniela", UserRole.USER), session.current.value)
    }
}
