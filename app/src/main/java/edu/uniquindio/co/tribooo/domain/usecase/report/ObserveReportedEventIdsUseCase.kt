package edu.uniquindio.co.tribooo.domain.usecase.report

import edu.uniquindio.co.tribooo.domain.repository.ReportRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.ObserveCurrentUserUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

class ObserveReportedEventIdsUseCase @Inject constructor(
    private val reportRepository: ReportRepository,
    private val observeCurrentUser: ObserveCurrentUserUseCase
) {
    /** Ids de los eventos que el usuario actual reportó y moderación aún no resolvió. */
    operator fun invoke(): Flow<Set<String>> = combine(reportRepository.reports, observeCurrentUser()) { reports, user ->
        reports.filter { it.reporter.id == user?.id && !it.resolved }.map { it.event.id }.toSet()
    }.distinctUntilChanged()
}
