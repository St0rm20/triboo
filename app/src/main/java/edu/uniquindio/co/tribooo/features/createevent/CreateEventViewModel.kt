package edu.uniquindio.co.tribooo.features.createevent

import androidx.lifecycle.ViewModel
import edu.uniquindio.co.tribooo.core.util.APP_ZONE
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.formatMoney
import edu.uniquindio.co.tribooo.data.EventRepository
import edu.uniquindio.co.tribooo.data.MockData
import edu.uniquindio.co.tribooo.data.PlaceSuggestion
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Contribution
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

private const val MIN_CUPO = 5
private const val CUPO_STEP = 5
private const val MAX_IMAGES = 5

data class CreateEventUiState(
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
    val publishResult: RequestResult? = null
) {
    val start: LocalDateTime get() = startDate.atTime(startTime)

    /** En eventos de un solo día el fin es la misma fecha de inicio. */
    val end: LocalDateTime get() = (if (multiDay) endDate else startDate).atTime(endTime)

    val isRangeValid: Boolean get() = end.isAfter(start)
}

class CreateEventViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreateEventUiState())
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    val placeSuggestions: List<PlaceSuggestion> = MockData.placeSuggestions
    val categories: List<Category> = Category.defaultCategories
    val contributionPresets: List<Long> = listOf(2000, 5000, 10000)

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

    fun onStartDateChange(date: LocalDate) {
        _uiState.update { it.copy(startDate = date) }
    }

    fun onStartTimeChange(time: LocalTime) {
        _uiState.update { it.copy(startTime = time) }
    }

    fun onEndDateChange(date: LocalDate) {
        _uiState.update { it.copy(endDate = date) }
    }

    fun onEndTimeChange(time: LocalTime) {
        _uiState.update { it.copy(endTime = time) }
    }

    fun onPlaceSelected(place: PlaceSuggestion) {
        _uiState.update { it.copy(place = place, placeError = false) }
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

        val event = Event(
            id = EventRepository.newEventId(),
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
            // Todo evento nuevo pasa por moderación antes de aparecer en el feed
            status = EventStatus.PENDIENTE,
            organizer = MockData.currentUser,
            category = category
        )
        EventRepository.addEvent(event, state.imageUris)

        val message = if (state.contributionEnabled) {
            "Evento enviado a revisión. Moderación verificará que el aporte de ${state.contributionAmount.formatMoney()} sea benéfico."
        } else {
            "Evento enviado a revisión. Te avisamos al aprobarlo."
        }
        _uiState.update { it.copy(publishResult = RequestResult.Success(message)) }
    }

    fun resetPublishResult() {
        _uiState.update { it.copy(publishResult = null) }
    }
}
