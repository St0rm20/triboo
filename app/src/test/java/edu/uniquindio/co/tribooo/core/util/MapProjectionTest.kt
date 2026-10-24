package edu.uniquindio.co.tribooo.core.util

import edu.uniquindio.co.tribooo.domain.model.Location
import org.junit.Assert.assertEquals
import org.junit.Test

class MapProjectionTest {

    private val delta = 1e-5f

    @Test
    fun singleLocationGoesToTheCenter() {
        val point = MapProjection(listOf(Location(4.5, -75.6))).project(Location(4.5, -75.6))

        assertEquals(0.5f, point.x, delta)
        assertEquals(0.5f, point.y, delta)
    }

    @Test
    fun extremesLandOnTheMargins() {
        val sw = Location(4.0, -76.0)
        val ne = Location(5.0, -75.0)
        val projection = MapProjection(listOf(sw, ne, Location(4.5, -75.5)))

        // Sur-oeste: abajo a la izquierda; nor-este: arriba a la derecha
        val southWest = projection.project(sw)
        assertEquals(MAP_MARGIN, southWest.x, delta)
        assertEquals(1f - MAP_MARGIN, southWest.y, delta)
        val northEast = projection.project(ne)
        assertEquals(1f - MAP_MARGIN, northEast.x, delta)
        assertEquals(MAP_MARGIN, northEast.y, delta)
        val middle = projection.project(Location(4.5, -75.5))
        assertEquals(0.5f, middle.x, delta)
        assertEquals(0.5f, middle.y, delta)
    }

    @Test
    fun equalLatitudesOrLongitudesDoNotDivideByZero() {
        val projection = MapProjection(listOf(Location(4.5, -76.0), Location(4.5, -75.0)))

        val point = projection.project(Location(4.5, -75.0))

        assertEquals(1f - MAP_MARGIN, point.x, delta)
        assertEquals(0.5f, point.y, delta)
    }

    @Test
    fun emptyListProjectsWithoutFailing() {
        val point = MapProjection(emptyList()).project(Location(1.0, 1.0))

        assertEquals(0.5f, point.x, delta)
        assertEquals(0.5f, point.y, delta)
    }
}
