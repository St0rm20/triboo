    package edu.uniquindio.co.tribooo.features.createevent

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.core.util.APP_ZONE
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.formatMoney
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Contribution
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.ImageEvent
import edu.uniquindio.co.tribooo.domain.model.PlaceSuggestion
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.CreateEventUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.GetEventUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.ObserveEventImagesUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.ResubmitEventUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.UpdateEventUseCase
import edu.uniquindio.co.tribooo.domain.usecase.location.GetPlaceSuggestionsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.location.ResolvePlaceUseCase
import edu.uniquindio.co.tribooo.domain.usecase.location.filterPlaceSuggestions
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

private const val MIN_CUPO = 5
private const val CUPO_STEP = 5
private const val MAX_IMAGES = 5
private const val EVENT_ID_KEY = "eventId"
private const val MODE_KEY = "mode"
private const val EVENT_NOT_FOUND = "No encontramos este evento o no es tuyo."

/** Selector abierto en la pantalla. Algunos encadenan al siguiente (fecha → hora). */
enum class PickerTarget { START_DATE, START_TIME, END_DATE, END_TIME, PLACE }

data class CreateEventUiState(
    /** null = crear un evento desde cero. */
    val mode: EventFormMode? = null,
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val title: String = "",
    val titleError: String? = null,
    val category: Category? = null,
    val categoryError: Boolean = false,
    val description: String = "",
    val multiDay: Boolean = false,
    val startDate: LocalDate = LocalDate.now(APP_ZONE).plusDays(1),
    val startTime: LocalTime = LocalTime.of(18, 0),
    val endDate: LocalDate = LocalDate.now(APP_ZONE).plusDays(1),
    val endTime: LocalTime = LocalTime.of(20, 30),
    val place: PlaceSuggestion? = null,
    val placeError: Boolean = false,
    val cupoEnabled: Boolean = true,
    val cupo: Int = 40,
    val contributionEnabled: Boolean = false,
    val contributionAmount: Long = 0,
    val contributionError: String? = null,
    val imageUris: List<String> = emptyList(),
    val picker: PickerTarget? = null,
    val publishResult: RequestResult? = null
) {
    val start: LocalDateTime get() = startDate.atTime(startTime)

    /** En eventos de un solo día el fin es la misma fecha de inicio. */
    val end: LocalDateTime get() = (if (multiDay) endDate else startDate).atTime(endTime)

    val isRangeValid: Boolean get() = end.isAfter(start)
}

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    getPlaceSuggestions: GetPlaceSuggestionsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val createEventUseCase: CreateEventUseCase,
    private val getEventUseCase: GetEventUseCase,
    private val updateEventUseCase: UpdateEventUseCase,
    private val resubmitEventUseCase: ResubmitEventUseCase,
    private val resolvePlaceUseCase: ResolvePlaceUseCase,
    observeEventImages: ObserveEventImagesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Solo la ruta EditEvent trae estos argumentos; en CreateEvent no existen (modo crear)
    private val eventId: String? = savedStateHandle[EVENT_ID_KEY]
    private val mode: EventFormMode? = when (val raw = savedStateHandle.get<Any>(MODE_KEY)) {
        is EventFormMode -> raw
        is String -> EventFormMode.entries.firstOrNull { it.name == raw }
        else -> null
    }

    private val _uiState = MutableStateFlow(CreateEventUiState(mode = mode, isLoading = mode != null))
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    val placeSuggestions: List<PlaceSuggestion> = getPlaceSuggestions()
    val categories: List<Category> = Category.defaultCategories
    val contributionPresets: List<Long> = listOf(2000, 5000, 10000)

    init {
        if (mode != null && eventId != null) {
            viewModelScope.launch { preload(eventId, mode, observeEventImages()) }
        } else if (mode != null) {
            _uiState.update { it.copy(isLoading = false, loadError = EVENT_NOT_FOUND) }
        }
    }

    // Se ejecuta una sola vez por ViewModel: girar la pantalla no pisa lo que el usuario ya editó
    private suspend fun preload(eventId: String, mode: EventFormMode, images: StateFlow<List<ImageEvent>>) {
        val event = getEventUseCase(eventId)
        val user = getCurrentUserUseCase()
        if (event == null || user == null || event.organizer.id != user.id) {
            _uiState.update { it.copy(isLoading = false, loadError = EVENT_NOT_FOUND) }
            return
        }
        val gallery = images.value.filter { it.event.id == eventId }.sortedBy { it.order }.map { it.url }
        val imageUris = (listOfNotNull(event.imageUrl) + gallery).distinct()
        _uiState.value = event.toFormState(mode, imageUris, placeSuggestions, Instant.now())
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value, titleError = null) }
    }

    fun onCategorySelected(category: Category) {
        _uiState.update { it.copy(category = category, categoryError = false) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onMultiDayChange(enabled: Boolean) {
        _uiState.update {
            // Al activar varios días, el fin parte del día siguiente al inicio
            val endDate = if (enabled && !it.endDate.isAfter(it.startDate)) it.startDate.plusDays(1) else it.endDate
            it.copy(multiDay = enabled, endDate = endDate)
        }
    }

    fun openPicker(target: PickerTarget) {
        _uiState.update { it.copy(picker = target) }
    }

    fun closePicker() {
        _uiState.update { it.copy(picker = null) }
    }

    // En eventos de varios días tras la fecha de inicio se pide también la hora
    fun onStartDateChange(date: LocalDate) {
        _uiState.update {
            it.copy(startDate = date, picker = if (it.multiDay) PickerTarget.START_TIME else null)
        }
    }

    // En eventos de un día el horario se elige como inicio → fin
    fun onStartTimeChange(time: LocalTime) {
        _uiState.update {
            it.copy(startTime = time, picker = if (it.multiDay) null else PickerTarget.END_TIME)
        }
    }

    fun onEndDateChange(date: LocalDate) {
        _uiState.update { it.copy(endDate = date, picker = PickerTarget.END_TIME) }
    }

    fun onEndTimeChange(time: LocalTime) {
        _uiState.update { it.copy(endTime = time, picker = null) }
    }

    /** Lugares conocidos que contienen [query] en su nombre o barrio. */
    fun suggestionsFor(query: String): List<PlaceSuggestion> = filterPlaceSuggestions(placeSuggestions, query)

    /** Fija el lugar escrito; un nombre vacío no cambia nada. */
    fun onPlaceConfirmed(name: String, neighborhood: String) {
        viewModelScope.launch {
            val place = resolvePlaceUseCase(name, neighborhood) ?: return@launch
            _uiState.update { it.copy(place = place, placeError = false, picker = null) }
        }
    }

    fun onCupoEnabledChange(enabled: Boolean) {
        _uiState.update { it.copy(cupoEnabled = enabled) }
    }

    fun increaseCupo() {
        _uiState.update { it.copy(cupo = it.cupo + CUPO_STEP) }
    }

    fun decreaseCupo() {
        _uiState.update { it.copy(cupo = (it.cupo - CUPO_STEP).coerceAtLeast(MIN_CUPO)) }
    }

    fun onContributionEnabledChange(enabled: Boolean) {
        _uiState.update { it.copy(contributionEnabled = enabled, contributionError = null) }
    }

    fun onContributionAmountChange(value: String) {
        val amount = value.filter { it.isDigit() }.take(9).toLongOrNull() ?: 0
        _uiState.update { it.copy(contributionAmount = amount, contributionError = null) }
    }

    fun onContributionPresetSelected(amount: Long) {
        _uiState.update { it.copy(contributionAmount = amount, contributionError = null) }
    }

    fun onImagesSelected(uris: List<String>) {
        if (uris.isEmpty()) return
        _uiState.update { it.copy(imageUris = uris.take(MAX_IMAGES)) }
    }

    fun publish() {
        val state = _uiState.value
        if (state.isLoading || state.loadError != null || state.publishResult == RequestResult.Loading) return
        val titleError = if (state.title.isBlank()) "Ponle un nombre claro al evento" else null
        val contributionError = if (state.contributionEnabled && state.contributionAmount <= 0) {
            "Indica el monto del aporte o desactiva el costo"
        } else {
            null
        }

        _uiState.update {
            it.copy(
                titleError = titleError,
                categoryError = state.category == null,
                placeError = state.place == null,
                contributionError = contributionError
            )
        }

        val category = state.category
        val place = state.place
        if (titleError != null || contributionError != null || category == null || place == null) {
            _uiState.update { it.copy(publishResult = RequestResult.Failure("Completa los campos marcados.")) }
            return
        }
        if (!state.isRangeValid) {
            _uiState.update {
                it.copy(publishResult = RequestResult.Failure("Revisa el horario: el fin debe ser posterior al inicio."))
            }
            return
        }

        // Se marca antes de lanzar la corrutina para evitar publicar dos veces con toques seguidos
        _uiState.update { it.copy(publishResult = RequestResult.Loading) }
        viewModelScope.launch {
            val organizer = getCurrentUserUseCase()
            if (organizer == null) {
                _uiState.update { it.copy(publishResult = RequestResult.Failure("Inicia sesión para publicar eventos.")) }
                return@launch
            }

            val event = Event(
                // Editar y reenviar actualizan el mismo evento; crear y duplicar generan uno nuevo
                id = if (mode == EventFormMode.EDIT || mode == EventFormMode.RESUBMIT) eventId.orEmpty() else UUID.randomUUID().toString(),
                title = state.title.trim(),
                description = state.description.trim(),
                startDate = state.start.atZone(APP_ZONE).toInstant(),
                endDate = state.end.atZone(APP_ZONE).toInstant(),
                multidia = state.multiDay,
                place = place.name,
                neighborhood = place.neighborhood,
                location = place.location,
                cupo = if (state.cupoEnabled) state.cupo else null,
                contribution = if (state.contributionEnabled) Contribution(amount = state.contributionAmount.toDouble()) else null,
                organizer = organizer,
                category = category
            )

            val result = when (mode) {
                EventFormMode.EDIT -> updateEventUseCase(event, state.imageUris)
                EventFormMode.RESUBMIT -> resubmitEventUseCase(event, state.imageUris)
                // El caso de uso lo envía a moderación antes de que aparezca en el feed
                else -> {
                    createEventUseCase(event, state.imageUris)
                    RequestResult.Success(createdMessage(state))
                }
            }
            _uiState.update { it.copy(publishResult = result) }
        }
    }

    private fun createdMessage(state: CreateEventUiState) = when {
        mode == EventFormMode.DUPLICATE -> "Evento duplicado y enviado a revisión."
        state.contributionEnabled ->
            "Evento enviado a revisión. Moderación verificará que el aporte de ${state.contributionAmount.formatMoney()} sea benéfico."
        else -> "Evento enviado a revisión. Te avisamos al aprobarlo."
    }

    fun resetPublishResult() {
        _uiState.update { it.copy(publishResult = null) }
    }
}
