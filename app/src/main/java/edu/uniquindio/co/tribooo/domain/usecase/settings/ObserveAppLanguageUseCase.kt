package edu.uniquindio.co.tribooo.domain.usecase.settings

import edu.uniquindio.co.tribooo.domain.model.AppLanguage
import edu.uniquindio.co.tribooo.domain.repository.LanguageRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class ObserveAppLanguageUseCase @Inject constructor(
    private val languageRepository: LanguageRepository
) {
    operator fun invoke(): StateFlow<AppLanguage> = languageRepository.language
}
