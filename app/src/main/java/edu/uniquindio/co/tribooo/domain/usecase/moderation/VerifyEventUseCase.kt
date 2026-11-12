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

class VerifyEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val recorder: ModerationRecorder,
    private val resources: ResourceProvider
) {
    /** PENDIENTE → ACTIVO verificado; avisa al organizador. Solo para administradores. */
    suspend operator fun invoke(eventId: String): RequestResult {
        val (target, failure) = resolveTarget(getCurrentUser, eventRepository, resources, eventId)
        if (target == null) return failure!!
        if (target.event.status != EventStatus.PENDIENTE) {
            return RequestResult.Failure(resources.getString(R.string.usecase_verify_not_pending))
        }

        val updated = target.event.copy(status = EventStatus.ACTIVO, verified = true)
        eventRepository.update(updated)
        recorder.record(updated, target.admin, ModerationAction.VERIFICAR, notify = NotificationType.APPROVAL)
        return RequestResult.Success(resources.getString(R.string.usecase_verify_success))
    }
}
