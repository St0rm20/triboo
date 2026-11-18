package edu.uniquindio.co.tribooo.domain.usecase.settings

import edu.uniquindio.co.tribooo.domain.model.AppLanguage
import edu.uniquindio.co.tribooo.domain.repository.LanguageRepository
import jakarta.inject.Inject

class SetAppLanguageUseCase @Inject constructor(
    private val languageRepository: LanguageRepository
) {
    /** Se llama desde el hilo principal. */
    operator fun invoke(language: AppLanguage) = languageRepository.setLanguage(language)
}
