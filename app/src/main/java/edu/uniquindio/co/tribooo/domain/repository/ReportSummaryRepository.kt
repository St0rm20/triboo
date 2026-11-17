package edu.uniquindio.co.tribooo.domain.repository

import edu.uniquindio.co.tribooo.domain.model.ReportSummary
import kotlinx.coroutines.flow.StateFlow

/** Resúmenes de reportes del asistente de moderación; se guardan para no volver a pedirlos al modelo. */
interface ReportSummaryRepository {
    val summaries: StateFlow<List<ReportSummary>>

    /** Guarda el resumen y reemplaza el que tuviera el mismo evento. */
    suspend fun save(summary: ReportSummary)
}
