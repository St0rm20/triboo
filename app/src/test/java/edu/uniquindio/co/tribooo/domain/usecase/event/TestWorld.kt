package edu.uniquindio.co.tribooo.domain.usecase.event

import edu.uniquindio.co.tribooo.data.repository.memory.AttendanceRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.EventRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.ReportRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.WaitlistRepositoryImpl
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
import edu.uniquindio.co.tribooo.domain.usecase.waitlist.JoinWaitlistUseCase
import edu.uniquindio.co.tribooo.domain.usecase.waitlist.LeaveWaitlistUseCase
import edu.uniquindio.co.tribooo.domain.usecase.waitlist.ObserveWaitlistUseCase
import edu.uniquindio.co.tribooo.domain.usecase.waitlist.PromoteFromWaitlistUseCase
import kotlinx.coroutines.runBlocking
import java.time.Instant

/** Repositorios en memoria sin datos de ejemplo, con la sesión de [me], para probar casos de uso y ViewModels. */
class TestWorld(userId: String = "u-daniela") {
    val users = UserRepositoryImpl()
    val session = FakeSessionRepository(userId)
    val events = EventRepositoryImpl()
    val attendances = AttendanceRepositoryImpl()
    val waitlist = WaitlistRepositoryImpl()
    val reports = ReportRepositoryImpl()

    val getCurrentUser = GetCurrentUserUseCase(session, users)
    val promoteFromWaitlist = PromoteFromWaitlistUseCase(waitlist, attendances)
    val observeCurrentUser = ObserveCurrentUserUseCase(session, users)
    val joinWaitlist = JoinWaitlistUseCase(waitlist, attendances, getCurrentUser)
    val leaveWaitlist = LeaveWaitlistUseCase(waitlist, getCurrentUser)
    val observeWaitlist = ObserveWaitlistUseCase(waitlist)
    val toggleAttendance = ToggleAttendanceUseCase(attendances, getCurrentUser, promoteFromWaitlist)
    val findScheduleConflict = FindScheduleConflictUseCase(attendances, events, getCurrentUser)
    val observeAttendances = ObserveAttendancesUseCase(attendances)

    val me: User = runBlocking { users.findById(userId)!! }
    val other = User("x", "Otra Persona", "x@mail.co", "123456", "Armenia", "Centro")

    init {
        runBlocking {
            events.events.value.forEach { events.delete(it.id) }
            attendances.attendances.value.forEach { attendances.delete(it.user.id, it.event.id) }
            waitlist.entries.value.forEach { waitlist.delete(it.user.id, it.event.id) }
        }
    }

    /** Guarda un evento publicado por otra persona entre [startHour] y [endHour] del 10 de enero de 2099 (UTC). */
    fun publish(id: String, startHour: Int, endHour: Int, status: EventStatus = EventStatus.ACTIVO,
        organizer: User = other,
        contribution: Contribution? = null,
        cupo: Int? = null
    ): Event {
        val day = Instant.parse("2099-01-10T00:00:00Z")
        val event = testEvent(id, organizer, status, start = day.plusSeconds(startHour * 3600L), end = day.plusSeconds(endHour * 3600L))
            .copy(contribution = contribution, cupo = cupo)
        runBlocking { events.save(event) }
        return event
    }

    /** Llena el cupo de [event] con asistentes de relleno hasta que haya [count]. */
    fun fill(event: Event, count: Int) = runBlocking {
        repeat(count) { n -> attendances.save(Attendance(User("fill-${event.id}-$n", "Relleno $n", "r$n@mail.co", "123456", "Armenia", "Centro"), event)) }
    }

    fun confirm(event: Event) = runBlocking { attendances.save(Attendance(me, event)) }

    fun attendingIds(): Set<String> = attendances.attendances.value.filter { it.user.id == me.id }.map { it.event.id }.toSet()
}
