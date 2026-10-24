package edu.uniquindio.co.tribooo.data.repository.memory

import edu.uniquindio.co.tribooo.domain.model.Report
import edu.uniquindio.co.tribooo.domain.repository.ReportRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Reportes en memoria que parten de [MockData]; los cambios se pierden al cerrar la app. */
@Singleton
class ReportRepositoryImpl @Inject constructor() : ReportRepository {

    private val _reports = MutableStateFlow(MockData.reports)
    override val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    override suspend fun save(report: Report) {
        _reports.update { it + report }
    }

    override suspend fun markResolved(eventId: String) {
        _reports.update { current -> current.map { if (it.event.id == eventId) it.copy(resolved = true) else it } }
    }
}
