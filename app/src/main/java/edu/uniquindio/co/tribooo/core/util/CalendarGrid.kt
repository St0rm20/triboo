package edu.uniquindio.co.tribooo.core.util

import edu.uniquindio.co.tribooo.domain.model.Event
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

/** Iniciales de los días de la semana, que empieza en lunes. */
val WEEKDAY_LETTERS = listOf("L", "M", "M", "J", "V", "S", "D")

/** Celdas del mes en filas de siete: null para los huecos antes del día 1 (la semana inicia en lunes). */
fun monthCells(month: YearMonth): List<LocalDate?> {
    val leading = month.atDay(1).dayOfWeek.value - 1
    return List(leading) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
}

/** Fecha de inicio del evento en la zona de la app. */
private fun Event.firstDay(): LocalDate = startDate.atAppZone().toLocalDate()

/** Fecha de fin del evento en la zona de la app. */
private fun Event.lastDay(): LocalDate = endDate.atAppZone().toLocalDate()

/** True si el evento ocurre en [day]; los multidía cubren todos los días entre inicio y fin. */
fun Event.occursOn(day: LocalDate): Boolean = day >= firstDay() && day <= lastDay()

/** True si el evento toca algún día del rango de [from] a [to], ambos incluidos. */
fun Event.touches(from: LocalDate, to: LocalDate): Boolean = firstDay() <= to && lastDay() >= from

/** Lunes y domingo de la semana de [day]. */
fun weekOf(day: LocalDate): Pair<LocalDate, LocalDate> {
    val monday = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    return monday to monday.plusDays(6)
}

/** Cantidad de [events] que tocan la semana (lunes a domingo) de [today]. */
fun countThisWeek(events: List<Event>, today: LocalDate): Int {
    val (monday, sunday) = weekOf(today)
    return events.count { it.touches(monday, sunday) }
}

/** "1 evento confirmado · 0 esta semana" o "3 eventos confirmados · 2 esta semana". */
fun agendaSummary(confirmed: Int, thisWeek: Int): String {
    val total = if (confirmed == 1) "1 evento confirmado" else "$confirmed eventos confirmados"
    return "$total · $thisWeek esta semana"
}

/** "Lunes 5 de octubre · 2 eventos", "· 1 evento" o "· sin eventos". */
fun selectedDayLabel(day: LocalDate, count: Int): String {
    val events = when (count) {
        0 -> "sin eventos"
        1 -> "1 evento"
        else -> "$count eventos"
    }
    return "${day.longLabel()} · $events"
}
