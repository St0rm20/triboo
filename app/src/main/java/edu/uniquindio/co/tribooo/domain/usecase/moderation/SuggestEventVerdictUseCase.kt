package edu.uniquindio.co.tribooo.domain.usecase.moderation

import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.ResourceProvider
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.EventVerdict
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.EventVerdictRepository
import edu.uniquindio.co.tribooo.domain.repository.ModerationAssistantRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject
import java.time.Duration

class SuggestEventVerdictUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val verdictRepository: EventVerdictRepository,
    private val assistant: ModerationAssistantRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val resources: ResourceProvider
) {
    /**
     * Genera y guarda el veredicto del asistente (APROBAR o RECHAZAR, máximo 50 palabras) para un evento en revisión.
     * Si ya hay uno para el contenido actual del evento no llama al modelo. El veredicto llega a la pantalla
     * por [ObserveModerationCasesUseCase]. Solo para administradores.
     */
    suspend operator fun invoke(eventId: String): RequestResult {
        val (target, failure) = resolveTarget(getCurrentUser, eventRepository, resources, eventId)
        if (target == null) return failure!!
        val event = target.event
        if (event.status != EventStatus.PENDIENTE) {
            return RequestResult.Failure(resources.getString(R.string.usecase_assistant_not_pending))
        }
        if (verdictRepository.verdicts.value.any { it.isCurrentFor(event) }) return RequestResult.Success("")

        // Candidatos a duplicado: publicados que empiezan a menos de un día de este
        val published = eventRepository.events.value
            .filter { it.id != event.id && it.status == EventStatus.ACTIVO }
            .filter { Duration.between(it.startDate, event.startDate).abs() <= DUPLICATE_WINDOW }
            .take(MAX_DUPLICATE_CANDIDATES)

        val suggestion = assistant.suggestEventVerdict(event, published)
        verdictRepository.save(
            EventVerdict(
                eventId = event.id,
                recommendation = suggestion.recommendation,
                explanation = suggestion.explanation,
                eventFingerprint = eventFingerprint(event)
            )
        )
        return RequestResult.Success("")
    }

    private companion object {
        val DUPLICATE_WINDOW: Duration = Duration.ofDays(1)
        const val MAX_DUPLICATE_CANDIDATES = 15
    }
}
