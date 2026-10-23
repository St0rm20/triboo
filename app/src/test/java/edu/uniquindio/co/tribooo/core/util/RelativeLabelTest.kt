package edu.uniquindio.co.tribooo.core.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.Instant

class RelativeLabelTest {

    // Mediodía en Armenia (UTC-5), para que restar minutos u horas no cambie de día
    private val now: Instant = Instant.parse("2026-10-23T17:00:00Z")

    private fun agoMinutes(minutes: Long) = now.minus(Duration.ofMinutes(minutes)).relativeLabel(now)

    @Test
    fun showsMinutes() {
        assertEquals("ahora", agoMinutes(0))
        assertEquals("hace 25 min", agoMinutes(25))
    }

    @Test
    fun showsHours() {
        assertEquals("hace 1 h", agoMinutes(60))
        assertEquals("hace 3 h", agoMinutes(200))
    }

    @Test
    fun showsYesterdayByCalendarDay() {
        assertEquals("ayer", Instant.parse("2026-10-22T06:00:00Z").relativeLabel(now))
        assertEquals("ayer", Instant.parse("2026-10-22T23:00:00Z").relativeLabel(now))
    }

    @Test
    fun showsDays() {
        assertEquals("hace 2 días", Instant.parse("2026-10-21T20:00:00Z").relativeLabel(now))
        assertEquals("hace 4 días", Instant.parse("2026-10-19T15:00:00Z").relativeLabel(now))
    }
}
