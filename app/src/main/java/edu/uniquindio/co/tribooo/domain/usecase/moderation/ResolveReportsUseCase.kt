package edu.uniquindio.co.tribooo.domain.usecase.moderation

import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.domain.model.ModerationAction
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.ReportRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject

class ResolveReportsUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val reportRepository: ReportRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val recorder: ModerationRecorder
) {
    /** Cierra todos los reportes abiertos del evento y deja la revisión RESOLVER. Solo para administradores. */
    suspend operator fun invoke(eventId: String): RequestResult {
        val (target, failure) = resolveTarget(getCurrentUser, eventRepository, eventId)
        if (target == null) return failure!!
        if (reportRepository.reports.value.none { it.event.id == eventId && !it.resolved }) {
            return RequestResult.Failure("Esta publicación no tiene reportes abiertos.")
        }

        reportRepository.markResolved(eventId)
        recorder.record(target.event, target.admin, ModerationAction.RESOLVER)
        return RequestResult.Success("Caso resuelto.")
    }
}
