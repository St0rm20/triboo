package edu.uniquindio.co.tribooo.domain.usecase.auth

import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RegisterUseCaseTest {

    private val users = UserRepositoryImpl()
    private val register = RegisterUseCase(users)

    @Test
    fun createsTheAccountAndItCanLogIn() = runBlocking {
        val user = register("  Ana Ruiz ", "Armenia", " Calle 1 ", " ana@mail.co ", "secreto1")

        assertNotNull(user)
        assertEquals("Ana Ruiz", user!!.name)
        assertEquals(user.id, users.login("ana@mail.co", "secreto1")?.id)
    }

    @Test
    fun repeatedEmailReturnsNullAndKeepsTheFirstAccount() = runBlocking {
        val first = register("Ana", "Armenia", "Calle 1", "ana@mail.co", "secreto1")

        assertNull(register("Otra", "Armenia", "Calle 2", "ana@mail.co", "otraclave"))
        assertEquals(first!!.id, users.findByEmail("ana@mail.co")?.id)
    }
}
