package edu.uniquindio.co.tribooo.domain.usecase.settings

import edu.uniquindio.co.tribooo.domain.model.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {

    @Test
    fun fromTagRecognizesSpanishAndEnglishVariants() {
        assertEquals(AppLanguage.SPANISH, AppLanguage.fromTag("es"))
        assertEquals(AppLanguage.SPANISH, AppLanguage.fromTag("es-CO"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("en"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("en-US"))
    }

    @Test
    fun fromTagFallsBackToSystemWhenEmptyOrUnknown() {
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTag(""))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTag("fr"))
    }

    @Test
    fun tagsRoundTripThroughFromTag() {
        AppLanguage.entries.forEach { assertEquals(it, AppLanguage.fromTag(it.tag)) }
    }
}
