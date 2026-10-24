package edu.uniquindio.co.tribooo.features.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.uniquindio.co.tribooo.core.util.distanceKmTo
import edu.uniquindio.co.tribooo.core.util.initials
import edu.uniquindio.co.tribooo.core.util.isFull
import edu.uniquindio.co.tribooo.core.util.occupancy
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.usecase.attendance.ObserveAttendancesUseCase
import edu.uniquindio.co.tribooo.domain.usecase.attendance.ToggleAttendanceUseCase
import edu.uniquindio.co.tribooo.domain.usecase.auth.ObserveCurrentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.ObservePublishedEventsUseCase
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

/** Evento listo para mostrarse en una tarjeta del feed. */
data class FeedEventItem(
    val event: Event,
    val attendeesCount: Int,
    val attendeeInitials: List<String>,
    val isAttending: Boolean,
    val isMine: Boolean,
    val distanceKm: Double
) {
    val isFull: Boolean get() = event.isFull(attendeesCount)
    val occupancy: Float get() = event.occupancy(attendeesCount)
}

data class FeedUiState(
    val query: String = "",
    val selectedCategories: Set<String> = emptySet(),
    val categories: List<Category> = Category.defaultCategories,
    val events: List<FeedEventItem> = emptyList(),
    val unreadNotifications: Int = 0,
    /** El administrador ve el martillo de moderación en vez de "+". */
    val isAdmin: Boolean = false,
    val pendingModeration: Int = 0
)

/** Búsqueda y categorías elegidas (se agrupan porque `combine` admite máximo 5 flujos). */
private data class FeedFilters(
    val query: String = "",
    val categories: Set<String> = emptySet()
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    observePublishedEvents: ObservePublishedEventsUseCase,
    observeAttendances: ObserveAttendancesUseCase,
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeCurrentLocation: ObserveCurrentLocationUseCase,
    observeUnreadNotifications: ObserveUnreadNotificationsCountUseCase,
    observeModerationCounts: ObserveModerationCountsUseCase,
    private val toggleAttendanceUseCase: ToggleAttendanceUseCase
) : ViewModel() {

    private val filters = MutableStateFlow(FeedFilters())

    private val feed = combine(
        observePublishedEvents(),
        observeAttendances(),
        filters,
        observeCurrentUser(),
        observeCurrentLocation()
    ) { events, attendances, filters, currentUser, location ->
        val attendeesByEvent = attendances.groupBy { it.event.id }
        val text = filters.query.trim().lowercase()
        val categories = filters.categories

        val items = events
            .filter { categories.isEmpty() || it.category.key in categories }
            .filter {
                text.isEmpty() || "${it.title} ${it.place} ${it.neighborhood}".lowercase().contains(text)
            }
            .map { event ->
                val attendees = attendeesByEvent[event.id].orEmpty()
                FeedEventItem(
                    event = event,
                    attendeesCount = attendees.size,
                    attendeeInitials = attendees.take(3).map { it.user.name.initials() },
                    isAttending = attendees.any { it.user.id == currentUser?.id },
                    isMine = event.organizer.id == currentUser?.id,
                    distanceKm = location.distanceKmTo(event.location)
                )
            }
            .sortedBy { it.distanceKm }

        FeedUiState(query = filters.query, selectedCategories = categories, events = items)
    }

    val uiState: StateFlow<FeedUiState> = combine(
        feed, observeUnreadNotifications(), observeCurrentUser(), observeModerationCounts()
    ) { feed, unread, user, moderation ->
        val isAdmin = user?.role == UserRole.ADMIN
        feed.copy(
            unreadNotifications = unread,
            isAdmin = isAdmin,
            pendingModeration = if (isAdmin) moderation.pending else 0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeedUiState())

    fun onQueryChange(value: String) {
        filters.update { it.copy(query = value) }
    }

    fun toggleCategory(key: String) {
        filters.update { it.copy(categories = if (key in it.categories) it.categories - key else it.categories + key) }
    }

    fun clearFilters() {
        filters.value = FeedFilters()
    }

    fun toggleAttendance(item: FeedEventItem) {
        // Sin lista de espera en el modelo: si el cupo está lleno solo se permite cancelar
        if (item.isFull && !item.isAttending) return
        viewModelScope.launch { toggleAttendanceUseCase(item.event) }
    }
}
