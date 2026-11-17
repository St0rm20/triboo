package edu.uniquindio.co.tribooo.domain.usecase.moderation

import edu.uniquindio.co.tribooo.domain.model.ModerationCase
import edu.uniquindio.co.tribooo.domain.model.ModerationTab
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.EventVerdictRepository
import edu.uniquindio.co.tribooo.domain.repository.ModerationReviewRepository
import edu.uniquindio.co.tribooo.domain.repository.ReportRepository
import edu.uniquindio.co.tribooo.domain.repository.ReportSummaryRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveModerationCasesUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val reportRepository: ReportRepository,
    private val moderationReviewRepository: ModerationReviewRepository,
    private val eventVerdictRepository: EventVerdictRepository,
    private val reportSummaryRepository: ReportSummaryRepository
) {
    /** Publicaciones de la pestaña [tab], con las ayudas vigentes del asistente. */
    operator fun invoke(tab: ModerationTab): Flow<List<ModerationCase>> = combine(
        eventRepository.events,
        reportRepository.reports,
        moderationReviewRepository.reviews,
        eventVerdictRepository.verdicts,
        reportSummaryRepository.summaries
    ) { events, reports, reviews, verdicts, summaries -> moderationCases(tab, events, reports, reviews, verdicts, summaries) }
}
