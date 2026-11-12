package edu.uniquindio.co.tribooo.core.component

import edu.uniquindio.co.tribooo.data.repository.memory.MockData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DropdownMenuTest {

    private val cities = MockData.cities

    @Test
    fun cityListIsAlphabeticalWithoutDuplicatesAndHasArmenia() {
        assertEquals(cities.distinct(), cities)
        assertEquals(cities.sortedWith(java.text.Collator.getInstance(java.util.Locale.forLanguageTag("es"))), cities)
        assertTrue("Armenia" in cities)
        assertTrue(cities.size >= 30)
    }

    @Test
    fun filterIgnoresCaseAndAccents() {
        assertEquals(listOf("Bogotá"), filterOptions(cities, "bogota"))
        assertEquals(listOf("Calarcá", "Cali"), filterOptions(cities, "CAL"))
        assertEquals(listOf("Medellín"), filterOptions(cities, "  medellin "))
    }

    @Test
    fun blankQueryKeepsAllAndUnknownGivesNone() {
        assertEquals(cities, filterOptions(cities, ""))
        assertEquals(emptyList<String>(), filterOptions(cities, "zzz"))
    }
}
