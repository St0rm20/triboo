package edu.uniquindio.co.tribooo.domain.usecase.auth

import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.AuthErrorReason
import edu.uniquindio.co.tribooo.domain.model.AuthException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterUseCaseTest {

    private val auth = FakeAuthRepository()
    private val users = UserRepositoryImpl()
    private val register = RegisterUseCase(auth, users)

    @Test
    fun createsTheAccountWithTheAuthIdAndItCanLogIn() = runBlocking {
        val user = register("  Ana Ruiz ", "Armenia", " Calle 1 ", " ana@mail.co ", "secreto1").getOrThrow()

        assertEquals("Ana Ruiz", user.name)
        assertEquals(user, users.findById(user.id))
        // El registro no deja la sesión abierta: el usuario inicia sesión después
        assertNull(auth.currentAccount)
        assertEquals(user.id, auth.login("ana@mail.co", "secreto1").uid)
    }

    @Test
    fun repeatedEmailFailsAndKeepsTheFirstAccount() = runBlocking {
        val first = register("Ana", "Armenia", "Calle 1", "ana@mail.co", "secreto1").getOrThrow()

        val second = register("Otra", "Armenia", "Calle 2", "ana@mail.co", "otraclave")

        assertTrue(second.isFailure)
        assertEquals(AuthErrorReason.EMAIL_ALREADY_IN_USE, (second.exceptionOrNull() as AuthException).reason)
        assertEquals(first.id, users.findByEmail("ana@mail.co")?.id)
    }
}
