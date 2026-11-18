package edu.uniquindio.co.tribooo.domain.usecase.settings

import edu.uniquindio.co.tribooo.domain.model.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageUseCasesTest {

    @Test
    fun observeStartsWithTheStoredLanguage() {
        val repository = FakeLanguageRepository(AppLanguage.ENGLISH)

        assertEquals(AppLanguage.ENGLISH, ObserveAppLanguageUseCase(repository)().value)
    }

    @Test
    fun setChangesWhatObserveEmits() {
        val repository = FakeLanguageRepository()
        val observe = ObserveAppLanguageUseCase(repository)
        val set = SetAppLanguageUseCase(repository)

        set(AppLanguage.SPANISH)
        assertEquals(AppLanguage.SPANISH, observe().value)

        set(AppLanguage.SYSTEM)
        assertEquals(AppLanguage.SYSTEM, observe().value)
    }
}
