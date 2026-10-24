package edu.uniquindio.co.tribooo.features.profile

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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfilePhotoTest {

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

    private fun savedPhoto() = world.users.users.value.first { it.id == world.me.id }.photo

    @Test
    fun savingPersistsThePickedPhotoOnTheUser() = runTest {
        val vm = viewModel()
        vm.onEditClick()
        assertNull(vm.uiState.value.editPhoto)

        vm.onPhotoSelected("content://media/picker/1")
        assertNull(savedPhoto())
        vm.onSaveEdit()

        assertEquals("content://media/picker/1", savedPhoto())
        assertEquals("content://media/picker/1", vm.uiState.value.user?.photo)
    }

    @Test
    fun removingThePhotoSavesNull() = runTest {
        val vm = viewModel()
        vm.onEditClick()
        vm.onPhotoSelected("content://media/picker/1")
        vm.onSaveEdit()

        vm.onEditClick()
        assertEquals("content://media/picker/1", vm.uiState.value.editPhoto)
        vm.onPhotoRemoved()
        assertNull(vm.uiState.value.editPhoto)
        vm.onSaveEdit()

        assertNull(savedPhoto())
    }

    @Test
    fun cancellingDiscardsTheDraftPhoto() = runTest {
        val vm = viewModel()
        vm.onEditClick()
        vm.onPhotoSelected("content://media/picker/2")

        vm.onDismissEditDialog()
        vm.onEditClick()

        assertNull(vm.uiState.value.editPhoto)
        assertNull(savedPhoto())
    }
}
