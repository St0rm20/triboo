package edu.uniquindio.co.tribooo.domain.usecase.attendance

import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.usecase.event.TestWorld
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FindScheduleConflictUseCaseTest {

    private val world = TestWorld()

    private fun conflictOf(id: String) = runBlocking { world.findScheduleConflict(world.events.findById(id)!!)?.id }

    @Test
    fun partialOverlapConflicts() {
        world.confirm(world.publish("a", 10, 12))
        world.publish("b", 11, 13)
        assertEquals("a", conflictOf("b"))
    }

    @Test
    fun containedEventConflicts() {
        world.confirm(world.publish("grande", 8, 20))
        world.publish("chico", 10, 11)
        assertEquals("grande", conflictOf("chico"))
    }

    @Test
    fun eventsTouchingAtTheEdgeDoNotConflict() {
        val a = world.publish("a", 10, 12)
        val b = world.publish("b", 12, 14)
        assertFalse(eventsOverlap(a, b))
        assertFalse(eventsOverlap(b, a))
        world.confirm(a)
        assertNull(conflictOf("b"))
    }

    @Test
    fun multiDayEventsConflictWhenSharingAnyStretch() {
        world.confirm(world.publish("largo", 10, 10 + 48))
        world.publish("segundo-dia", 10 + 30, 10 + 32)
        world.publish("despues", 10 + 49, 10 + 50)
        assertEquals("largo", conflictOf("segundo-dia"))
        assertNull(conflictOf("despues"))
    }

    @Test
    fun finishedEventsAreIgnored() {
        world.confirm(world.publish("viejo", 10, 12, status = EventStatus.FINALIZADO))
        world.publish("nuevo", 11, 13)
        assertNull(conflictOf("nuevo"))
    }

    @Test
    fun theSameEventIsIgnored() {
        world.confirm(world.publish("a", 10, 12))
        assertNull(conflictOf("a"))
    }

    @Test
    fun eventsAttendedByOthersOrNoAttendancesDoNotConflict() {
        world.publish("a", 10, 12)
        world.publish("b", 11, 13)
        assertNull(conflictOf("b"))
        runBlocking {
            world.attendances.save(edu.uniquindio.co.tribooo.domain.model.Attendance(world.other, world.events.findById("a")!!))
        }
        assertNull(conflictOf("b"))
    }

    @Test
    fun firstConflictByStartTimeWins() {
        world.confirm(world.publish("tarde", 14, 16))
        world.confirm(world.publish("temprano", 9, 11))
        val target = world.publish("largo", 10, 15)
        assertEquals("temprano", runBlocking { world.findScheduleConflict(target) }?.id)
        assertTrue(eventsOverlap(target, world.events.events.value.first { it.id == "tarde" }))
    }
}
