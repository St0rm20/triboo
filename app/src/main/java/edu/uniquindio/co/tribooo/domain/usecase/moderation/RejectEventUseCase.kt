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

class RejectEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val recorder: ModerationRecorder,
    private val resources: ResourceProvider
) {
    /**
     * PENDIENTE o ACTIVO → RECHAZADO con el [reason] (recortado, mínimo [MIN_REASON_LENGTH] caracteres)
     * que recibe el organizador. Solo para administradores.
     */
    suspend operator fun invoke(eventId: String, reason: String): RequestResult {
        val (target, failure) = resolveTarget(getCurrentUser, eventRepository, resources, eventId)
        if (target == null) return failure!!
        val text = reason.trim()
        if (text.length < MIN_REASON_LENGTH) return RequestResult.Failure(resources.getString(R.string.usecase_reject_reason_too_short, MIN_REASON_LENGTH))
        if (target.event.status != EventStatus.PENDIENTE && target.event.status != EventStatus.ACTIVO) {
            return RequestResult.Failure(resources.getString(R.string.usecase_reject_not_allowed))
        }

        val updated = target.event.copy(status = EventStatus.RECHAZADO, verified = false)
        eventRepository.update(updated)
        recorder.record(updated, target.admin, ModerationAction.RECHAZAR, reason = text, notify = NotificationType.REJECTION)
        return RequestResult.Success(resources.getString(R.string.usecase_reject_success))
    }

    companion object {
        const val MIN_REASON_LENGTH = 15
    }
}
