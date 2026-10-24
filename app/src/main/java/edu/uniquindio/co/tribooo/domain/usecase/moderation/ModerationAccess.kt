package edu.uniquindio.co.tribooo.domain.usecase.moderation

import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase

internal const val NOT_ADMIN_MESSAGE = "No tienes acceso a esta sección"
internal const val EVENT_NOT_FOUND_MESSAGE = "La publicación ya no está disponible."

/** Administrador y evento (leído del repositorio) sobre los que se puede moderar. */
internal data class ModerationTarget(val admin: User, val event: Event)

/** Resuelve quién modera y qué evento; si no se puede, retorna el fallo listo para devolver. */
internal suspend fun resolveTarget(
    getCurrentUser: GetCurrentUserUseCase,
    eventRepository: EventRepository,
    eventId: String
): Pair<ModerationTarget?, RequestResult.Failure?> {
    val admin = getCurrentUser()?.takeIf { it.role == UserRole.ADMIN }
        ?: return null to RequestResult.Failure(NOT_ADMIN_MESSAGE)
    // Siempre el estado actual del repositorio, nunca una copia vieja
    val event = eventRepository.findById(eventId)
        ?: return null to RequestResult.Failure(EVENT_NOT_FOUND_MESSAGE)
    return ModerationTarget(admin, event) to null
}
