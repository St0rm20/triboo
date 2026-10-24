package edu.uniquindio.co.tribooo.features.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.core.component.FeedEventItem
import edu.uniquindio.co.tribooo.core.component.toCardItems
import edu.uniquindio.co.tribooo.core.util.WAITLIST_LEFT_MESSAGE
import edu.uniquindio.co.tribooo.core.util.waitlistJoinedMessage
import edu.uniquindio.co.tribooo.core.util.MapProjection
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventFilters
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
    val message: String? = null
)

/** Parte del estado que vive solo en esta pantalla. */
internal data class ExploreLocal(
    val view: ExploreView = ExploreView.MAP,
    val selectedId: String? = null,
    val showFilters: Boolean = false,
    val attendanceStep: AttendanceStep? = null,
    val message: String? = null
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
    private val leaveWaitlist: LeaveWaitlistUseCase
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
        // El mapa se ajusta a todos los publicados para que los pines no se muevan al filtrar
        val projection = MapProjection(published.map { it.location })

        ExploreContent(
            events = toCardItems(filtered, attendances, waitlist, user?.id, location),
            pins = filtered.map { MapPin(it.id, it.category, projection.project(it.location)) },
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
            message = local.message
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExploreUiState())

    fun onViewChange(view: ExploreView) = local.update { it.copy(view = view) }

    fun onPinClick(eventId: String) = local.update { it.copy(selectedId = eventId) }

    /** "Mi ubicación": vuelve al evento más cercano. */
    fun onRecenter() = local.update { it.copy(selectedId = null) }

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
        when {
            item.isAttending -> confirmAttendance(item.event)
            item.isWaitlisted -> viewModelScope.launch {
                leaveWaitlist(item.event)
                local.update { it.copy(message = WAITLIST_LEFT_MESSAGE) }
            }
            item.isFull -> viewModelScope.launch {
                joinWaitlist(item.event)?.let { position ->
                    local.update { it.copy(message = waitlistJoinedMessage(position)) }
                }
            }
            else -> viewModelScope.launch {
                val step = startAttendanceFlow(item.event, findScheduleConflict(item.event))
                local.update { it.copy(attendanceStep = step) }
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
        viewModelScope.launch { toggleAttendanceUseCase(event) }
    }
}
