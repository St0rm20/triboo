package edu.uniquindio.co.tribooo.domain.usecase.event

import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.Contribution
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EditEventUseCasesTest {

    private val users = UserRepositoryImpl()
    private val me: User = runBlocking { users.findById("u-daniela")!! }
    private val other = User("x", "Otra Persona", "x@mail.co", "123456", "Armenia", "Centro")

    private fun edit(userId: String?, block: suspend (UpdateEventUseCase, ResubmitEventUseCase, EventRepository) -> Unit) =
        runBlocking {
            val repository = emptyEventRepository()
            repository.save(testEvent("pend", me, EventStatus.PENDIENTE), listOf("a.jpg", "b.jpg", "c.jpg"))
            repository.save(testEvent("rej", me, EventStatus.RECHAZADO))
            repository.save(testEvent("act", me, EventStatus.ACTIVO))
            repository.save(testEvent("ajeno", other, EventStatus.PENDIENTE))
            repository.save(testEvent("ajeno-rej", other, EventStatus.RECHAZADO))
            val user = currentUserUseCase(userId)
            block(UpdateEventUseCase(repository, user), ResubmitEventUseCase(repository, user), repository)
        }

    @Test
    fun updateKeepsIdOrganizerAndPending() = edit("u-daniela") { update, _, repository ->
        val stored = repository.findById("pend")!!
        val edited = stored.copy(title = "Nuevo", contribution = Contribution(2000.0), organizer = other, status = EventStatus.ACTIVO)

        val result = update(edited, listOf("z.jpg", "y.jpg"))

        assertTrue(result is RequestResult.Success)
        val saved = repository.findById("pend")!!
        assertEquals("Nuevo", saved.title)
        assertEquals(2000.0, saved.contribution?.amount)
        assertEquals(me.id, saved.organizer.id)
        assertEquals(EventStatus.PENDIENTE, saved.status)
        assertEquals(stored.publicationDate, saved.publicationDate)
        assertEquals("z.jpg", saved.imageUrl)
        assertEquals(listOf("y.jpg"), repository.images.value.filter { it.event.id == "pend" }.map { it.url })
    }

    @Test
    fun updateRejectsOtherStatesOtherOwnersAndUnknownEvents() = edit("u-daniela") { update, _, repository ->
        for (id in listOf("rej", "act", "ajeno")) {
            val event = repository.findById(id)!!
            assertTrue(id, update(event.copy(title = "X"), emptyList()) is RequestResult.Failure)
            assertEquals(id, repository.findById(id)!!.title)
        }
        assertTrue(update(testEvent("nope", me, EventStatus.PENDIENTE), emptyList()) is RequestResult.Failure)
    }

    @Test
    fun updateRequiresSession() = edit(null) { update, _, repository ->
        val event = repository.findById("pend")!!
        assertTrue(update(event.copy(title = "X"), emptyList()) is RequestResult.Failure)
    }

    @Test
    fun resubmitMovesRejectedToPendingWithSameId() = edit("u-daniela") { _, resubmit, repository ->
        val stored = repository.findById("rej")!!

        val result = resubmit(stored.copy(title = "Corregido"), emptyList())

        assertTrue(result is RequestResult.Success)
        val saved = repository.findById("rej")!!
        assertEquals("Corregido", saved.title)
        assertEquals(EventStatus.PENDIENTE, saved.status)
        assertEquals(me.id, saved.organizer.id)
        assertTrue(saved.publicationDate.isAfter(stored.publicationDate))
        assertEquals(1, repository.events.value.count { it.id == "rej" })
    }

    @Test
    fun resubmitRejectsNonRejectedAndOtherOwners() = edit("u-daniela") { _, resubmit, repository ->
        for (id in listOf("pend", "act", "ajeno-rej")) {
            val event = repository.findById(id)!!
            assertTrue(id, resubmit(event, emptyList()) is RequestResult.Failure)
        }
        assertEquals(EventStatus.RECHAZADO, repository.findById("ajeno-rej")!!.status)
    }

    @Test
    fun duplicateCreatesNewPendingEventAndLeavesOriginalUntouched() = edit("u-daniela") { _, _, repository ->
        val original = repository.findById("rej")!!

        CreateEventUseCase(repository)(original.copy(id = "copia", title = "Copia"), listOf("a.jpg"))

        assertEquals(original, repository.findById("rej"))
        assertEquals(EventStatus.PENDIENTE, repository.findById("copia")!!.status)
        assertEquals(2, repository.events.value.count { it.title == "Copia" || it.id == "rej" })
    }
}
