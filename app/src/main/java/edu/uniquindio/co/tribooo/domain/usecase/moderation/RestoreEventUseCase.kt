package edu.uniquindio.co.tribooo.domain.usecase.moderation

import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.ModerationAction
import edu.uniquindio.co.tribooo.domain.model.NotificationType
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject

class RestoreEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val recorder: ModerationRecorder
) {
    /** RECHAZADO → ACTIVO verificado; avisa al organizador. Solo para administradores. */
    suspend operator fun invoke(eventId: String): RequestResult {
        val (target, failure) = resolveTarget(getCurrentUser, eventRepository, eventId)
        if (target == null) return failure!!
        if (target.event.status != EventStatus.RECHAZADO) {
            return RequestResult.Failure("Solo se pueden restaurar publicaciones rechazadas.")
        }

        val updated = target.event.copy(status = EventStatus.ACTIVO, verified = true)
        eventRepository.update(updated)
        recorder.record(updated, target.admin, ModerationAction.RESTAURAR, notify = NotificationType.APPROVAL)
        return RequestResult.Success("Publicación restaurada.")
    }
}
