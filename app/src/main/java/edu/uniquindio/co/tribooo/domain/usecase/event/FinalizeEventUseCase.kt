package edu.uniquindio.co.tribooo.domain.usecase.event

import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject
import java.time.Instant

class FinalizeEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val getCurrentUser: GetCurrentUserUseCase
) {
    /**
     * Pasa a FINALIZADO un evento ACTIVO del usuario actual. Si terminaba después de [now], su fin se recorta a [now]
     * (y el inicio también, si quedaría después del fin). Asistencias, comentarios e intereses no se tocan.
     */
    suspend operator fun invoke(eventId: String, now: Instant = Instant.now()): RequestResult {
        val user = getCurrentUser() ?: return RequestResult.Failure("Inicia sesión para finalizar eventos.")
        val stored = eventRepository.findById(eventId) ?: return RequestResult.Failure("El evento ya no existe.")
        if (stored.organizer.id != user.id) return RequestResult.Failure("Solo puedes finalizar tus propios eventos.")
        if (stored.status != EventStatus.ACTIVO) {
            return RequestResult.Failure("Solo se pueden finalizar eventos activos.")
        }

        val endDate = minOf(stored.endDate, now)
        eventRepository.update(
            stored.copy(status = EventStatus.FINALIZADO, startDate = minOf(stored.startDate, endDate), endDate = endDate),
            imageUrls = null
        )
        return RequestResult.Success("Evento finalizado. Lo encuentras en Mis eventos → Finalizados.")
    }
}
