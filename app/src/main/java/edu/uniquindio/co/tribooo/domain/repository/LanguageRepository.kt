package edu.uniquindio.co.tribooo.domain.repository

import edu.uniquindio.co.tribooo.domain.model.AppLanguage
import kotlinx.coroutines.flow.StateFlow

/** Idioma de la app; cambiarlo se aplica al instante y se conserva entre sesiones. */
interface LanguageRepository {
    val language: StateFlow<AppLanguage>
    fun setLanguage(language: AppLanguage)
}
