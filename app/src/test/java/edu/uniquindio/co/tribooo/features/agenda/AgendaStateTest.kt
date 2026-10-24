package edu.uniquindio.co.tribooo.features.agenda

import edu.uniquindio.co.tribooo.core.util.APP_ZONE
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.usecase.agenda.agendaData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

class AgendaStateTest {

    private val me = User("me", "Ana", "a@mail.co", "123456", "Armenia", "Centro")
    private val other = User("x", "Otro", "o@mail.co", "123456", "Armenia", "Centro")
    private val today = LocalDate.of(2026, 10, 21)

    private fun event(
        id: String, day: LocalDate, hour: Int = 18, endDay: LocalDate = day, status: EventStatus = EventStatus.ACTIVO
    ) = Event(
        id = id, title = id, description = "",
        startDate = day.atTime(LocalTime.of(hour, 0)).atZone(APP_ZONE).toInstant(),
        endDate = endDay.atTime(LocalTime.of(22, 0)).atZone(APP_ZONE).toInstant(),
        place = "", neighborhood = "", location = Location(0.0, 0.0), status = status,
        organizer = other, category = Category.SOCIAL
    )

    private fun state(events: List<Event>, going: List<Event>, selection: AgendaSelection = AgendaSelection.of(today)) =
        buildAgendaState(agendaData(me, going.map { Attendance(me, it) }, events), selection, today)

    private fun AgendaUiState.cell(day: Int) = cells.filterNotNull().first { it.date.dayOfMonth == day }

    @Test
    fun startsOnTodayOfTheCurrentMonth() {
        val state = state(emptyList(), emptyList())

        assertEquals("octubre 2026", state.monthLabel)
        assertTrue(state.cell(21).isSelected)
        assertTrue(state.cell(21).isToday)
        assertEquals("Miércoles 21 de octubre · sin eventos", state.selectedDayLabel)
    }

    @Test
    fun dotIsStrongOnlyWhenTheUserConfirmedAndListIsSortedByStart() {
        val late = event("tarde", today, hour = 19)
        val early = event("temprano", today, hour = 9)
        val notGoing = event("ajeno", today.plusDays(2))
        val state = state(listOf(late, early, notGoing), going = listOf(late))

        assertEquals(listOf("temprano", "tarde"), state.events.map { it.event.id })
        assertEquals(listOf(false, true), state.events.map { it.confirmed })
        assertTrue(state.cell(21).hasConfirmed)
        assertTrue(state.cell(23).hasEvents)
        assertFalse(state.cell(23).hasConfirmed)
        assertFalse(state.cell(22).hasEvents)
    }

    @Test
    fun onlyPublishedEventsAppear() {
        val pending = event("pendiente", today, status = EventStatus.PENDIENTE)

        assertTrue(state(listOf(pending), emptyList()).events.isEmpty())
    }

    @Test
    fun multiDayEventMarksEveryDay() {
        val trip = event("viaje", today.minusDays(1), endDay = today.plusDays(1))
        val state = state(listOf(trip), listOf(trip))

        assertEquals(listOf(true, true, true), listOf(20, 21, 22).map { state.cell(it).hasConfirmed })
        assertFalse(state.cell(23).hasEvents)
    }

    @Test
    fun summaryIgnoresFinishedEventsAndReactsToAttendanceChanges() {
        val upcoming = event("proximo", today.plusDays(1))
        val finished = event("terminado", today.minusDays(30), status = EventStatus.FINALIZADO)
        val events = listOf(upcoming, finished)

        assertEquals("0 eventos confirmados · 0 esta semana", state(events, emptyList()).summary)
        assertEquals("1 evento confirmado · 1 esta semana", state(events, listOf(upcoming)).summary)
        // Una asistencia a un evento ya finalizado no cuenta
        assertEquals("1 evento confirmado · 1 esta semana", state(events, listOf(upcoming, finished)).summary)
    }

    @Test
    fun changingMonthSelectsDayOneOrTodayInTheCurrentMonth() {
        val start = AgendaSelection.of(today)

        val next = start.shiftMonth(1, today)
        assertEquals(YearMonth.of(2026, 11), next.month)
        assertEquals(LocalDate.of(2026, 11, 1), next.day)

        val back = next.shiftMonth(-1, today)
        assertEquals(today, back.day)

        val previous = start.shiftMonth(-1, today)
        assertEquals(LocalDate.of(2026, 9, 1), previous.day)
        assertEquals("septiembre 2026", state(emptyList(), emptyList(), previous).monthLabel)
    }

    @Test
    fun selectingADayShowsItsEvents() {
        val next = event("proximo", today.plusDays(3))
        val selection = AgendaSelection.of(today).selectDay(today.plusDays(3))
        val state = state(listOf(next), emptyList(), selection)

        assertTrue(state.cell(24).isSelected)
        assertFalse(state.cell(21).isSelected)
        assertEquals(listOf("proximo"), state.events.map { it.event.id })
    }
}
