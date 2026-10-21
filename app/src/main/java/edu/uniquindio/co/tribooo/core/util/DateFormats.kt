package edu.uniquindio.co.tribooo.core.util

import java.text.NumberFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Zona horaria de la app (Armenia, Quindío). */
val APP_ZONE: ZoneId = ZoneId.of("America/Bogota")

private val DAYS = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
private val DAYS_LONG = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
private val MONTHS = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
private val MONTHS_LONG = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)
private val TIME = DateTimeFormatter.ofPattern("HH:mm")
private val COP = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO"))

fun Instant.atAppZone(): ZonedDateTime = atZone(APP_ZONE)

/** "Lunes 5 de octubre" */
fun LocalDate.longLabel(): String =
    "${DAYS_LONG[dayOfWeek.value - 1]} $dayOfMonth de ${MONTHS_LONG[monthValue - 1]}"

/** "Sáb 26 sep" */
fun LocalDate.shortLabel(): String = "${DAYS[dayOfWeek.value - 1]} $dayOfMonth ${MONTHS[monthValue - 1]}"

/** "Sáb 26 sep 2026" */
fun LocalDate.shortLabelWithYear(): String = "${shortLabel()} $year"

/** "sep" */
fun LocalDate.monthShort(): String = MONTHS[monthValue - 1]

/** "18:00" */
fun LocalTime.label(): String = format(TIME)

/** "Sáb 26 sep · 18:00" */
fun dateTimeLabel(date: LocalDate, time: LocalTime): String = "${date.shortLabel()} · ${time.label()}"

/** "Sáb 26 sep · 18:00 – 20:30" o "Sáb 10 oct – Dom 11 oct · 08:00 – 08:00" */
fun eventWhenLabel(start: Instant, end: Instant): String = "${eventDaysLabel(start, end)} · ${eventHoursLabel(start, end)}"

/** "Sáb 26 sep" o "Sáb 10 oct – Dom 11 oct" */
fun eventDaysLabel(start: Instant, end: Instant): String {
    val s = start.atAppZone().toLocalDate()
    val e = end.atAppZone().toLocalDate()
    return if (s == e) s.shortLabel() else "${s.shortLabel()} – ${e.shortLabel()}"
}

/** "18:00 – 20:30" */
fun eventHoursLabel(start: Instant, end: Instant): String =
    "${start.atAppZone().toLocalTime().label()} – ${end.atAppZone().toLocalTime().label()}"

/** "ahora", "hace 40 min", "hace 2 h" o "hace 3 d" */
fun Instant.agoLabel(now: Instant = Instant.now()): String {
    val minutes = Duration.between(this, now).toMinutes()
    return when {
        minutes < 1 -> "ahora"
        minutes < 60 -> "hace $minutes min"
        minutes < 24 * 60 -> "hace ${minutes / 60} h"
        else -> "hace ${minutes / (24 * 60)} d"
    }
}

/** "1.234" */
fun Number.formatThousands(): String = COP.format(this)

/** "$5.000" */
fun Number.formatMoney(): String = "$" + formatThousands()
