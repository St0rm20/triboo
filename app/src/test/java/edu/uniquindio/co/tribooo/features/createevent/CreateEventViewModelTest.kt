package edu.uniquindio.co.tribooo.features.createevent

import androidx.lifecycle.SavedStateHandle
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.data.repository.memory.LocationRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.usecase.event.CreateEventUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.GetEventUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.ObserveEventImagesUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.ResubmitEventUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.UpdateEventUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.currentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.emptyEventRepository
import edu.uniquindio.co.tribooo.domain.usecase.event.testEvent
import edu.uniquindio.co.tribooo.domain.usecase.location.GetPlaceSuggestionsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateEventViewModelTest {

    private val me: User = runBlocking { UserRepositoryImpl().findById("u-daniela")!! }
    private val other = User("x", "Otra Persona", "x@mail.co", "123456", "Armenia", "Centro")
    private lateinit var repository: EventRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = runBlocking {
            emptyEventRepository().also {
                it.save(testEvent("pend", me, EventStatus.PENDIENTE, title = "Pendiente"), listOf("a.jpg", "b.jpg"))
                it.save(testEvent("rej", me, EventStatus.RECHAZADO, title = "Rechazado"))
                it.save(testEvent("fin", me, EventStatus.FINALIZADO, title = "Finalizado"))
                it.save(testEvent("ajeno", other, EventStatus.PENDIENTE))
            }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(eventId: String? = null, mode: EventFormMode? = null): CreateEventViewModel {
        val args = buildMap<String, Any> {
            eventId?.let { put("eventId", it) }
            mode?.let { put("mode", it) }
        }
        val user = currentUserUseCase("u-daniela")
        return CreateEventViewModel(
            getPlaceSuggestions = GetPlaceSuggestionsUseCase(LocationRepositoryImpl()),
            getCurrentUserUseCase = user,
            createEventUseCase = CreateEventUseCase(repository),
            getEventUseCase = GetEventUseCase(repository),
            updateEventUseCase = UpdateEventUseCase(repository, user),
            resubmitEventUseCase = ResubmitEventUseCase(repository, user),
            observeEventImages = ObserveEventImagesUseCase(repository),
            savedStateHandle = SavedStateHandle(args)
        )
    }

    @Test
    fun createModeStartsEmptyAndWithoutLoading() {
        val state = viewModel().uiState.value
        assertNull(state.mode)
        assertFalse(state.isLoading)
        assertNull(state.loadError)
        assertEquals("", state.title)
    }

    @Test
    fun editPreloadsTheEventAndSavesOverTheSameId() = runTest {
        val vm = viewModel("pend", EventFormMode.EDIT)
        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Pendiente", state.title)
        assertEquals(listOf("a.jpg", "b.jpg"), state.imageUris.sorted())

        vm.onTitleChange("Editado")
        vm.publish()

        assertEquals(RequestResult.Success("Cambios guardados. El evento sigue en revisión."), vm.uiState.value.publishResult)
        assertEquals("Editado", repository.findById("pend")!!.title)
        assertEquals(4, repository.events.value.size)
    }

    @Test
    fun resubmitMovesRejectedEventBackToPending() = runTest {
        val vm = viewModel("rej", EventFormMode.RESUBMIT)

        vm.publish()

        assertEquals(RequestResult.Success("Evento reenviado a revisión."), vm.uiState.value.publishResult)
        assertEquals(EventStatus.PENDIENTE, repository.findById("rej")!!.status)
    }

    @Test
    fun duplicateCreatesANewEventAndKeepsTheOriginal() = runTest {
        val original = repository.findById("fin")!!
        val vm = viewModel("fin", EventFormMode.DUPLICATE)
        assertEquals("Finalizado", vm.uiState.value.title)

        vm.publish()

        assertEquals(RequestResult.Success("Evento duplicado y enviado a revisión."), vm.uiState.value.publishResult)
        assertEquals(original, repository.findById("fin"))
        val copy = repository.events.value.single { it.id != "fin" && it.title == "Finalizado" }
        assertEquals(EventStatus.PENDIENTE, copy.status)
        assertEquals(me.id, copy.organizer.id)
        assertEquals(5, repository.events.value.size)
    }

    @Test
    fun preloadRunsOnlyOnce() = runTest {
        val vm = viewModel("pend", EventFormMode.EDIT)
        vm.onTitleChange("Mi cambio")

        // El estado ya cargado no se vuelve a pisar aunque el evento cambie en el repositorio
        repository.update(repository.findById("pend")!!.copy(title = "Cambio externo"))
        vm.onDescriptionChange("otra")

        assertEquals("Mi cambio", vm.uiState.value.title)
    }

    @Test
    fun missingOrForeignEventExposesLoadError() = runTest {
        for (id in listOf("no-existe", "ajeno")) {
            val state = viewModel(id, EventFormMode.EDIT).uiState.value
            assertFalse(state.isLoading)
            assertNotNull(state.loadError)
        }
        assertNotNull(viewModel(null, EventFormMode.EDIT).uiState.value.loadError)
    }

    @Test
    fun publishIsBlockedWhenLoadFailed() = runTest {
        val vm = viewModel("ajeno", EventFormMode.EDIT)
        vm.publish()
        assertNull(vm.uiState.value.publishResult)
    }

    @Test
    fun secondTapWhileLoadingIsIgnored() = runTest {
        // Con el dispatcher pausado el primer toque deja Loading y el segundo no debe crear otro evento
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = viewModel("fin", EventFormMode.DUPLICATE)
        testScheduler.advanceUntilIdle()

        vm.publish()
        assertEquals(RequestResult.Loading, vm.uiState.value.publishResult)
        vm.publish()
        testScheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.publishResult is RequestResult.Success)
        assertEquals(5, repository.events.value.size)
    }

    @Test
    fun validationMatchesCreate() = runTest {
        val vm = viewModel("pend", EventFormMode.EDIT)
        vm.onTitleChange("  ")
        vm.onContributionEnabledChange(true)
        vm.onContributionAmountChange("0")

        vm.publish()

        assertTrue(vm.uiState.value.publishResult is RequestResult.Failure)
        assertNotNull(vm.uiState.value.titleError)
        assertNotNull(vm.uiState.value.contributionError)
        assertEquals("Pendiente", repository.findById("pend")!!.title)
    }
}
