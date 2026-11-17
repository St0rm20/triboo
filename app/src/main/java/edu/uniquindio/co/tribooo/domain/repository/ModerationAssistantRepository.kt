package edu.uniquindio.co.tribooo.domain.repository

import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.Report
import edu.uniquindio.co.tribooo.domain.model.VerdictSuggestion

/**
 * Asistente (modelo de lenguaje) que ayuda a moderación; solo sugiere, la decisión es del moderador.
 * Lanza `DataException` si la petición falla.
 */
interface ModerationAssistantRepository {

    /**
     * Veredicto sugerido (APROBAR o RECHAZAR, máximo 50 palabras) para un evento en revisión.
     * [published] son eventos ya publicados con los que se compara para detectar duplicados.
     */
    suspend fun suggestEventVerdict(event: Event, published: List<Event>): VerdictSuggestion

    /** Resumen (máximo 25 palabras) de las reglas que, según los [reports], podría estar incumpliendo el evento. */
    suspend fun summarizeReports(event: Event, reports: List<Report>): String
}
