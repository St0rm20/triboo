package edu.uniquindio.co.tribooo.features.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.usecase.saved.ObserveMySavedEventsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.saved.ToggleSavedEventUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Mensaje de la pantalla; si trae [undoEvent], el snackbar ofrece "Deshacer" para devolverlo a guardados. */
data class SavedMessage(val text: String, val undoEvent: Event? = null)

data class SavedEventsUiState(
    val summary: String = SAVED_EMPTY_SUMMARY,
    val events: List<Event> = emptyList(),
    val message: SavedMessage? = null
)

const val SAVED_EMPTY_SUMMARY = "Tu lista para decidir después"

/** "Tu lista para decidir después", "1 evento guardado" o "3 eventos guardados" */
fun savedSummary(count: Int): String = when (count) {
    0 -> SAVED_EMPTY_SUMMARY
    1 -> "1 evento guardado"
    else -> "$count eventos guardados"
}

@HiltViewModel
class SavedEventsViewModel @Inject constructor(
    observeMySavedEvents: ObserveMySavedEventsUseCase,
    private val toggleSavedEventUseCase: ToggleSavedEventUseCase
) : ViewModel() {

    private val message = MutableStateFlow<SavedMessage?>(null)

    val uiState: StateFlow<SavedEventsUiState> = combine(observeMySavedEvents(), message) { events, message ->
        SavedEventsUiState(summary = savedSummary(events.size), events = events, message = message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SavedEventsUiState())

    /** Quita el evento de guardados y ofrece deshacer. */
    fun onRemove(event: Event) {
        viewModelScope.launch {
            toggleSavedEventUseCase(event)
            message.value = SavedMessage("Evento quitado de guardados.", undoEvent = event)
        }
    }

    /** Devuelve a guardados el evento quitado. */
    fun onUndo(event: Event) {
        viewModelScope.launch {
            toggleSavedEventUseCase(event)
            message.value = SavedMessage("Evento devuelto a guardados.")
        }
    }

    fun onMessageShown() = message.update { null }
}
