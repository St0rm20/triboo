package edu.uniquindio.co.tribooo.domain.usecase.moderation

import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.ResourceProvider
import edu.uniquindio.co.tribooo.domain.model.ModerationAction
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.ReportRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject

class ResolveReportsUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val reportRepository: ReportRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val recorder: ModerationRecorder,
    private val resources: ResourceProvider
) {
    /** Cierra todos los reportes abiertos del evento y deja la revisión RESOLVER. Solo para administradores. */
    suspend operator fun invoke(eventId: String): RequestResult {
        val (target, failure) = resolveTarget(getCurrentUser, eventRepository, resources, eventId)
        if (target == null) return failure!!
        if (reportRepository.reports.value.none { it.event.id == eventId && !it.resolved }) {
            return RequestResult.Failure(resources.getString(R.string.usecase_resolve_no_open_reports))
        }

        reportRepository.markResolved(eventId)
        recorder.record(target.event, target.admin, ModerationAction.RESOLVER)
        return RequestResult.Success(resources.getString(R.string.usecase_resolve_success))
    }
}
