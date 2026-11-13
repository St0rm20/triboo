package edu.uniquindio.co.tribooo.domain.usecase.moderation

import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.ResourceProvider
import edu.uniquindio.co.tribooo.domain.model.ReportSummary
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.ModerationAssistantRepository
import edu.uniquindio.co.tribooo.domain.repository.ReportRepository
import edu.uniquindio.co.tribooo.domain.repository.ReportSummaryRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject

class SummarizeReportsUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val reportRepository: ReportRepository,
    private val summaryRepository: ReportSummaryRepository,
    private val assistant: ModerationAssistantRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val resources: ResourceProvider
) {
    /**
     * Genera y guarda el resumen del asistente (máximo 25 palabras) de los reportes abiertos del evento.
     * Si el resumen guardado ya cubre exactamente esos reportes no llama al modelo; si llegaron reportes
     * nuevos, lo reemplaza. El resumen llega a la pantalla por [ObserveModerationCasesUseCase]. Solo para administradores.
     */
    suspend operator fun invoke(eventId: String): RequestResult {
        val (target, failure) = resolveTarget(getCurrentUser, eventRepository, resources, eventId)
        if (target == null) return failure!!
        val openReports = reportRepository.reports.value.filter { it.event.id == eventId && !it.resolved }
        if (openReports.isEmpty()) return RequestResult.Failure(resources.getString(R.string.usecase_resolve_no_open_reports))
        if (summaryRepository.summaries.value.any { it.eventId == eventId && it.isCurrentFor(openReports) }) {
            return RequestResult.Success("")
        }

        val text = assistant.summarizeReports(target.event, openReports)
        summaryRepository.save(ReportSummary(eventId = eventId, text = text, reportIds = openReports.map { it.id }.toSet()))
        return RequestResult.Success("")
    }
}
