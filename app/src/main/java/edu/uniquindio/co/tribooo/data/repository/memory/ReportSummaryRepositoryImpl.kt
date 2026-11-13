package edu.uniquindio.co.tribooo.data.repository.memory

import edu.uniquindio.co.tribooo.domain.model.ReportSummary
import edu.uniquindio.co.tribooo.domain.repository.ReportSummaryRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Resúmenes de reportes del asistente de moderación en memoria; los cambios se pierden al cerrar la app. */
@Singleton
class ReportSummaryRepositoryImpl @Inject constructor() : ReportSummaryRepository {

    private val _summaries = MutableStateFlow<List<ReportSummary>>(emptyList())
    override val summaries: StateFlow<List<ReportSummary>> = _summaries.asStateFlow()

    // Uno por evento: el nuevo reemplaza al anterior
    override suspend fun save(summary: ReportSummary) {
        _summaries.update { list -> list.filterNot { it.eventId == summary.eventId } + summary }
    }
}
