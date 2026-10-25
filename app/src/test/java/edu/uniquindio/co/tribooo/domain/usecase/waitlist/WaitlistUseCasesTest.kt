package edu.uniquindio.co.tribooo.domain.usecase.waitlist

import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.model.WaitlistEntry
import edu.uniquindio.co.tribooo.domain.usecase.event.TestWorld
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class WaitlistUseCasesTest {

    private val world = TestWorld()

    private fun fullEvent(organizer: User = world.other): Event =
        world.publish("e", 10, 12, organizer = organizer, cupo = 2).also { world.fill(it, 2) }

    private fun person(id: String) = User(id, id, "$id@mail.co", "123456", "Armenia", "Centro")

    private fun queue(event: Event, vararg users: User) = runBlocking {
        users.forEachIndexed { n, user ->
            world.waitlist.save(WaitlistEntry(user, event, Instant.parse("2020-01-01T00:00:00Z").plusSeconds(n * 60L)))
        }
    }

    private fun waitingIds(event: Event) = world.waitlist.entries.value.queueOf(event.id).map { it.user.id }

    @Test
    fun joinsAFullEventAndGetsTheLastPlace() = runBlocking {
        val event = fullEvent()
        queue(event, person("a"), person("b"))

        assertEquals(3, world.joinWaitlist(event))
        assertEquals(listOf("a", "b", world.me.id), waitingIds(event))
    }

    @Test
    fun cannotJoinWhenThereIsRoom() = runBlocking {
        val event = world.publish("e", 10, 12, cupo = 5)
        world.fill(event, 2)

        assertNull(world.joinWaitlist(event))
        assertEquals(emptyList<String>(), waitingIds(event))
    }

    @Test
    fun cannotJoinAnEventWithoutLimit() = runBlocking {
        val event = world.publish("e", 10, 12)
        world.fill(event, 50)

        assertNull(world.joinWaitlist(event))
    }

    @Test
    fun cannotJoinWhenAlreadyAttending() = runBlocking {
        val event = world.publish("e", 10, 12, cupo = 2)
        world.fill(event, 1)
        world.confirm(event)

        assertNull(world.joinWaitlist(event))
    }

    @Test
    fun cannotJoinTwice() = runBlocking {
        val event = fullEvent()

        assertEquals(1, world.joinWaitlist(event))
        assertNull(world.joinWaitlist(event))
        assertEquals(listOf(world.me.id), waitingIds(event))
    }

    @Test
    fun cannotJoinYourOwnEvent() = runBlocking {
        val event = fullEvent(organizer = world.me)

        assertNull(world.joinWaitlist(event))
    }

    @Test
    fun positionFollowsJoinOrderNotListOrder() {
        val event = fullEvent()
        val late = WaitlistEntry(person("late"), event, Instant.parse("2020-01-03T00:00:00Z"))
        val early = WaitlistEntry(person("early"), event, Instant.parse("2020-01-01T00:00:00Z"))
        val otherEvent = WaitlistEntry(person("late"), world.publish("o", 1, 2), Instant.parse("2019-01-01T00:00:00Z"))
        val entries = listOf(late, early, otherEvent)

        assertEquals(1, waitlistPosition(entries, "early", event.id))
        assertEquals(2, waitlistPosition(entries, "late", event.id))
        assertNull(waitlistPosition(entries, "nobody", event.id))
    }

    @Test
    fun leavingRemovesOnlyMyEntryAndMovesOthersUp() = runBlocking {
        val event = fullEvent()
        queue(event, person("a"))
        world.joinWaitlist(event)

        world.leaveWaitlist(event)

        assertEquals(listOf("a"), waitingIds(event))
    }

    @Test
    fun cancellingAnAttendancePromotesTheFirstInLine() = runBlocking {
        val event = world.publish("e", 10, 12, cupo = 2).also { world.fill(it, 1); world.confirm(it) }
        queue(event, person("a"), person("b"))

        world.toggleAttendance(event)

        val attendees = world.attendances.attendances.value.filter { it.event.id == event.id }.map { it.user.id }
        assertEquals(true, "a" in attendees)
        assertEquals(2, attendees.size)
        assertEquals(listOf("b"), waitingIds(event))
    }

    @Test
    fun cancellingWithAnEmptyListJustFreesTheSeat() = runBlocking {
        val event = world.publish("e", 10, 12, cupo = 2).also { world.fill(it, 1); world.confirm(it) }

        world.toggleAttendance(event)

        assertEquals(1, world.attendances.attendances.value.count { it.event.id == event.id })
    }

    @Test
    fun cancellingOnAnUnlimitedEventPromotesNobody() = runBlocking {
        val event = world.publish("e", 10, 12).also { world.confirm(it) }
        queue(event, person("a"))

        world.toggleAttendance(event)

        assertEquals(0, world.attendances.attendances.value.count { it.event.id == event.id })
        assertEquals(listOf("a"), waitingIds(event))
    }
}
