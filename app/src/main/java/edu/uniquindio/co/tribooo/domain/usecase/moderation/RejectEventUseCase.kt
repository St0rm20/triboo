package edu.uniquindio.co.tribooo.domain.usecase.moderation

import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.ModerationAction
import edu.uniquindio.co.tribooo.domain.model.NotificationType
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject

class RejectEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val recorder: ModerationRecorder
) {
    /**
     * PENDIENTE o ACTIVO → RECHAZADO con el [reason] (recortado, mínimo [MIN_REASON_LENGTH] caracteres)
     * que recibe el organizador. Solo para administradores.
     */
    suspend operator fun invoke(eventId: String, reason: String): RequestResult {
        val (target, failure) = resolveTarget(getCurrentUser, eventRepository, eventId)
        if (target == null) return failure!!
        val text = reason.trim()
        if (text.length < MIN_REASON_LENGTH) return RequestResult.Failure(REASON_TOO_SHORT_MESSAGE)
        if (target.event.status != EventStatus.PENDIENTE && target.event.status != EventStatus.ACTIVO) {
            return RequestResult.Failure("Esta publicación no se puede rechazar.")
        }

        val updated = target.event.copy(status = EventStatus.RECHAZADO, verified = false)
        eventRepository.update(updated)
        recorder.record(updated, target.admin, ModerationAction.RECHAZAR, reason = text, notify = NotificationType.REJECTION)
        return RequestResult.Success("Publicación rechazada.")
    }

    companion object {
        const val MIN_REASON_LENGTH = 15
        const val REASON_TOO_SHORT_MESSAGE =
            "Escribe un motivo de al menos $MIN_REASON_LENGTH caracteres: el autor lo recibirá tal cual."
    }
}
