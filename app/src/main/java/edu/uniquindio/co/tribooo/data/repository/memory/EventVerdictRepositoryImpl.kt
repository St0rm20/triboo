package edu.uniquindio.co.tribooo.data.repository.memory

import edu.uniquindio.co.tribooo.domain.model.EventVerdict
import edu.uniquindio.co.tribooo.domain.repository.EventVerdictRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Veredictos del asistente de moderación en memoria; los cambios se pierden al cerrar la app. */
@Singleton
class EventVerdictRepositoryImpl @Inject constructor() : EventVerdictRepository {

    private val _verdicts = MutableStateFlow<List<EventVerdict>>(emptyList())
    override val verdicts: StateFlow<List<EventVerdict>> = _verdicts.asStateFlow()

    // Uno por evento: el nuevo reemplaza al anterior
    override suspend fun save(verdict: EventVerdict) {
        _verdicts.update { list -> list.filterNot { it.eventId == verdict.eventId } + verdict }
    }
}
