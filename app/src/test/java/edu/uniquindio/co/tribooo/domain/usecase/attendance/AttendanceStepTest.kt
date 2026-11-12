package edu.uniquindio.co.tribooo.domain.usecase.attendance

import edu.uniquindio.co.tribooo.domain.model.Contribution
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.usecase.event.testEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AttendanceStepTest {

    private val organizer = User("x", "Otra Persona", "x@mail.co", "123456", "Armenia", "Centro")
    private val free = testEvent("free", organizer, EventStatus.ACTIVO)
    private val paid = testEvent("paid", organizer, EventStatus.ACTIVO).copy(contribution = Contribution(3000.0, "Fundación"))
    private val other = testEvent("other", organizer, EventStatus.ACTIVO)

    @Test
    fun withoutConflictTheFirstStepIsTheCostOrTheFreeOne() {
        assertTrue(startAttendanceFlow(free, null) is AttendanceStep.Free)
        assertEquals(3000.0, (startAttendanceFlow(paid, null) as AttendanceStep.Cost).contribution.amount, 0.0)
    }

    @Test
    fun withConflictItComesFirstAndThenTheCostOrFreeStep() {
        for (event in listOf(free, paid)) {
            val conflict = startAttendanceFlow(event, other)
            assertEquals(AttendanceStep.Conflict(event, other), conflict)
            assertEquals(startAttendanceFlow(event, null), nextAttendanceStep(conflict))
        }
    }

    @Test
    fun afterTheCostOrFreeDialogTheAttendanceIsConfirmed() {
        assertNull(nextAttendanceStep(startAttendanceFlow(free, null)))
        assertNull(nextAttendanceStep(startAttendanceFlow(paid, null)))
    }

    @Test
    fun declaredCauseIsNullWhenMissingOrBlank() {
        assertEquals("Fundación", Contribution(1000.0, "Fundación").declaredCause)
        assertNull(Contribution(1000.0, null).declaredCause)
        assertNull(Contribution(1000.0, "").declaredCause)
        assertNull(Contribution(1000.0, "   ").declaredCause)
    }
}
