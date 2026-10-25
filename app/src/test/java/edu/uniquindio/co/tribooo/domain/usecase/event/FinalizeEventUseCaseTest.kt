package edu.uniquindio.co.tribooo.domain.usecase.event

import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class FinalizeEventUseCaseTest {

    private val users = UserRepositoryImpl()
    private val me: User = runBlocking { users.findById("u-daniela")!! }
    private val other = User("x", "Otra Persona", "x@mail.co", "123456", "Armenia", "Centro")
    private val now = Instant.parse("2099-01-10T12:00:00Z")

    private fun finalize(userId: String?, block: suspend (FinalizeEventUseCase, EventRepository) -> Unit) = runBlocking {
        val repository = emptyEventRepository()
        repository.save(testEvent("act", me, EventStatus.ACTIVO))
        repository.save(testEvent("pend", me, EventStatus.PENDIENTE))
        repository.save(testEvent("rej", me, EventStatus.RECHAZADO))
        repository.save(testEvent("done", me, EventStatus.FINALIZADO))
        repository.save(testEvent("ajeno", other, EventStatus.ACTIVO))
        block(FinalizeEventUseCase(repository, currentUserUseCase(userId)), repository)
    }

    @Test
    fun finalizesAnActiveOwnEvent() = finalize("u-daniela") { useCase, repository ->
        val result = useCase("act", now)

        assertEquals(RequestResult.Success("Evento finalizado. Lo encuentras en Mis eventos → Finalizados."), result)
        assertEquals(EventStatus.FINALIZADO, repository.findById("act")!!.status)
    }

    @Test
    fun failsWithoutSession() = finalize(null) { useCase, repository ->
        assertEquals(RequestResult.Failure("Inicia sesión para finalizar eventos."), useCase("act", now))
        assertEquals(EventStatus.ACTIVO, repository.findById("act")!!.status)
    }

    @Test
    fun failsForUnknownEvent() = finalize("u-daniela") { useCase, _ ->
        assertEquals(RequestResult.Failure("El evento ya no existe."), useCase("nope", now))
    }

    @Test
    fun failsForSomeoneElsesEvent() = finalize("u-daniela") { useCase, repository ->
        assertEquals(RequestResult.Failure("Solo puedes finalizar tus propios eventos."), useCase("ajeno", now))
        assertEquals(EventStatus.ACTIVO, repository.findById("ajeno")!!.status)
    }

    @Test
    fun failsUnlessTheEventIsActive() = finalize("u-daniela") { useCase, repository ->
        for (id in listOf("pend", "rej", "done")) {
            val before = repository.findById(id)!!
            assertEquals(id, RequestResult.Failure("Solo se pueden finalizar eventos activos."), useCase(id, now))
            assertEquals(id, before, repository.findById(id))
        }
    }

    @Test
    fun futureEndIsCutToNow() = finalize("u-daniela") { useCase, repository ->
        val stored = repository.findById("act")!!
        repository.update(stored.copy(startDate = now.minusSeconds(3600), endDate = now.plusSeconds(3600)))

        useCase("act", now)

        val saved = repository.findById("act")!!
        assertEquals(now, saved.endDate)
        assertEquals(now.minusSeconds(3600), saved.startDate)
    }

    @Test
    fun pastEndIsKept() = finalize("u-daniela") { useCase, repository ->
        val end = now.minusSeconds(600)
        val stored = repository.findById("act")!!
        repository.update(stored.copy(startDate = end.minusSeconds(3600), endDate = end))

        useCase("act", now)

        assertEquals(end, repository.findById("act")!!.endDate)
    }

    @Test
    fun notYetStartedEventKeepsStartNotAfterEnd() = finalize("u-daniela") { useCase, repository ->
        // testEvent empieza en 2099-01-10T23:00Z, después de now
        useCase("act", now)

        val saved = repository.findById("act")!!
        assertEquals(now, saved.endDate)
        assertTrue(saved.startDate <= saved.endDate)
    }

    @Test
    fun attendancesAreKept() = finalize("u-daniela") { useCase, repository ->
        val attendances = edu.uniquindio.co.tribooo.data.repository.memory.AttendanceRepositoryImpl()
        attendances.attendances.value.forEach { attendances.delete(it.user.id, it.event.id) }
        attendances.save(Attendance(other, repository.findById("act")!!))

        useCase("act", now)

        assertEquals(1, attendances.attendances.value.count { it.event.id == "act" })
    }
}
