package edu.uniquindio.co.tribooo.features.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.core.util.ResourceProvider
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
    val summary: String = "",
    val events: List<Event> = emptyList(),
    val message: SavedMessage? = null
)

/** "Tu lista para decidir después", "1 evento guardado" o "3 eventos guardados" */
fun savedSummary(resources: ResourceProvider, count: Int): String =
    if (count == 0) {
        resources.getString(R.string.saved_empty_summary)
    } else {
        resources.getQuantityString(R.plurals.saved_summary, count, count)
    }

@HiltViewModel
class SavedEventsViewModel @Inject constructor(
    observeMySavedEvents: ObserveMySavedEventsUseCase,
    private val toggleSavedEventUseCase: ToggleSavedEventUseCase,
    private val resources: ResourceProvider
) : ViewModel() {

    private val message = MutableStateFlow<SavedMessage?>(null)

    val uiState: StateFlow<SavedEventsUiState> = combine(observeMySavedEvents(), message) { events, message ->
        SavedEventsUiState(summary = savedSummary(resources, events.size), events = events, message = message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
        SavedEventsUiState(summary = savedSummary(resources, 0))
    )

    /** Quita el evento de guardados y ofrece deshacer. */
    fun onRemove(event: Event) {
        viewModelScope.launch {
            toggleSavedEventUseCase(event)
            message.value = SavedMessage(resources.getString(R.string.saved_message_removed), undoEvent = event)
        }
    }

    /** Devuelve a guardados el evento quitado. */
    fun onUndo(event: Event) {
        viewModelScope.launch {
            toggleSavedEventUseCase(event)
            message.value = SavedMessage(resources.getString(R.string.saved_message_restored))
        }
    }

    fun onMessageShown() = message.update { null }
}
