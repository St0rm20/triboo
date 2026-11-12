package edu.uniquindio.co.tribooo.domain.usecase.location

import edu.uniquindio.co.tribooo.data.repository.memory.MockData
import edu.uniquindio.co.tribooo.domain.model.Location
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ResolvePlaceTest {

    private val base = Location(4.5339, -75.6811)
    private val known = MockData.placeSuggestions

    private fun custom(name: String, neighborhood: String = "", known: List<edu.uniquindio.co.tribooo.domain.model.PlaceSuggestion> = emptyList()) =
        buildCustomPlace(name, neighborhood, "Armenia", base, known)

    @Test
    fun blankNameIsInvalid() {
        assertNull(custom(""))
        assertNull(custom("    "))
    }

    @Test
    fun textIsTrimmedCollapsedAndLimited() {
        val place = custom("  Cancha   del   barrio ", "  La   Esperanza ")!!
        assertEquals("Cancha del barrio", place.name)
        assertEquals("La Esperanza", place.neighborhood)
        assertEquals("La Esperanza", place.detail)

        assertEquals(MAX_PLACE_NAME_LENGTH, custom("x".repeat(200))!!.name.length)
        assertEquals(MAX_NEIGHBORHOOD_LENGTH, custom("Lugar", "y".repeat(200))!!.neighborhood.length)
    }

    @Test
    fun emptyNeighborhoodUsesTheCity() {
        assertEquals("Armenia", custom("Lugar")!!.neighborhood)
        assertEquals("Armenia", custom("Lugar", "   ")!!.detail)
    }

    @Test
    fun sameNameGivesSameCoordinatesRegardlessOfCaseAndSpaces() {
        val a = custom("Cancha Nueva")!!.location
        assertEquals(a, custom("  cancha   NUEVA ", "Otro barrio")!!.location)
        assertTrue(a != custom("Cancha Vieja")!!.location)
    }

    @Test
    fun offsetStaysWithinTwoKilometres() {
        listOf("a", "Salón comunal", "Parque Las Palmas", "x".repeat(80), "Ñandú").forEach { name ->
            val location = custom(name)!!.location
            assertTrue(abs(location.latitude - base.latitude) <= 0.018 + 1e-9)
            assertTrue(abs(location.longitude - base.longitude) <= 0.018 + 1e-9)
        }
    }

    @Test
    fun chosenSuggestionKeepsItsLocation() {
        val parque = known.first()
        val place = custom(parque.name, parque.neighborhood, known)!!
        assertSame(parque, place)
        assertEquals(parque.location, place.location)

        // Con otro barrio ya es un lugar distinto, escrito a mano
        assertTrue(custom(parque.name, "Otro", known)!!.location != parque.location)
    }

    @Test
    fun suggestionFilterIgnoresCaseAccentsAndShowsAllWhenEmpty() {
        assertEquals(known, filterPlaceSuggestions(known, ""))
        assertEquals(known, filterPlaceSuggestions(known, "   "))
        // Coincide por nombre ("Plaza de Bolívar") y por barrio ("Av. Bolívar")
        assertEquals(setOf("Plaza de Bolívar", "Museo del Oro Quimbaya"), filterPlaceSuggestions(known, "BOLIVAR").map { it.name }.toSet())
        assertTrue(filterPlaceSuggestions(known, "bolívar").any { it.name == "Plaza de Bolívar" })
        // También busca por barrio
        assertTrue(filterPlaceSuggestions(known, "la fachada").any { it.name == "Cancha La Fachada" })
        assertEquals(emptyList<String>(), filterPlaceSuggestions(known, "zzzz").map { it.name })
    }

    @Test
    fun seededSuggestionsAreTenToTwelveAndUnique() {
        assertTrue(known.size in 10..12)
        assertEquals(known.size, known.map { it.name }.toSet().size)
    }
}
