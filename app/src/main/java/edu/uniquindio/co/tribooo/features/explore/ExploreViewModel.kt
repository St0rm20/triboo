package edu.uniquindio.co.tribooo.features.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.core.component.FeedEventItem
import edu.uniquindio.co.tribooo.core.component.MapPin
import edu.uniquindio.co.tribooo.core.component.toCardItems
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.ResourceProvider
import edu.uniquindio.co.tribooo.core.util.messageOrNull
import edu.uniquindio.co.tribooo.core.util.runRequest
import edu.uniquindio.co.tribooo.core.util.waitlistLeftMessage
import edu.uniquindio.co.tribooo.core.util.waitlistJoinedMessage
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventFilters
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.usecase.attendance.AttendanceStep
import edu.uniquindio.co.tribooo.domain.usecase.attendance.FindScheduleConflictUseCase
import edu.uniquindio.co.tribooo.domain.usecase.attendance.ObserveAttendancesUseCase
import edu.uniquindio.co.tribooo.domain.usecase.attendance.ToggleAttendanceUseCase
import edu.uniquindio.co.tribooo.domain.usecase.attendance.nextAttendanceStep
import edu.uniquindio.co.tribooo.domain.usecase.attendance.startAttendanceFlow
import edu.uniquindio.co.tribooo.domain.usecase.auth.ObserveCurrentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.ObservePublishedEventsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.filters.ClearEventFiltersUseCase
import edu.uniquindio.co.tribooo.domain.usecase.filters.FilterEventsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.filters.ObserveEventFiltersUseCase
import edu.uniquindio.co.tribooo.domain.usecase.filters.UpdateEventFiltersUseCase
import edu.uniquindio.co.tribooo.domain.usecase.location.ObserveCurrentLocationUseCase
import edu.uniquindio.co.tribooo.domain.usecase.location.UpdateCurrentLocationUseCase
import edu.uniquindio.co.tribooo.domain.usecase.waitlist.JoinWaitlistUseCase
import edu.uniquindio.co.tribooo.domain.usecase.waitlist.LeaveWaitlistUseCase
import edu.uniquindio.co.tribooo.domain.usecase.waitlist.ObserveWaitlistUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ExploreView { MAP, LIST }

data class ExploreUiState(
    val view: ExploreView = ExploreView.MAP,
    /** Eventos que cumplen los filtros, del más cercano al más lejano. */
    val events: List<FeedEventItem> = emptyList(),
    val pins: List<MapPin> = emptyList(),
    /** Evento de la hoja inferior del mapa; null si no hay eventos. */
    val selected: FeedEventItem? = null,
    val filters: EventFilters = EventFilters(),
    val categories: List<Category> = Category.defaultCategories,
    val showFilters: Boolean = false,
    /** Diálogo que el usuario debe aceptar antes de confirmar su asistencia. */
    val attendanceStep: AttendanceStep? = null,
    val message: String? = null,
    /** Resultado de la última petición hecha desde una tarjeta (asistencia o lista de espera). */
    val actionResult: RequestResult? = null,
    /** Evento cuya petición sigue en curso; su botón muestra el indicador de carga. */
    val loadingEventId: String? = null
)

/** Parte del estado que vive solo en esta pantalla. */
internal data class ExploreLocal(
    val view: ExploreView = ExploreView.MAP,
    val selectedId: String? = null,
    val showFilters: Boolean = false,
    val attendanceStep: AttendanceStep? = null,
    val message: String? = null,
    val actionResult: RequestResult? = null,
    val loadingEventId: String? = null
)

/** Eventos filtrados con sus pines, tal como salen de los repositorios. */
internal data class ExploreContent(
    val events: List<FeedEventItem>,
    val pins: List<MapPin>,
    val filters: EventFilters
)

/**
 * Evento seleccionado: el elegido si sigue cumpliendo los filtros y, si no (o si aún no se eligió),
 * el primero de la lista, que es el más cercano.
 */
internal fun resolveSelected(events: List<FeedEventItem>, selectedId: String?): FeedEventItem? =
    events.firstOrNull { it.event.id == selectedId } ?: events.firstOrNull()

@HiltViewModel
class ExploreViewModel @Inject constructor(
    observePublishedEvents: ObservePublishedEventsUseCase,
    observeAttendances: ObserveAttendancesUseCase,
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeCurrentLocation: ObserveCurrentLocationUseCase,
    observeFilters: ObserveEventFiltersUseCase,
    observeWaitlist: ObserveWaitlistUseCase,
    private val filterEvents: FilterEventsUseCase,
    private val updateFilters: UpdateEventFiltersUseCase,
    private val clearFiltersUseCase: ClearEventFiltersUseCase,
    private val toggleAttendanceUseCase: ToggleAttendanceUseCase,
    private val findScheduleConflict: FindScheduleConflictUseCase,
    private val joinWaitlist: JoinWaitlistUseCase,
    private val leaveWaitlist: LeaveWaitlistUseCase,
    private val updateCurrentLocation: UpdateCurrentLocationUseCase,
    private val resources: ResourceProvider
) : ViewModel() {

    private val local = MutableStateFlow(ExploreLocal())

    private val attendancesAndWaitlist = combine(observeAttendances(), observeWaitlist(), ::Pair)

    private val content = combine(
        observePublishedEvents(),
        attendancesAndWaitlist,
        observeFilters(),
        observeCurrentUser(),
        observeCurrentLocation()
    ) { published, (attendances, waitlist), filters, user, location ->
        val attendeesByEvent = attendances.groupingBy { it.event.id }.eachCount()
        val filtered = filterEvents(published, attendeesByEvent, location, filters)

        ExploreContent(
            events = toCardItems(filtered, attendances, waitlist, user?.id, location),
            pins = filtered.map { MapPin(it.id, it.category, it.location) },
            filters = filters
        )
    }

    val uiState: StateFlow<ExploreUiState> = combine(content, local) { content, local ->
        ExploreUiState(
            view = local.view,
            events = content.events,
            pins = content.pins,
            selected = resolveSelected(content.events, local.selectedId),
            filters = content.filters,
            showFilters = local.showFilters,
            attendanceStep = local.attendanceStep,
            message = local.message,
            actionResult = local.actionResult,
            loadingEventId = local.loadingEventId.takeIf { local.actionResult is RequestResult.Loading }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExploreUiState())

    fun onViewChange(view: ExploreView) = local.update { it.copy(view = view) }

    fun onPinClick(eventId: String) = local.update { it.copy(selectedId = eventId) }

    /** "Mi ubicación": vuelve al evento más cercano. */
    fun onRecenter() = local.update { it.copy(selectedId = null) }

    /** El mapa reportó la posición real del usuario. */
    fun onUserLocation(location: Location) {
        // Solo actualiza la ubicación del dispositivo: no es una petición en línea
        viewModelScope.launch { updateCurrentLocation(location) }
    }

    fun onQueryChange(value: String) = updateFilters { it.copy(query = value) }

    fun clearFilters() = clearFiltersUseCase()

    fun onOpenFilters() = local.update { it.copy(showFilters = true) }

    fun onDismissFilters() = local.update { it.copy(showFilters = false) }

    /** Aplica el borrador de la hoja (sin tocar el texto de búsqueda) y la cierra. */
    fun onApplyFilters(draft: EventFilters) {
        updateFilters { it.withDraft(draft) }
        local.update { it.copy(showFilters = false) }
    }

    fun toggleAttendance(item: FeedEventItem) {
        val event = item.event
        when {
            item.isAttending -> confirmAttendance(event)
            item.isWaitlisted -> request(event, { leaveWaitlist(event) }) {
                RequestResult.Success(waitlistLeftMessage(resources))
            }
            item.isFull -> request(event, { joinWaitlist(event) }) { position ->
                RequestResult.Success(position?.let { waitlistJoinedMessage(resources, it) }.orEmpty())
            }
            // Antes de confirmar se revisa si choca con otro evento confirmado
            else -> request(event, { findScheduleConflict(event) }) { conflict ->
                local.update { it.copy(attendanceStep = startAttendanceFlow(event, conflict)) }
                RequestResult.Success("")
            }
        }
    }

    fun onMessageShown() = local.update { it.copy(message = null) }

    /** El usuario aceptó el diálogo actual: pasa al siguiente o, si era el último, confirma la asistencia. */
    fun onAttendanceContinue() {
        val step = local.value.attendanceStep ?: return
        val next = nextAttendanceStep(step)
        local.update { it.copy(attendanceStep = next) }
        if (next == null) confirmAttendance(step.event)
    }

    /** "Cancelar" o "No confirmar": cierra el diálogo sin cambiar la asistencia. */
    fun onAttendanceCancel() = local.update { it.copy(attendanceStep = null) }

    private fun confirmAttendance(event: Event) {
        request(event, { toggleAttendanceUseCase(event) }) { RequestResult.Success("") }
    }

    /**
     * Petición en línea sobre [event]: marca Loading antes de empezar y, al terminar, guarda el éxito o el
     * fallo (sin conexión, sin permiso…) y muestra su mensaje. Mientras una sigue en curso se ignoran las demás.
     */
    private fun <T> request(event: Event, block: suspend () -> T, onSuccess: (T) -> RequestResult) {
        if (local.value.actionResult is RequestResult.Loading) return
        viewModelScope.launch {
            local.update { it.copy(actionResult = RequestResult.Loading, loadingEventId = event.id) }

            val result = runRequest(resources, onSuccess = onSuccess, block = block)

            local.update {
                when (result) {
                    is RequestResult.Success, is RequestResult.Failure ->
                        it.copy(actionResult = result, message = result.messageOrNull() ?: it.message)
                    RequestResult.Loading -> it
                }
            }
        }
    }
}
