package edu.uniquindio.co.tribooo.features.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.core.util.MessageTarget
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.ResourceProvider
import edu.uniquindio.co.tribooo.core.util.SnackbarMessenger
import edu.uniquindio.co.tribooo.core.util.messageOrNull
import edu.uniquindio.co.tribooo.core.util.runRequest
import edu.uniquindio.co.tribooo.core.component.FeedEventItem
import edu.uniquindio.co.tribooo.core.component.toCardItems
import edu.uniquindio.co.tribooo.core.util.waitlistLeftMessage
import edu.uniquindio.co.tribooo.core.util.waitlistJoinedMessage
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventFilters
import edu.uniquindio.co.tribooo.domain.model.UserRole
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
import edu.uniquindio.co.tribooo.domain.usecase.moderation.ObserveModerationCountsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.notification.ObserveUnreadNotificationsCountUseCase
import edu.uniquindio.co.tribooo.domain.usecase.waitlist.JoinWaitlistUseCase
import edu.uniquindio.co.tribooo.domain.usecase.waitlist.LeaveWaitlistUseCase
import edu.uniquindio.co.tribooo.domain.usecase.waitlist.ObserveWaitlistUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedUiState(
    val query: String = "",
    val selectedCategories: Set<String> = emptySet(),
    val categories: List<Category> = Category.defaultCategories,
    val events: List<FeedEventItem> = emptyList(),
    val unreadNotifications: Int = 0,
    /** El administrador ve el martillo de moderación en vez de "+". */
    val isAdmin: Boolean = false,
    val pendingModeration: Int = 0,
    /** Filtros compartidos con Explorar y cuántos cuentan en la insignia. */
    val filters: EventFilters = EventFilters(),
    val showFilters: Boolean = false,
    /** Diálogo que el usuario debe aceptar antes de confirmar su asistencia. */
    val attendanceStep: AttendanceStep? = null,
    val message: String? = null,
    /** Resultado de la última petición hecha desde una tarjeta (asistencia o lista de espera). */
    val actionResult: RequestResult? = null,
    /** Evento cuya petición sigue en curso; su botón muestra el indicador de carga. */
    val loadingEventId: String? = null
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    observePublishedEvents: ObservePublishedEventsUseCase,
    observeAttendances: ObserveAttendancesUseCase,
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeCurrentLocation: ObserveCurrentLocationUseCase,
    observeUnreadNotifications: ObserveUnreadNotificationsCountUseCase,
    observeModerationCounts: ObserveModerationCountsUseCase,
    observeFilters: ObserveEventFiltersUseCase,
    observeWaitlist: ObserveWaitlistUseCase,
    private val filterEvents: FilterEventsUseCase,
    private val updateFilters: UpdateEventFiltersUseCase,
    private val clearFiltersUseCase: ClearEventFiltersUseCase,
    private val toggleAttendanceUseCase: ToggleAttendanceUseCase,
    private val findScheduleConflict: FindScheduleConflictUseCase,
    private val joinWaitlist: JoinWaitlistUseCase,
    private val leaveWaitlist: LeaveWaitlistUseCase,
    messenger: SnackbarMessenger,
    private val resources: ResourceProvider
) : ViewModel() {

    /** Avisos enviados al feed desde otra pantalla (por ejemplo, "Inicio de sesión exitoso"). */
    val messages: Flow<String> = messenger.messages(MessageTarget.FEED)

    private val showFilters = MutableStateFlow(false)
    private val attendanceStep = MutableStateFlow<AttendanceStep?>(null)
    private val message = MutableStateFlow<String?>(null)
    private val actionResult = MutableStateFlow<RequestResult?>(null)
    private val loadingEventId = MutableStateFlow<String?>(null)

    private val attendancesAndWaitlist = combine(observeAttendances(), observeWaitlist(), ::Pair)

    private val feed = combine(
        observePublishedEvents(),
        attendancesAndWaitlist,
        observeFilters(),
        observeCurrentUser(),
        observeCurrentLocation()
    ) { events, (attendances, waitlist), filters, currentUser, location ->
        val attendeesByEvent = attendances.groupingBy { it.event.id }.eachCount()
        val filtered = filterEvents(events, attendeesByEvent, location, filters)

        FeedUiState(
            query = filters.query,
            selectedCategories = filters.categories,
            events = toCardItems(filtered, attendances, waitlist, currentUser?.id, location),
            filters = filters
        )
    }

    private val feedWithAccount = combine(
        feed, observeUnreadNotifications(), observeCurrentUser(), observeModerationCounts(), showFilters
    ) { feed, unread, user, moderation, showFilters ->
        val isAdmin = user?.role == UserRole.ADMIN
        feed.copy(
            unreadNotifications = unread,
            isAdmin = isAdmin,
            pendingModeration = if (isAdmin) moderation.pending else 0,
            showFilters = showFilters
        )
    }

    private val request = combine(actionResult, loadingEventId, ::Pair)

    val uiState: StateFlow<FeedUiState> = combine(feedWithAccount, attendanceStep, message, request) { state, step, message, (result, loadingId) ->
        state.copy(
            attendanceStep = step,
            message = message,
            actionResult = result,
            loadingEventId = loadingId.takeIf { result is RequestResult.Loading }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeedUiState())

    fun onQueryChange(value: String) = updateFilters { it.copy(query = value) }

    fun toggleCategory(key: String) = updateFilters {
        it.copy(categories = if (key in it.categories) it.categories - key else it.categories + key)
    }

    fun clearFilters() = clearFiltersUseCase()

    fun onOpenFilters() = showFilters.update { true }

    fun onDismissFilters() = showFilters.update { false }

    /** Aplica el borrador de la hoja (sin tocar el texto de búsqueda) y la cierra. */
    fun onApplyFilters(draft: EventFilters) {
        updateFilters { it.withDraft(draft) }
        showFilters.update { false }
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
                attendanceStep.value = startAttendanceFlow(event, conflict)
                RequestResult.Success("")
            }
        }
    }

    fun onMessageShown() = message.update { null }

    /** El usuario aceptó el diálogo actual: pasa al siguiente o, si era el último, confirma la asistencia. */
    fun onAttendanceContinue() {
        val step = attendanceStep.value ?: return
        val next = nextAttendanceStep(step)
        attendanceStep.value = next
        if (next == null) confirmAttendance(step.event)
    }

    /** "Cancelar" o "No confirmar": cierra el diálogo sin cambiar la asistencia. */
    fun onAttendanceCancel() {
        attendanceStep.value = null
    }

    private fun confirmAttendance(event: Event) {
        request(event, { toggleAttendanceUseCase(event) }) { RequestResult.Success("") }
    }

    /**
     * Petición en línea sobre [event]: marca Loading antes de empezar y, al terminar, guarda el éxito o el
     * fallo (sin conexión, sin permiso…) y muestra su mensaje. Mientras una sigue en curso se ignoran las demás.
     */
    private fun <T> request(event: Event, block: suspend () -> T, onSuccess: (T) -> RequestResult) {
        if (actionResult.value is RequestResult.Loading) return
        viewModelScope.launch {
            loadingEventId.value = event.id
            actionResult.value = RequestResult.Loading

            val result = runRequest(resources, onSuccess = onSuccess, block = block)

            actionResult.value = result
            when (result) {
                is RequestResult.Success, is RequestResult.Failure -> result.messageOrNull()?.let { message.value = it }
                RequestResult.Loading -> Unit
            }
        }
    }
}
