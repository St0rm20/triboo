package edu.uniquindio.co.tribooo.features.profile

import edu.uniquindio.co.tribooo.core.util.FormTexts
import edu.uniquindio.co.tribooo.data.repository.memory.CommentRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.FollowRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.LocationRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.ModerationReviewRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.NotificationRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.SavedEventRepositoryImpl
import edu.uniquindio.co.tribooo.domain.usecase.auth.LogoutUseCase
import edu.uniquindio.co.tribooo.domain.usecase.comment.ObserveCommentsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.ObserveEventsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.TestWorld
import edu.uniquindio.co.tribooo.domain.usecase.location.GetCitiesUseCase
import edu.uniquindio.co.tribooo.domain.usecase.moderation.ObserveModerationCountsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.notification.ObserveNotificationsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.notification.ObserveUnreadNotificationsCountUseCase
import edu.uniquindio.co.tribooo.domain.usecase.saved.ObserveSavedEventsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.user.ObserveFollowsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.user.UpdateProfileUseCase
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
class EditProfileFormTest {

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

    private fun TestScope.viewModel(): ProfileViewModel {
        val vm = ProfileViewModel(
            observeCurrentUser = world.observeCurrentUser,
            observeEvents = ObserveEventsUseCase(world.events),
            observeAttendances = world.observeAttendances,
            observeFollows = ObserveFollowsUseCase(FollowRepositoryImpl()),
            observeSavedEvents = ObserveSavedEventsUseCase(SavedEventRepositoryImpl()),
            observeComments = ObserveCommentsUseCase(CommentRepositoryImpl()),
            observeUnreadNotifications = ObserveUnreadNotificationsCountUseCase(
                ObserveNotificationsUseCase(NotificationRepositoryImpl(), world.observeCurrentUser)
            ),
            observeModerationCounts = ObserveModerationCountsUseCase(world.events, world.reports, ModerationReviewRepositoryImpl()),
            getCities = GetCitiesUseCase(LocationRepositoryImpl()),
            updateProfileUseCase = UpdateProfileUseCase(world.users),
            logoutUseCase = LogoutUseCase(world.session)
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        return vm
    }

    private fun ProfileViewModel.openEmpty() {
        onEditClick()
        onNameChange("")
        onNeighborhoodChange("")
    }

    @Test
    fun openingTheFormShowsNoErrors() = runTest {
        val vm = viewModel()
        vm.onEditClick()

        assertNull(vm.uiState.value.nameError)
        assertNull(vm.uiState.value.neighborhoodError)
        assertFalse(vm.uiState.value.showInvalidNotice)
    }

    @Test
    fun emptyFormSaveShowsAllErrorsAtOnceAndKeepsTheSheetOpen() = runTest {
        val vm = viewModel()
        vm.openEmpty()

        vm.onSaveEdit()

        val state = vm.uiState.value
        assertEquals(FormTexts.NAME_REQUIRED, state.nameError)
        assertEquals(FormTexts.NEIGHBORHOOD_REQUIRED, state.neighborhoodError)
        assertTrue(state.showInvalidNotice)
        assertEquals(EditProfileField.NAME, state.editFocusField)
        assertTrue(state.showEditDialog)
        assertNull(state.snackbarMessage)
    }

    @Test
    fun touchedEmptyFieldShowsItsErrorAndUntouchedDoesNot() = runTest {
        val vm = viewModel()
        vm.openEmpty()

        vm.onEditFieldFocusLost(EditProfileField.NAME)

        assertEquals(FormTexts.NAME_REQUIRED, vm.uiState.value.nameError)
        assertNull(vm.uiState.value.neighborhoodError)
    }

    @Test
    fun fixingOneFieldClearsOnlyItsErrorAndFocusMovesToTheNextOne() = runTest {
        val vm = viewModel()
        vm.openEmpty()
        vm.onSaveEdit()

        vm.onNameChange("Daniela Restrepo")

        assertNull(vm.uiState.value.nameError)
        assertEquals(FormTexts.NEIGHBORHOOD_REQUIRED, vm.uiState.value.neighborhoodError)
        vm.onEditFocusHandled()
        vm.onSaveEdit()
        assertEquals(EditProfileField.NEIGHBORHOOD, vm.uiState.value.editFocusField)
    }

    @Test
    fun shortNameKeepsTheThreeCharacterThreshold() = runTest {
        val vm = viewModel()
        vm.onEditClick()
        vm.onNameChange("Al")

        vm.onEditFieldFocusLost(EditProfileField.NAME)

        assertEquals(FormTexts.NAME_TOO_SHORT, vm.uiState.value.nameError)
    }

    @Test
    fun bioIsLimitedAndTheLimitCanBeShown() = runTest {
        val vm = viewModel()
        vm.onEditClick()

        vm.onBioChange("x".repeat(MAX_BIO_LENGTH + 30))

        assertEquals(MAX_BIO_LENGTH, vm.uiState.value.editBio.length)
    }

    @Test
    fun validSaveClosesTheSheetAndConfirms() = runTest {
        val vm = viewModel()
        vm.onEditClick()

        vm.onSaveEdit()

        assertFalse(vm.uiState.value.showEditDialog)
        assertFalse(vm.uiState.value.isSaving)
        assertEquals("Datos personales actualizados.", vm.uiState.value.snackbarMessage)
    }

    @Test
    fun reopeningTheSheetStartsCleanAgain() = runTest {
        val vm = viewModel()
        vm.openEmpty()
        vm.onSaveEdit()

        vm.onDismissEditDialog()
        vm.onEditClick()

        assertNull(vm.uiState.value.nameError)
        assertFalse(vm.uiState.value.showInvalidNotice)
    }
}
