package edu.uniquindio.co.tribooo.domain.usecase.event

import edu.uniquindio.co.tribooo.data.repository.memory.AttendanceRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.EventRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.ReportRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Contribution
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.usecase.attendance.FindScheduleConflictUseCase
import edu.uniquindio.co.tribooo.domain.usecase.attendance.ObserveAttendancesUseCase
import edu.uniquindio.co.tribooo.domain.usecase.attendance.ToggleAttendanceUseCase
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.auth.ObserveCurrentUserUseCase
import kotlinx.coroutines.runBlocking
import java.time.Instant

/** Repositorios en memoria sin datos de ejemplo, con la sesión de [me], para probar casos de uso y ViewModels. */
class TestWorld(userId: String = "u-daniela") {
    val users = UserRepositoryImpl()
    val session = FakeSessionRepository(userId)
    val events = EventRepositoryImpl()
    val attendances = AttendanceRepositoryImpl()
    val reports = ReportRepositoryImpl()

    val getCurrentUser = GetCurrentUserUseCase(session, users)
    val observeCurrentUser = ObserveCurrentUserUseCase(session, users)
    val toggleAttendance = ToggleAttendanceUseCase(attendances, getCurrentUser)
    val findScheduleConflict = FindScheduleConflictUseCase(attendances, events, getCurrentUser)
    val observeAttendances = ObserveAttendancesUseCase(attendances)

    val me: User = runBlocking { users.findById(userId)!! }
    val other = User("x", "Otra Persona", "x@mail.co", "123456", "Armenia", "Centro")

    init {
        runBlocking {
            events.events.value.forEach { events.delete(it.id) }
            attendances.attendances.value.forEach { attendances.delete(it.user.id, it.event.id) }
        }
    }

    /** Guarda un evento publicado por otra persona entre [startHour] y [endHour] del 10 de enero de 2099 (UTC). */
    fun publish(id: String, startHour: Int, endHour: Int, status: EventStatus = EventStatus.ACTIVO,
        organizer: User = other,
        contribution: Contribution? = null
    ): Event {
        val day = Instant.parse("2099-01-10T00:00:00Z")
        val event = testEvent(id, organizer, status, start = day.plusSeconds(startHour * 3600L), end = day.plusSeconds(endHour * 3600L))
            .copy(contribution = contribution)
        runBlocking { events.save(event) }
        return event
    }

    fun confirm(event: Event) = runBlocking { attendances.save(Attendance(me, event)) }

    fun attendingIds(): Set<String> = attendances.attendances.value.filter { it.user.id == me.id }.map { it.event.id }.toSet()
}
