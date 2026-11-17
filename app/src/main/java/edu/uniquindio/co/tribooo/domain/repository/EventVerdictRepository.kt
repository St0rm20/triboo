package edu.uniquindio.co.tribooo.domain.repository

import edu.uniquindio.co.tribooo.domain.model.EventVerdict
import kotlinx.coroutines.flow.StateFlow

/** Veredictos del asistente de moderación; se guardan para no volver a pedirlos al modelo. */
interface EventVerdictRepository {
    val verdicts: StateFlow<List<EventVerdict>>

    /** Guarda el veredicto y reemplaza el que tuviera el mismo evento. */
    suspend fun save(verdict: EventVerdict)
}
