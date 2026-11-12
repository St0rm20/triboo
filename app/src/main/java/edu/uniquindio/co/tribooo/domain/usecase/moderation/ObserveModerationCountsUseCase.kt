package edu.uniquindio.co.tribooo.domain.usecase.moderation

import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.ModerationReviewRepository
import edu.uniquindio.co.tribooo.domain.repository.ReportRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveModerationCountsUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val reportRepository: ReportRepository,
    private val moderationReviewRepository: ModerationReviewRepository
) {
    /** Pendientes, rechazadas, reportes abiertos y casos resueltos, siempre al día. */
    operator fun invoke(): Flow<ModerationCounts> = combine(
        eventRepository.events, reportRepository.reports, moderationReviewRepository.reviews, ::moderationCounts
    )
}
