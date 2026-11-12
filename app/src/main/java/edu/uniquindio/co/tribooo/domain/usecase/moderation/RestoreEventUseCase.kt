package edu.uniquindio.co.tribooo.domain.usecase.moderation

import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.ResourceProvider
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.ModerationAction
import edu.uniquindio.co.tribooo.domain.model.NotificationType
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject

class RestoreEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val recorder: ModerationRecorder,
    private val resources: ResourceProvider
) {
    /** RECHAZADO → ACTIVO verificado; avisa al organizador. Solo para administradores. */
    suspend operator fun invoke(eventId: String): RequestResult {
        val (target, failure) = resolveTarget(getCurrentUser, eventRepository, resources, eventId)
        if (target == null) return failure!!
        if (target.event.status != EventStatus.RECHAZADO) {
            return RequestResult.Failure(resources.getString(R.string.usecase_restore_not_rejected))
        }

        val updated = target.event.copy(status = EventStatus.ACTIVO, verified = true)
        eventRepository.update(updated)
        recorder.record(updated, target.admin, ModerationAction.RESTAURAR, notify = NotificationType.APPROVAL)
        return RequestResult.Success(resources.getString(R.string.usecase_restore_success))
    }
}
