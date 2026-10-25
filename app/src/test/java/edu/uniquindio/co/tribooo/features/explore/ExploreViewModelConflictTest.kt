package edu.uniquindio.co.tribooo.features.explore

import edu.uniquindio.co.tribooo.core.component.FeedEventItem
import edu.uniquindio.co.tribooo.data.repository.memory.EventFilterRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.LocationRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.Contribution
import edu.uniquindio.co.tribooo.domain.usecase.attendance.AttendanceStep
import edu.uniquindio.co.tribooo.domain.usecase.event.ObservePublishedEventsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.TestWorld
import edu.uniquindio.co.tribooo.domain.usecase.filters.ClearEventFiltersUseCase
import edu.uniquindio.co.tribooo.domain.usecase.filters.FilterEventsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.filters.ObserveEventFiltersUseCase
import edu.uniquindio.co.tribooo.domain.usecase.filters.UpdateEventFiltersUseCase
import edu.uniquindio.co.tribooo.domain.usecase.location.ObserveCurrentLocationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelConflictTest {

    private lateinit var world: TestWorld

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        world = TestWorld()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel(): ExploreViewModel {
        val filters = EventFilterRepositoryImpl()
        val vm = ExploreViewModel(
            observePublishedEvents = ObservePublishedEventsUseCase(world.events),
            observeAttendances = world.observeAttendances,
            observeCurrentUser = world.observeCurrentUser,
            observeCurrentLocation = ObserveCurrentLocationUseCase(LocationRepositoryImpl()),
            observeFilters = ObserveEventFiltersUseCase(filters),
            observeWaitlist = world.observeWaitlist,
            filterEvents = FilterEventsUseCase(),
            updateFilters = UpdateEventFiltersUseCase(filters),
            clearFiltersUseCase = ClearEventFiltersUseCase(filters),
            toggleAttendanceUseCase = world.toggleAttendance,
            findScheduleConflict = world.findScheduleConflict,
            joinWaitlist = world.joinWaitlist,
            leaveWaitlist = world.leaveWaitlist
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        return vm
    }

    private fun ExploreViewModel.item(id: String): FeedEventItem = uiState.value.events.first { it.event.id == id }

    private fun stepOf(vm: ExploreViewModel) = vm.uiState.value.attendanceStep

    @Test
    fun freeEventAsksToConfirmThenAttendsOnAccept() = runTest {
        world.publish("a", 10, 12)
        val vm = viewModel()

        vm.toggleAttendance(vm.item("a"))
        assertTrue(stepOf(vm) is AttendanceStep.Free)
        assertEquals(emptySet<String>(), world.attendingIds())

        vm.onAttendanceContinue()
        assertNull(stepOf(vm))
        assertEquals(setOf("a"), world.attendingIds())
    }

    @Test
    fun eventWithContributionAsksForTheCost() = runTest {
        world.publish("a", 10, 12, contribution = Contribution(5000.0, "Fundación"))
        val vm = viewModel()

        vm.toggleAttendance(vm.item("a"))

        val step = stepOf(vm) as AttendanceStep.Cost
        assertEquals(5000.0, step.contribution.amount, 0.0)
        vm.onAttendanceContinue()
        assertEquals(setOf("a"), world.attendingIds())
    }

    @Test
    fun cancelOnTheDialogChangesNothing() = runTest {
        world.publish("a", 10, 12)
        val vm = viewModel()

        vm.toggleAttendance(vm.item("a"))
        vm.onAttendanceCancel()

        assertNull(stepOf(vm))
        assertEquals(emptySet<String>(), world.attendingIds())
    }

    @Test
    fun conflictThenCostThenConfirmInThatOrder() = runTest {
        world.confirm(world.publish("a", 10, 12))
        world.publish("b", 11, 13, contribution = Contribution(2000.0))
        val vm = viewModel()

        vm.toggleAttendance(vm.item("b"))
        assertEquals("a", (stepOf(vm) as AttendanceStep.Conflict).other.id)

        vm.onAttendanceContinue()
        assertTrue(stepOf(vm) is AttendanceStep.Cost)
        assertEquals(setOf("a"), world.attendingIds())

        vm.onAttendanceContinue()
        assertNull(stepOf(vm))
        assertEquals(setOf("a", "b"), world.attendingIds())
    }

    @Test
    fun cancellingAtTheCostStepAfterAConflictChangesNothing() = runTest {
        world.confirm(world.publish("a", 10, 12))
        world.publish("b", 11, 13)
        val vm = viewModel()

        vm.toggleAttendance(vm.item("b"))
        vm.onAttendanceContinue()
        vm.onAttendanceCancel()

        assertNull(stepOf(vm))
        assertEquals(setOf("a"), world.attendingIds())
    }

    @Test
    fun cancellingAttendanceNeverShowsDialogs() = runTest {
        world.confirm(world.publish("a", 10, 12))
        world.confirm(world.publish("b", 11, 13, contribution = Contribution(2000.0)))
        val vm = viewModel()

        vm.toggleAttendance(vm.item("b"))

        assertNull(stepOf(vm))
        assertEquals(setOf("a"), world.attendingIds())
    }

    @Test
    fun fullEventShowsTheFourWaitlistStates() = runTest {
        val full = world.publish("full", 10, 12, cupo = 1).also { world.fill(it, 1) }
        val vm = viewModel()
        assertTrue(vm.item("full").canJoinWaitlist)

        vm.toggleAttendance(vm.item("full"))
        assertNull(stepOf(vm))
        assertEquals(1, vm.item("full").waitlistPosition)
        assertTrue(vm.item("full").isWaitlisted)
        assertEquals("Te anotamos en la lista de espera · puesto 1.", vm.uiState.value.message)

        vm.toggleAttendance(vm.item("full"))
        assertFalse(vm.item("full").isWaitlisted)
        assertEquals("Saliste de la lista de espera.", vm.uiState.value.message)
        assertEquals(emptySet<String>(), world.attendingIds())
        assertEquals(1, world.attendances.attendances.value.count { it.event.id == full.id })
    }

    @Test
    fun cancellingAFullEventShowsTheCancelState() = runTest {
        val event = world.publish("a", 10, 12, cupo = 2).also { world.fill(it, 1); world.confirm(it) }
        val vm = viewModel()
        assertTrue(vm.item("a").isAttending)
        assertFalse(vm.item("a").canJoinWaitlist)

        vm.toggleAttendance(vm.item("a"))

        assertFalse(vm.item("a").isAttending)
        assertEquals(1, world.attendances.attendances.value.count { it.event.id == event.id })
    }

    @Test
    fun ownFullEventCannotJoinItsWaitlist() = runTest {
        world.publish("mine", 10, 12, organizer = world.me, cupo = 1).also { world.fill(it, 1) }
        val vm = viewModel()

        vm.toggleAttendance(vm.item("mine"))

        assertFalse(vm.item("mine").canJoinWaitlist)
        assertNull(vm.uiState.value.message)
        assertTrue(world.waitlist.entries.value.isEmpty())
    }
}
