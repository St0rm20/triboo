package edu.uniquindio.co.tribooo.features.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.ResourceProvider
import edu.uniquindio.co.tribooo.core.util.runRequest
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
    /** Evento que se está quitando o devolviendo; su botón espera la respuesta. */
    val busyEventId: String? = null,
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

    /** Resultado de la última petición y el evento al que corresponde. */
    private val action = MutableStateFlow<Pair<String, RequestResult>?>(null)

    val uiState: StateFlow<SavedEventsUiState> = combine(observeMySavedEvents(), message, action) { events, message, action ->
        SavedEventsUiState(
            summary = savedSummary(resources, events.size),
            events = events,
            busyEventId = action?.takeIf { it.second is RequestResult.Loading }?.first,
            message = message
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
        SavedEventsUiState(summary = savedSummary(resources, 0))
    )

    /** Quita el evento de guardados y ofrece deshacer. */
    fun onRemove(event: Event) = toggle(event, SavedMessage(resources.getString(R.string.saved_message_removed), undoEvent = event))

    /** Devuelve a guardados el evento quitado. */
    fun onUndo(event: Event) = toggle(event, SavedMessage(resources.getString(R.string.saved_message_restored)))

    /** Marca Loading, cambia el guardado y muestra [done] o, si falló, el motivo. Una petición a la vez. */
    private fun toggle(event: Event, done: SavedMessage) {
        if (action.value?.second is RequestResult.Loading) return
        viewModelScope.launch {
            action.value = event.id to RequestResult.Loading
            val result = runRequest(resources, onSuccess = { _: Unit -> RequestResult.Success(done.text) }) {
                toggleSavedEventUseCase(event)
            }
            action.value = event.id to result
            when (result) {
                is RequestResult.Success -> message.value = done
                is RequestResult.Failure -> message.value = SavedMessage(result.errorMessage)
                RequestResult.Loading -> Unit
            }
        }
    }

    fun onMessageShown() = message.update { null }
}
