package edu.uniquindio.co.tribooo.domain.usecase.event

import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.ResourceProvider
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject
import java.time.Instant

class FinalizeEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val resources: ResourceProvider
) {
    /**
     * Pasa a FINALIZADO un evento ACTIVO del usuario actual. Si terminaba después de [now], su fin se recorta a [now]
     * (y el inicio también, si quedaría después del fin). Asistencias, comentarios e intereses no se tocan.
     */
    suspend operator fun invoke(eventId: String, now: Instant = Instant.now()): RequestResult {
        val user = getCurrentUser() ?: return RequestResult.Failure(resources.getString(R.string.usecase_finalize_sign_in))
        val stored = eventRepository.findById(eventId) ?: return RequestResult.Failure(resources.getString(R.string.usecase_event_not_found))
        if (stored.organizer.id != user.id) return RequestResult.Failure(resources.getString(R.string.usecase_finalize_not_owner))
        if (stored.status != EventStatus.ACTIVO) {
            return RequestResult.Failure(resources.getString(R.string.usecase_finalize_not_active))
        }

        val endDate = minOf(stored.endDate, now)
        eventRepository.update(
            stored.copy(status = EventStatus.FINALIZADO, startDate = minOf(stored.startDate, endDate), endDate = endDate),
            imageUrls = null
        )
        return RequestResult.Success(resources.getString(R.string.usecase_finalize_success))
    }
}
