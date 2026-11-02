package edu.uniquindio.co.tribooo.core.util

import edu.uniquindio.co.tribooo.R
import java.text.NumberFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Zona horaria de la app (Armenia, Quindío). */
val APP_ZONE: ZoneId = ZoneId.of("America/Bogota")

private val TIME = DateTimeFormatter.ofPattern("HH:mm")
private val COP = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO"))

fun Instant.atAppZone(): ZonedDateTime = atZone(APP_ZONE)

private fun ResourceProvider.days(): List<String> = getStringArray(R.array.days_short)
private fun ResourceProvider.daysLong(): List<String> = getStringArray(R.array.days_long)
private fun ResourceProvider.months(): List<String> = getStringArray(R.array.months_short)
private fun ResourceProvider.monthsLong(): List<String> = getStringArray(R.array.months_long)

/** "Lunes 5 de octubre" */
fun LocalDate.longLabel(rp: ResourceProvider): String =
    rp.getString(R.string.date_long, rp.daysLong()[dayOfWeek.value - 1], dayOfMonth, rp.monthsLong()[monthValue - 1])

/** "Sáb 26 sep" */
fun LocalDate.shortLabel(rp: ResourceProvider): String =
    rp.getString(R.string.date_short, rp.days()[dayOfWeek.value - 1], dayOfMonth, rp.months()[monthValue - 1])

/** "Sáb 26 sep 2026" */
fun LocalDate.shortLabelWithYear(rp: ResourceProvider): String =
    rp.getString(R.string.date_short_with_year, shortLabel(rp), year)

/** "18 jul 2026" */
fun LocalDate.dayMonthYearLabel(rp: ResourceProvider): String =
    rp.getString(R.string.date_day_month_year, dayOfMonth, rp.months()[monthValue - 1], year)

/** "18 jul 2026, 14:30" */
fun Instant.dayMonthYearTimeLabel(rp: ResourceProvider): String =
    atAppZone().let { rp.getString(R.string.date_with_time, it.toLocalDate().dayMonthYearLabel(rp), it.toLocalTime().label()) }

/** "marzo 2025" */
fun Instant.monthYearLabel(rp: ResourceProvider): String =
    atAppZone().let { rp.getString(R.string.date_month_year, rp.monthsLong()[it.monthValue - 1], it.year) }

/** "octubre 2026" */
fun YearMonth.label(rp: ResourceProvider): String =
    rp.getString(R.string.date_month_year, rp.monthsLong()[monthValue - 1], year)

/** "sep" */
fun LocalDate.monthShort(rp: ResourceProvider): String = rp.months()[monthValue - 1]

/** "18:00" */
fun LocalTime.label(): String = format(TIME)

/** "Sáb 26 sep · 18:00" */
fun dateTimeLabel(rp: ResourceProvider, date: LocalDate, time: LocalTime): String =
    rp.getString(R.string.common_dot_join, date.shortLabel(rp), time.label())

/** "Sáb 26 sep · 18:00 – 20:30" o "Sáb 10 oct – Dom 11 oct · 08:00 – 08:00" */
fun eventWhenLabel(rp: ResourceProvider, start: Instant, end: Instant): String =
    rp.getString(R.string.common_dot_join, eventDaysLabel(rp, start, end), eventHoursLabel(start, end))

/** "Sáb 26 sep" o "Sáb 10 oct – Dom 11 oct" */
fun eventDaysLabel(rp: ResourceProvider, start: Instant, end: Instant): String {
    val s = start.atAppZone().toLocalDate()
    val e = end.atAppZone().toLocalDate()
    return if (s == e) s.shortLabel(rp) else rp.getString(R.string.date_range, s.shortLabel(rp), e.shortLabel(rp))
}

/** "18:00 – 20:30" */
fun eventHoursLabel(start: Instant, end: Instant): String =
    "${start.atAppZone().toLocalTime().label()} – ${end.atAppZone().toLocalTime().label()}"

/** "ahora", "hace 40 min", "hace 2 h" o "hace 3 d" */
fun Instant.agoLabel(rp: ResourceProvider, now: Instant = Instant.now()): String {
    val minutes = Duration.between(this, now).toMinutes()
    return when {
        minutes < 1 -> rp.getString(R.string.time_now)
        minutes < 60 -> rp.getString(R.string.time_ago_minutes, minutes)
        minutes < 24 * 60 -> rp.getString(R.string.time_ago_hours, minutes / 60)
        else -> rp.getString(R.string.time_ago_days_short, minutes / (24 * 60))
    }
}

/** "ahora", "hace 25 min", "hace 2 h", "ayer" o "hace 3 días" (por día calendario) */
fun Instant.relativeLabel(rp: ResourceProvider, now: Instant = Instant.now(), zone: ZoneId = APP_ZONE): String {
    val minutes = Duration.between(this, now).toMinutes()
    val daysAgo = ChronoUnit.DAYS.between(atZone(zone).toLocalDate(), now.atZone(zone).toLocalDate())
    return when {
        daysAgo >= 2 -> rp.getQuantityString(R.plurals.time_ago_days, daysAgo.toInt(), daysAgo)
        daysAgo == 1L -> rp.getString(R.string.time_yesterday)
        minutes < 1 -> rp.getString(R.string.time_now)
        minutes < 60 -> rp.getString(R.string.time_ago_minutes, minutes)
        else -> rp.getString(R.string.time_ago_hours, minutes / 60)
    }
}

/** "1.234" */
fun Number.formatThousands(): String = COP.format(this)

/** "$5.000" */
fun Number.formatMoney(): String = "$" + formatThousands()
