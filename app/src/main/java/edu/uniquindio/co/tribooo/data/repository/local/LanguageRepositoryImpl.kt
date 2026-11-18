package edu.uniquindio.co.tribooo.data.repository.local

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import edu.uniquindio.co.tribooo.domain.model.AppLanguage
import edu.uniquindio.co.tribooo.domain.repository.LanguageRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * El idioma lo guarda AppCompat (el sistema en API 33+, el servicio de autoguardado antes), así que
 * no hay otra fuente de verdad: este repositorio solo lee y escribe en [AppCompatDelegate].
 */
@Singleton
class LanguageRepositoryImpl @Inject constructor() : LanguageRepository {

    private val _language = MutableStateFlow(AppLanguage.fromTag(AppCompatDelegate.getApplicationLocales().toLanguageTags()))
    override val language: StateFlow<AppLanguage> = _language.asStateFlow()

    /** Debe llamarse en el hilo principal: AppCompat aplica el idioma y recrea la pantalla. */
    override fun setLanguage(language: AppLanguage) {
        if (_language.value == language) return
        _language.value = language
        AppCompatDelegate.setApplicationLocales(
            if (language == AppLanguage.SYSTEM) LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(language.tag)
        )
    }
}
