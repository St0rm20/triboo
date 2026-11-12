package edu.uniquindio.co.tribooo.domain.usecase.report

import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.Report
import edu.uniquindio.co.tribooo.domain.model.ReportReasons
import edu.uniquindio.co.tribooo.domain.repository.ReportRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject
import java.util.UUID

enum class SubmitReportResult { SENT, INVALID_REASON, OWN_EVENT, ALREADY_REPORTED, NOT_SIGNED_IN }

class SubmitReportUseCase @Inject constructor(
    private val reportRepository: ReportRepository,
    private val getCurrentUser: GetCurrentUserUseCase
) {
    /**
     * Reporta [event] con un [reason] de [ReportReasons] y un [detail] opcional. No se puede reportar el evento
     * propio ni uno que el usuario ya reportó y moderación aún no resolvió.
     */
    suspend operator fun invoke(event: Event, reason: String?, detail: String?): SubmitReportResult {
        val user = getCurrentUser() ?: return SubmitReportResult.NOT_SIGNED_IN
        if (reason == null || reason !in ReportReasons.all) return SubmitReportResult.INVALID_REASON
        if (event.organizer.id == user.id) return SubmitReportResult.OWN_EVENT
        val alreadyReported = reportRepository.reports.value.any {
            it.event.id == event.id && it.reporter.id == user.id && !it.resolved
        }
        if (alreadyReported) return SubmitReportResult.ALREADY_REPORTED

        reportRepository.save(
            Report(
                id = UUID.randomUUID().toString(),
                event = event,
                reporter = user,
                reason = reason,
                detail = detail?.trim()?.take(MAX_DETAIL_LENGTH)?.ifEmpty { null }
            )
        )
        return SubmitReportResult.SENT
    }

    companion object {
        const val MAX_DETAIL_LENGTH = 500
    }
}
