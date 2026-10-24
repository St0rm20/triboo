package edu.uniquindio.co.tribooo.features.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.core.util.APP_ZONE
import edu.uniquindio.co.tribooo.core.util.agendaSummary
import edu.uniquindio.co.tribooo.core.util.countThisWeek
import edu.uniquindio.co.tribooo.core.util.label
import edu.uniquindio.co.tribooo.core.util.monthCells
import edu.uniquindio.co.tribooo.core.util.occursOn
import edu.uniquindio.co.tribooo.core.util.selectedDayLabel
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.usecase.agenda.AgendaData
import edu.uniquindio.co.tribooo.domain.usecase.agenda.ObserveAgendaUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth

/** Celda de un día del mes. */
data class AgendaCell(
    val date: LocalDate,
    val hasEvents: Boolean,
    /** El usuario confirmó asistencia a algún evento de ese día. */
    val hasConfirmed: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean
)

/** Evento de la lista del día; [confirmed] muestra la píldora "Vas". */
data class AgendaEventItem(val event: Event, val confirmed: Boolean)

data class AgendaUiState(
    val monthLabel: String = "",
    /** Un elemento por celda de la cuadrícula; null para los huecos antes del día 1. */
    val cells: List<AgendaCell?> = emptyList(),
    val selectedDayLabel: String = "",
    val events: List<AgendaEventItem> = emptyList(),
    val summary: String = ""
)

/** Mes visible y día elegido. */
data class AgendaSelection(val month: YearMonth, val day: LocalDate) {

    /** Mes [delta] meses más allá; el día pasa a hoy si es el mes actual y, si no, al día 1. */
    fun shiftMonth(delta: Long, today: LocalDate): AgendaSelection {
        val target = month.plusMonths(delta)
        return AgendaSelection(target, if (target == YearMonth.from(today)) today else target.atDay(1))
    }

    fun selectDay(date: LocalDate) = copy(day = date)

    companion object {
        fun of(today: LocalDate) = AgendaSelection(YearMonth.from(today), today)
    }
}

/** Arma el estado de la pantalla a partir de los datos y la selección. */
internal fun buildAgendaState(data: AgendaData, selection: AgendaSelection, today: LocalDate): AgendaUiState {
    val confirmedIds = data.confirmed.map { it.id }.toSet()
    val cells = monthCells(selection.month).map { date ->
        date?.let {
            val dayEvents = data.published.filter { event -> event.occursOn(date) }
            AgendaCell(
                date = date,
                hasEvents = dayEvents.isNotEmpty(),
                hasConfirmed = dayEvents.any { event -> event.id in confirmedIds },
                isToday = date == today,
                isSelected = date == selection.day
            )
        }
    }
    val events = data.published
        .filter { it.occursOn(selection.day) }
        .sortedWith(compareBy({ it.startDate }, { it.title }))
        .map { AgendaEventItem(it, it.id in confirmedIds) }

    return AgendaUiState(
        monthLabel = selection.month.label(),
        cells = cells,
        selectedDayLabel = selectedDayLabel(selection.day, events.size),
        events = events,
        summary = agendaSummary(data.confirmed.size, countThisWeek(data.confirmed, today))
    )
}

@HiltViewModel
class AgendaViewModel @Inject constructor(
    observeAgenda: ObserveAgendaUseCase
) : ViewModel() {

    // Se usa la fecha real: los eventos de prueba son relativos a hoy
    private val today = LocalDate.now(APP_ZONE)

    private val selection = MutableStateFlow(AgendaSelection.of(today))

    val uiState: StateFlow<AgendaUiState> = combine(observeAgenda(), selection) { data, selection ->
        buildAgendaState(data, selection, today)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AgendaUiState())

    fun onPreviousMonth() = selection.update { it.shiftMonth(-1, today) }

    fun onNextMonth() = selection.update { it.shiftMonth(1, today) }

    fun onDaySelected(date: LocalDate) = selection.update { it.selectDay(date) }
}
