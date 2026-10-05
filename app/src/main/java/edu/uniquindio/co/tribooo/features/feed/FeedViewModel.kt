package edu.uniquindio.co.tribooo.features.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.uniquindio.co.tribooo.data.EventRepository
import edu.uniquindio.co.tribooo.data.MockData
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Evento listo para mostrarse en una tarjeta del feed. */
data class FeedEventItem(
    val event: Event,
    val attendeesCount: Int,
    val attendeeInitials: List<String>,
    val isAttending: Boolean,
    val isMine: Boolean,
    val distanceKm: Double
) {
    val isFull: Boolean get() = event.cupo != null && attendeesCount >= event.cupo
    val occupancy: Float get() = event.cupo?.let { (attendeesCount.toFloat() / it).coerceAtMost(1f) } ?: 0f
}

data class FeedUiState(
    val query: String = "",
    val selectedCategories: Set<String> = emptySet(),
    val categories: List<Category> = Category.defaultCategories,
    val events: List<FeedEventItem> = emptyList()
)

class FeedViewModel : ViewModel() {

    private val currentUser = MockData.currentUser
    private val query = MutableStateFlow("")
    private val selectedCategories = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<FeedUiState> = combine(
        EventRepository.events,
        EventRepository.attendances,
        query,
        selectedCategories
    ) { events, attendances, q, categories ->
        val attendeesByEvent = attendances.groupBy { it.event.id }
        val text = q.trim().lowercase()

        val items = events
            // Solo los eventos aprobados por moderación aparecen en el feed
            .filter { it.status == EventStatus.ACTIVO }
            .filter { categories.isEmpty() || it.category.key in categories }
            .filter {
                text.isEmpty() || "${it.title} ${it.place} ${it.neighborhood}".lowercase().contains(text)
            }
            .map { event ->
                val attendees = attendeesByEvent[event.id].orEmpty()
                FeedEventItem(
                    event = event,
                    attendeesCount = attendees.size,
                    attendeeInitials = attendees.take(3).map { initials(it.user.name) },
                    isAttending = attendees.any { it.user.id == currentUser.id },
                    isMine = event.organizer.id == currentUser.id,
                    distanceKm = distanceKm(MockData.currentLocation, event.location)
                )
            }
            .sortedBy { it.distanceKm }

        FeedUiState(query = q, selectedCategories = categories, events = items)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeedUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun toggleCategory(key: String) {
        selectedCategories.update { if (key in it) it - key else it + key }
    }

    fun clearFilters() {
        query.value = ""
        selectedCategories.value = emptySet()
    }

    fun toggleAttendance(item: FeedEventItem) {
        // Sin lista de espera en el modelo: si el cupo está lleno solo se permite cancelar
        if (item.isFull && !item.isAttending) return
        EventRepository.toggleAttendance(currentUser, item.event)
    }

    private fun initials(name: String): String =
        name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

    /** Distancia en km entre dos coordenadas (fórmula de Haversine). */
    private fun distanceKm(from: Location, to: Location): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLon = Math.toRadians(to.longitude - from.longitude)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(from.latitude)) * cos(Math.toRadians(to.latitude)) * sin(dLon / 2).pow(2)
        return 2 * earthRadiusKm * asin(sqrt(a))
    }
}
