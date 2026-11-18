package edu.uniquindio.co.tribooo.domain.usecase.settings

import edu.uniquindio.co.tribooo.domain.model.AppLanguage
import edu.uniquindio.co.tribooo.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Idioma en memoria para pruebas sin AppCompat. */
class FakeLanguageRepository(initial: AppLanguage = AppLanguage.SYSTEM) : LanguageRepository {
    private val _language = MutableStateFlow(initial)
    override val language: StateFlow<AppLanguage> = _language.asStateFlow()

    override fun setLanguage(language: AppLanguage) {
        _language.value = language
    }
}
