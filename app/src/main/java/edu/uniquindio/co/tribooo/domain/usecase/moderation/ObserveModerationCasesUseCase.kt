package edu.uniquindio.co.tribooo.domain.usecase.moderation

import edu.uniquindio.co.tribooo.domain.model.ModerationCase
import edu.uniquindio.co.tribooo.domain.model.ModerationTab
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.ModerationReviewRepository
import edu.uniquindio.co.tribooo.domain.repository.ReportRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveModerationCasesUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val reportRepository: ReportRepository,
    private val moderationReviewRepository: ModerationReviewRepository
) {
    /** Publicaciones de la pestaña [tab]. */
    operator fun invoke(tab: ModerationTab): Flow<List<ModerationCase>> = combine(
        eventRepository.events, reportRepository.reports, moderationReviewRepository.reviews
    ) { events, reports, reviews -> moderationCases(tab, events, reports, reviews) }
}
