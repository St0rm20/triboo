package edu.uniquindio.co.tribooo.features.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.core.component.FeedEventItem
import edu.uniquindio.co.tribooo.core.component.toCardItems
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.EventFilters
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.usecase.attendance.ObserveAttendancesUseCase
import edu.uniquindio.co.tribooo.domain.usecase.attendance.ToggleAttendanceUseCase
import edu.uniquindio.co.tribooo.domain.usecase.auth.ObserveCurrentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.ObservePublishedEventsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.filters.ClearEventFiltersUseCase
import edu.uniquindio.co.tribooo.domain.usecase.filters.FilterEventsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.filters.ObserveEventFiltersUseCase
import edu.uniquindio.co.tribooo.domain.usecase.filters.UpdateEventFiltersUseCase
import edu.uniquindio.co.tribooo.domain.usecase.location.ObserveCurrentLocationUseCase
import edu.uniquindio.co.tribooo.domain.usecase.moderation.ObserveModerationCountsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.notification.ObserveUnreadNotificationsCountUseCase
import jakarta.inject.Inject
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
    val showFilters: Boolean = false
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
    private val filterEvents: FilterEventsUseCase,
    private val updateFilters: UpdateEventFiltersUseCase,
    private val clearFiltersUseCase: ClearEventFiltersUseCase,
    private val toggleAttendanceUseCase: ToggleAttendanceUseCase
) : ViewModel() {

    private val showFilters = MutableStateFlow(false)

    private val feed = combine(
        observePublishedEvents(),
        observeAttendances(),
        observeFilters(),
        observeCurrentUser(),
        observeCurrentLocation()
    ) { events, attendances, filters, currentUser, location ->
        val attendeesByEvent = attendances.groupingBy { it.event.id }.eachCount()
        val filtered = filterEvents(events, attendeesByEvent, location, filters)

        FeedUiState(
            query = filters.query,
            selectedCategories = filters.categories,
            events = toCardItems(filtered, attendances, currentUser?.id, location),
            filters = filters
        )
    }

    val uiState: StateFlow<FeedUiState> = combine(
        feed, observeUnreadNotifications(), observeCurrentUser(), observeModerationCounts(), showFilters
    ) { feed, unread, user, moderation, showFilters ->
        val isAdmin = user?.role == UserRole.ADMIN
        feed.copy(
            unreadNotifications = unread,
            isAdmin = isAdmin,
            pendingModeration = if (isAdmin) moderation.pending else 0,
            showFilters = showFilters
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
        // Sin lista de espera en el modelo: si el cupo está lleno solo se permite cancelar
        if (item.isFull && !item.isAttending) return
        viewModelScope.launch { toggleAttendanceUseCase(item.event) }
    }
}
