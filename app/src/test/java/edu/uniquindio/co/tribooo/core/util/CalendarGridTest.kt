package edu.uniquindio.co.tribooo.core.util

import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

class CalendarGridTest {

    private val user = User("u", "Ana", "a@mail.co", "123456", "Armenia", "Centro")

    private fun event(start: LocalDate, end: LocalDate = start) = Event(
        id = "e", title = "Evento",
        description = "",
        startDate = start.atTime(LocalTime.of(18, 0)).atZone(APP_ZONE).toInstant(),
        endDate = end.atTime(LocalTime.of(20, 0)).atZone(APP_ZONE).toInstant(),
        place = "", neighborhood = "", location = Location(0.0, 0.0), organizer = user, category = Category.SOCIAL
    )

    @Test
    fun februaryOf2026HasTwentyEightDaysStartingOnSunday() {
        // 1 feb 2026 es domingo: seis huecos antes del día 1
        val cells = monthCells(YearMonth.of(2026, 2))

        assertEquals(6 + 28, cells.size)
        assertTrue(cells.take(6).all { it == null })
        assertEquals(LocalDate.of(2026, 2, 1), cells[6])
        assertEquals(LocalDate.of(2026, 2, 28), cells.last())
    }

    @Test
    fun monthStartingOnMondayHasNoLeadingBlanks() {
        // 1 jun 2026 es lunes y junio tiene 30 días
        val cells = monthCells(YearMonth.of(2026, 6))

        assertEquals(30, cells.size)
        assertEquals(LocalDate.of(2026, 6, 1), cells.first())
    }

    @Test
    fun monthOfThirtyOneDaysStartingOnThursday() {
        // 1 oct 2026 es jueves: tres huecos
        val cells = monthCells(YearMonth.of(2026, 10))

        assertEquals(3 + 31, cells.size)
        assertNull(cells[2])
        assertEquals(LocalDate.of(2026, 10, 1), cells[3])
    }

    @Test
    fun multiDayEventOccursOnEveryDayBetweenStartAndEnd() {
        val event = event(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 12))

        assertFalse(event.occursOn(LocalDate.of(2026, 10, 9)))
        assertTrue(event.occursOn(LocalDate.of(2026, 10, 10)))
        assertTrue(event.occursOn(LocalDate.of(2026, 10, 11)))
        assertTrue(event.occursOn(LocalDate.of(2026, 10, 12)))
        assertFalse(event.occursOn(LocalDate.of(2026, 10, 13)))
    }

    @Test
    fun thisWeekCountsEventsTouchingMondayToSunday() {
        val today = LocalDate.of(2026, 10, 21) // miércoles; semana del 19 al 25
        val events = listOf(
            event(LocalDate.of(2026, 10, 18)),                             // domingo anterior: no
            event(LocalDate.of(2026, 10, 19)),                             // lunes: sí
            event(LocalDate.of(2026, 10, 25)),                             // domingo: sí
            event(LocalDate.of(2026, 10, 26)),                             // lunes siguiente: no
            event(LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 20)), // multidía que entra: sí
            event(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31))   // multidía que la cubre: sí
        )

        assertEquals(4, countThisWeek(events, today))
    }

    @Test
    fun summaryUsesSingularAndPlural() {
        assertEquals("1 evento confirmado · 1 esta semana", agendaSummary(1, 1))
        assertEquals("0 eventos confirmados · 0 esta semana", agendaSummary(0, 0))
        assertEquals("3 eventos confirmados · 2 esta semana", agendaSummary(3, 2))
    }

    @Test
    fun selectedDayLabelCountsEvents() {
        val day = LocalDate.of(2026, 10, 5)

        assertEquals("Lunes 5 de octubre · sin eventos", selectedDayLabel(day, 0))
        assertEquals("Lunes 5 de octubre · 1 evento", selectedDayLabel(day, 1))
        assertEquals("Lunes 5 de octubre · 2 eventos", selectedDayLabel(day, 2))
        assertEquals("octubre 2026", YearMonth.of(2026, 10).label())
    }
}
