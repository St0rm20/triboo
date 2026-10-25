package edu.uniquindio.co.tribooo.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PasswordStrengthTest {

    @Test
    fun emptyPasswordScoresZero() = assertEquals(0, passwordScore(""))

    @Test
    fun shortLowercasePasswordScoresZero() = assertEquals(0, passwordScore("abc"))

    @Test
    fun eightCharactersAddOne() = assertEquals(1, passwordScore("abcdefgh"))

    @Test
    fun twelveCharactersAddAnotherOne() = assertEquals(2, passwordScore("abcdefghijkl"))

    @Test
    fun upperAndLowerCaseTogetherAddOne() {
        assertEquals(1, passwordScore("Ab"))
        assertEquals(0, passwordScore("AB"))
        assertEquals(0, passwordScore("ab"))
    }

    @Test
    fun aDigitAddsOne() = assertEquals(1, passwordScore("1"))

    @Test
    fun aSymbolAddsOne() {
        assertEquals(1, passwordScore("!"))
        assertEquals(1, passwordScore("_ "))
    }

    @Test
    fun scoreIsCappedAtFour() {
        assertEquals(4, passwordScore("Abcdefghijk1!"))
        assertEquals(4, passwordScore("Abcdefg1!"))
    }

    @Test
    fun barWidthAndLabelFollowTheScore() {
        assertEquals(listOf(0f, 0.34f, 0.58f, 0.8f, 1f), (0..4).map(::passwordStrengthFraction))
        assertEquals(
            listOf("Seguridad", "Débil", "Aceptable", "Buena", "Excelente"),
            (0..4).map(::passwordStrengthLabel)
        )
    }
}
