package edu.uniquindio.co.tribooo.features.eventdetail

import androidx.lifecycle.SavedStateHandle
import edu.uniquindio.co.tribooo.data.repository.memory.CommentRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.FollowRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.InterestRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.LocationRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.SavedEventRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.Contribution
import edu.uniquindio.co.tribooo.domain.model.ReportReasons
import edu.uniquindio.co.tribooo.domain.usecase.attendance.AttendanceStep
import edu.uniquindio.co.tribooo.domain.usecase.comment.AddCommentUseCase
import edu.uniquindio.co.tribooo.domain.usecase.comment.ObserveCommentsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.ObserveEventImagesUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.ObserveEventsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.TestWorld
import edu.uniquindio.co.tribooo.domain.usecase.interest.ObserveInterestsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.interest.ToggleInterestUseCase
import edu.uniquindio.co.tribooo.domain.usecase.location.ObserveCurrentLocationUseCase
import edu.uniquindio.co.tribooo.domain.usecase.report.ObserveReportedEventIdsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.report.SubmitReportUseCase
import edu.uniquindio.co.tribooo.domain.usecase.saved.ObserveSavedEventsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.saved.ToggleSavedEventUseCase
import edu.uniquindio.co.tribooo.domain.usecase.user.ObserveFollowsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.user.ToggleFollowUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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
class EventDetailViewModelTest {

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

    private fun viewModel(eventId: String): EventDetailViewModel {
        val savedEvents = SavedEventRepositoryImpl()
        return EventDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("eventId" to eventId)),
            observeEvents = ObserveEventsUseCase(world.events),
            observeEventImages = ObserveEventImagesUseCase(world.events),
            observeAttendances = world.observeAttendances,
            observeInterests = ObserveInterestsUseCase(InterestRepositoryImpl()),
            observeSavedEvents = ObserveSavedEventsUseCase(savedEvents),
            observeFollows = ObserveFollowsUseCase(FollowRepositoryImpl()),
            observeComments = ObserveCommentsUseCase(CommentRepositoryImpl()),
            observeCurrentUser = world.observeCurrentUser,
            observeCurrentLocation = ObserveCurrentLocationUseCase(LocationRepositoryImpl()),
            toggleAttendanceUseCase = world.toggleAttendance,
            toggleInterestUseCase = ToggleInterestUseCase(InterestRepositoryImpl(), world.getCurrentUser),
            toggleSavedEventUseCase = ToggleSavedEventUseCase(savedEvents, world.getCurrentUser),
            toggleFollowUseCase = ToggleFollowUseCase(FollowRepositoryImpl(), world.getCurrentUser),
            observeReportedEventIds = ObserveReportedEventIdsUseCase(world.reports, world.observeCurrentUser),
            addCommentUseCase = AddCommentUseCase(CommentRepositoryImpl(), world.getCurrentUser),
            findScheduleConflict = world.findScheduleConflict,
            submitReportUseCase = SubmitReportUseCase(world.reports, world.getCurrentUser)
        )
    }

    /** Mantiene activo el estado, que solo se calcula con un colector. */
    private fun kotlinx.coroutines.test.TestScope.started(vm: EventDetailViewModel): EventDetailViewModel {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        return vm
    }

    @Test
    fun reportSheetOpensAndCloses() = runTest {
        world.publish("e1", 10, 12)
        val vm = started(viewModel("e1"))

        vm.onAction(EventDetailAction.ShowReport)
        assertEquals(ReportDraft(), vm.uiState.value.report)

        vm.onAction(EventDetailAction.HideReport)
        assertNull(vm.uiState.value.report)
    }

    @Test
    fun sendingWithoutReasonShowsErrorAndKeepsSheetOpen() = runTest {
        world.publish("e1", 10, 12)
        val vm = started(viewModel("e1"))

        vm.onAction(EventDetailAction.ShowReport)
        vm.onAction(EventDetailAction.SendReport)

        assertEquals("Elige el motivo del reporte para poder enviarlo.", vm.uiState.value.report?.error)
        assertTrue(world.reports.reports.value.none { it.event.id == "e1" })

        vm.onAction(EventDetailAction.ReportReasonChange(ReportReasons.FALSE_INFO))
        assertNull(vm.uiState.value.report?.error)
    }

    @Test
    fun successfulReportClosesSheetAndMarksTheEvent() = runTest {
        world.publish("e1", 10, 12)
        val vm = started(viewModel("e1"))
        assertFalse(vm.uiState.value.detail!!.hasReported)

        vm.onAction(EventDetailAction.ShowReport)
        vm.onAction(EventDetailAction.ReportReasonChange(ReportReasons.SPAM_OR_DUPLICATE))
        vm.onAction(EventDetailAction.ReportDetailChange("  parece repetido "))
        vm.onAction(EventDetailAction.SendReport)

        val state = vm.uiState.value
        assertNull(state.report)
        assertEquals("Reporte enviado · moderación revisará “e1”.", state.message)
        assertTrue(state.detail!!.hasReported)
        assertEquals("parece repetido", world.reports.reports.value.single { it.event.id == "e1" }.detail)
    }

    @Test
    fun alreadyReportedEventShowsMessageInsteadOfTheSheet() = runTest {
        world.publish("e1", 10, 12)
        val vm = started(viewModel("e1"))
        vm.onAction(EventDetailAction.ShowReport)
        vm.onAction(EventDetailAction.ReportReasonChange(ReportReasons.FALSE_INFO))
        vm.onAction(EventDetailAction.SendReport)
        vm.onMessageShown()

        vm.onAction(EventDetailAction.ShowReport)

        assertNull(vm.uiState.value.report)
        assertEquals("Ya reportaste este evento · moderación está revisando el caso.", vm.uiState.value.message)
        assertEquals(1, world.reports.reports.value.count { it.event.id == "e1" && it.reporter.id == world.me.id })
    }

    @Test
    fun ownEventCannotOpenTheReportSheet() = runTest {
        world.publish("propio", 10, 12, organizer = world.me)
        val vm = started(viewModel("propio"))
        assertTrue(vm.uiState.value.detail!!.isMine)

        vm.onAction(EventDetailAction.ShowReport)

        assertNull(vm.uiState.value.report)
    }

    private fun stepOf(vm: EventDetailViewModel) = vm.uiState.value.attendanceStep

    @Test
    fun freeEventAsksToConfirmThenAttendsOnAccept() = runTest {
        world.publish("e1", 10, 12)
        val vm = started(viewModel("e1"))

        vm.onAction(EventDetailAction.ToggleAttendance)
        assertTrue(stepOf(vm) is AttendanceStep.Free)
        assertEquals(emptySet<String>(), world.attendingIds())
        assertNull(vm.uiState.value.message)

        vm.onAction(EventDetailAction.ContinueAttendance)
        assertNull(stepOf(vm))
        assertEquals(setOf("e1"), world.attendingIds())
        assertEquals("¡Listo! Tu lugar está reservado.", vm.uiState.value.message)
    }

    @Test
    fun eventWithContributionAsksForTheCost() = runTest {
        world.publish("e1", 10, 12, contribution = Contribution(5000.0))
        val vm = started(viewModel("e1"))

        vm.onAction(EventDetailAction.ToggleAttendance)

        assertEquals(5000.0, (stepOf(vm) as AttendanceStep.Cost).contribution.amount, 0.0)
    }

    @Test
    fun cancelOnTheDialogChangesNothing() = runTest {
        world.publish("e1", 10, 12)
        val vm = started(viewModel("e1"))

        vm.onAction(EventDetailAction.ToggleAttendance)
        vm.onAction(EventDetailAction.CancelAttendance)

        assertNull(stepOf(vm))
        assertEquals(emptySet<String>(), world.attendingIds())
        assertNull(vm.uiState.value.message)
    }

    @Test
    fun conflictThenCostThenConfirmInThatOrder() = runTest {
        world.confirm(world.publish("a", 10, 12))
        world.publish("b", 11, 13, contribution = Contribution(2000.0))
        val vm = started(viewModel("b"))

        vm.onAction(EventDetailAction.ToggleAttendance)
        assertEquals("a", (stepOf(vm) as AttendanceStep.Conflict).other.id)

        vm.onAction(EventDetailAction.ContinueAttendance)
        assertTrue(stepOf(vm) is AttendanceStep.Cost)
        assertEquals(setOf("a"), world.attendingIds())

        vm.onAction(EventDetailAction.ContinueAttendance)
        assertNull(stepOf(vm))
        assertEquals(setOf("a", "b"), world.attendingIds())
    }

    @Test
    fun cancellingAttendanceNeverShowsDialogs() = runTest {
        world.confirm(world.publish("a", 10, 12))
        world.confirm(world.publish("b", 11, 13, contribution = Contribution(2000.0)))
        val vm = started(viewModel("b"))
        assertTrue(vm.uiState.value.detail!!.isAttending)

        vm.onAction(EventDetailAction.ToggleAttendance)

        assertNull(stepOf(vm))
        assertEquals(setOf("a"), world.attendingIds())
        assertEquals("Cancelaste tu asistencia.", vm.uiState.value.message)
    }
}
