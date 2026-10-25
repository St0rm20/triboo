package edu.uniquindio.co.tribooo.features.createevent

import androidx.lifecycle.SavedStateHandle
import edu.uniquindio.co.tribooo.core.util.FormLimits
import edu.uniquindio.co.tribooo.core.util.FormTexts
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
import edu.uniquindio.co.tribooo.domain.usecase.location.ResolvePlaceUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.runBlocking
import java.time.LocalTime
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
            resolvePlaceUseCase = ResolvePlaceUseCase(LocationRepositoryImpl(), user),
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

    @Test
    fun freeTextPlaceIsSavedInThePublishedEvent() = runTest {
        val vm = viewModel()
        vm.onTitleChange("Chocolatada")
        vm.onCategorySelected(vm.categories.first())

        vm.onPlaceConfirmed("  Cancha   del  barrio ", "")
        vm.publish()

        assertTrue(vm.uiState.value.publishResult is RequestResult.Success)
        val saved = repository.events.value.single { it.title == "Chocolatada" }
        assertEquals("Cancha del barrio", saved.place)
        assertEquals(me.city, saved.neighborhood)
    }

    @Test
    fun blankPlaceNameKeepsThePreviousPlace() = runTest {
        val vm = viewModel()
        vm.onPlaceConfirmed("Parque X", "Norte")

        vm.onPlaceConfirmed("   ", "Sur")

        assertEquals("Parque X", vm.uiState.value.place?.name)
    }

    private fun readyToPublish(): CreateEventViewModel {
        val vm = viewModel()
        vm.onTitleChange("Bazar benéfico")
        vm.onCategorySelected(vm.categories.first())
        vm.onPlaceConfirmed("Salón comunal", "Norte")
        return vm
    }

    @Test
    fun contributionRequiresADeclaredCause() = runTest {
        val vm = readyToPublish()
        vm.onContributionEnabledChange(true)
        vm.onContributionAmountChange("5000")
        vm.onContributionCauseChange("   ")

        vm.publish()

        assertTrue(vm.uiState.value.publishResult is RequestResult.Failure)
        assertEquals("Indica a qué se destina el aporte", vm.uiState.value.contributionCauseError)
        assertNull(vm.uiState.value.contributionError)
        assertTrue(repository.events.value.none { it.title == "Bazar benéfico" })
    }

    @Test
    fun causeIsTrimmedAndSavedWithTheContribution() = runTest {
        val vm = readyToPublish()
        vm.onContributionEnabledChange(true)
        vm.onContributionAmountChange("5000")
        vm.onContributionCauseChange("  Becas de la Escuela de Bambuco  ")

        vm.publish()
        testScheduler.advanceUntilIdle()

        val contribution = repository.events.value.single { it.title == "Bazar benéfico" }.contribution!!
        assertEquals(5000.0, contribution.amount, 0.0)
        assertEquals("Becas de la Escuela de Bambuco", contribution.cause)
    }

    @Test
    fun causeIsLimitedTo120CharactersAndTypingClearsItsError() = runTest {
        val vm = readyToPublish()
        vm.onContributionEnabledChange(true)
        vm.onContributionAmountChange("5000")
        vm.publish()
        assertNotNull(vm.uiState.value.contributionCauseError)

        vm.onContributionCauseChange("x".repeat(200))

        assertEquals(120, vm.uiState.value.contributionCause.length)
        assertNull(vm.uiState.value.contributionCauseError)
    }

    @Test
    fun withoutContributionNoCauseIsRequired() = runTest {
        val vm = readyToPublish()

        vm.publish()
        testScheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.publishResult is RequestResult.Success)
        assertNull(repository.events.value.single { it.title == "Bazar benéfico" }.contribution)
    }

    @Test
    fun openingTheFormShowsNoErrors() {
        val state = viewModel().uiState.value
        assertNull(state.titleError)
        assertNull(state.categoryError)
        assertNull(state.placeError)
        assertNull(state.scheduleError)
        assertNull(state.contributionError)
        assertNull(state.contributionCauseError)
    }

    @Test
    fun emptyFormPublishShowsAllErrorsAtOnceAndTheGeneralNotice() = runTest {
        val vm = viewModel()

        vm.publish()

        val state = vm.uiState.value
        assertEquals(FormTexts.EVENT_TITLE_REQUIRED, state.titleError)
        assertEquals("Elige una categoría", state.categoryError)
        assertEquals("Elige o escribe el lugar del evento", state.placeError)
        assertEquals(RequestResult.Failure("Revisa los campos marcados."), state.publishResult)
        assertEquals(EventField.TITLE, state.focusField)
        assertTrue(repository.events.value.none { it.title.isBlank() })
    }

    @Test
    fun touchedAndEmptyTitleShowsItsErrorButUntouchedFieldsDoNot() {
        val vm = viewModel()

        vm.onFieldFocusLost(EventField.TITLE)

        assertEquals(FormTexts.EVENT_TITLE_REQUIRED, vm.uiState.value.titleError)
        assertNull(vm.uiState.value.categoryError)
        assertNull(vm.uiState.value.placeError)
    }

    @Test
    fun fixingTheTitleClearsOnlyItsError() = runTest {
        val vm = viewModel()
        vm.publish()

        vm.onTitleChange("Bazar")

        assertNull(vm.uiState.value.titleError)
        assertNotNull(vm.uiState.value.categoryError)
        assertNotNull(vm.uiState.value.placeError)
    }

    @Test
    fun focusSkipsToTheFirstInvalidFieldAndIsConsumedOnce() = runTest {
        val vm = viewModel()
        vm.onTitleChange("Bazar")

        vm.publish()
        assertEquals(EventField.CATEGORY, vm.uiState.value.focusField)

        vm.onFocusHandled()
        assertNull(vm.uiState.value.focusField)
    }

    @Test
    fun endBeforeStartShowsATextualScheduleError() = runTest {
        val vm = readyToPublish()
        vm.onStartTimeChange(LocalTime.of(20, 0))
        vm.onEndTimeChange(LocalTime.of(18, 0))

        assertEquals("La hora de fin debe ser posterior a la de inicio", vm.uiState.value.scheduleError)
        vm.publish()

        assertEquals(EventField.SCHEDULE, vm.uiState.value.focusField)
        assertTrue(vm.uiState.value.publishResult is RequestResult.Failure)
        assertTrue(repository.events.value.none { it.title == "Bazar benéfico" })
    }

    @Test
    fun contributionErrorsOnlyApplyWhenTheContributionIsEnabled() = runTest {
        val vm = readyToPublish()
        vm.publish()
        testScheduler.advanceUntilIdle()
        assertTrue(vm.uiState.value.publishResult is RequestResult.Success)

        val other = readyToPublish()
        other.onContributionEnabledChange(true)
        other.publish()

        assertEquals(FormTexts.CONTRIBUTION_AMOUNT_REQUIRED, other.uiState.value.contributionError)
        assertEquals(FormTexts.CONTRIBUTION_CAUSE_REQUIRED, other.uiState.value.contributionCauseError)
        assertEquals(EventField.CONTRIBUTION_AMOUNT, other.uiState.value.focusField)
    }

    @Test
    fun moreThanFiveImagesKeepsTheFirstFiveAndExplainsIt() {
        val vm = viewModel()

        vm.onImagesSelected((1..7).map { "img$it.jpg" })

        assertEquals((1..5).map { "img$it.jpg" }, vm.uiState.value.imageUris)
        assertEquals("Máximo 5 imágenes; se usaron las primeras 5", vm.uiState.value.imageNotice)

        vm.onImagesSelected(listOf("a.jpg"))
        assertNull(vm.uiState.value.imageNotice)
    }

    @Test
    fun descriptionAndCauseAreLimitedAndTheLimitIsKnown() {
        val vm = viewModel()

        vm.onDescriptionChange("d".repeat(FormLimits.EVENT_DESCRIPTION + 40))
        vm.onContributionCauseChange("c".repeat(FormLimits.CONTRIBUTION_CAUSE + 40))

        assertEquals(FormLimits.EVENT_DESCRIPTION, vm.uiState.value.description.length)
        assertEquals(FormLimits.CONTRIBUTION_CAUSE, vm.uiState.value.contributionCause.length)
    }

    @Test
    fun editModeValidatesLikeCreateAndKeepsTheLoadedValuesValid() = runTest {
        val vm = viewModel("pend", EventFormMode.EDIT)

        assertNull(vm.uiState.value.titleError)
        vm.onTitleChange("")
        vm.onFieldFocusLost(EventField.TITLE)

        assertEquals(FormTexts.EVENT_TITLE_REQUIRED, vm.uiState.value.titleError)
    }
}
