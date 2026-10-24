package edu.uniquindio.co.tribooo.domain.repository

import edu.uniquindio.co.tribooo.domain.model.Report
import kotlinx.coroutines.flow.StateFlow

interface ReportRepository {
    val reports: StateFlow<List<Report>>
    suspend fun save(report: Report)

    /** Marca como resueltos todos los reportes del evento. */
    suspend fun markResolved(eventId: String)
}
