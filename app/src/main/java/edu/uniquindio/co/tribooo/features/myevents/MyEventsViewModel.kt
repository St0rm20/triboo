package edu.uniquindio.co.tribooo.features.myevents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.core.util.averageRating
import edu.uniquindio.co.tribooo.core.util.myEventFooter
import edu.uniquindio.co.tribooo.core.util.myEventSummary
import edu.uniquindio.co.tribooo.core.util.myEventsLabel
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.usecase.attendance.ObserveAttendancesUseCase
import edu.uniquindio.co.tribooo.domain.usecase.comment.ObserveCommentsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.ObserveMyEventsUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant

/** Pestañas de la pantalla; cada una muestra los eventos de un estado. */
enum class MyEventsTab(val label: String, val status: EventStatus) {
    ACTIVOS("Activos", EventStatus.ACTIVO),
    PENDIENTES("Pendientes", EventStatus.PENDIENTE),
    FINALIZADOS("Finalizados", EventStatus.FINALIZADO),
    RECHAZADOS("Rechazados", EventStatus.RECHAZADO)
}

/** Evento listo para mostrarse en una tarjeta de "Mis eventos". */
data class MyEventItem(
    val event: Event,
    val statusLabel: String,
    val summary: String,
    val footer: String
) {
    val status: EventStatus get() = event.status
}

data class MyEventsUiState(
    val selectedTab: MyEventsTab = MyEventsTab.ACTIVOS,
    val events: List<MyEventItem> = emptyList()
)

/** Eventos de la pestaña elegida, del más reciente al más antiguo, con sus textos ya calculados. */
internal fun buildMyEventItems(
    events: List<Event>,
    tab: MyEventsTab,
    attendances: List<Attendance>,
    comments: List<Comment>,
    now: Instant = Instant.now()
): List<MyEventItem> {
    val attendeesByEvent = attendances.groupBy { it.event.id }
    return events
        .filter { it.status == tab.status }
        .sortedByDescending { it.startDate }
        .map { event ->
            MyEventItem(
                event = event,
                statusLabel = event.status.myEventsLabel(),
                summary = myEventSummary(
                    event = event,
                    attendees = attendeesByEvent[event.id].orEmpty().size,
                    rating = averageRating(event.id, comments)
                ),
                footer = myEventFooter(event, now)
            )
        }
}

@HiltViewModel
class MyEventsViewModel @Inject constructor(
    observeMyEvents: ObserveMyEventsUseCase,
    observeAttendances: ObserveAttendancesUseCase,
    observeComments: ObserveCommentsUseCase
) : ViewModel() {

    private val selectedTab = MutableStateFlow(MyEventsTab.ACTIVOS)

    val uiState: StateFlow<MyEventsUiState> = combine(
        observeMyEvents(),
        observeAttendances(),
        observeComments(),
        selectedTab
    ) { events, attendances, comments, tab ->
        MyEventsUiState(
            selectedTab = tab,
            events = buildMyEventItems(events, tab, attendances, comments)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MyEventsUiState())

    fun onTabSelected(tab: MyEventsTab) {
        selectedTab.value = tab
    }
}
