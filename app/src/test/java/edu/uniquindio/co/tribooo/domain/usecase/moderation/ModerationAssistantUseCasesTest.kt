package edu.uniquindio.co.tribooo.domain.usecase.moderation

import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.testResources
import edu.uniquindio.co.tribooo.data.repository.memory.EventVerdictRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.ReportSummaryRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.ImageEvent
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.ModerationTab
import edu.uniquindio.co.tribooo.domain.model.Report
import edu.uniquindio.co.tribooo.domain.model.ReportReasons
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.model.UserSession
import edu.uniquindio.co.tribooo.domain.model.VerdictRecommendation
import edu.uniquindio.co.tribooo.domain.model.VerdictSuggestion
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.ModerationAssistantRepository
import edu.uniquindio.co.tribooo.domain.repository.ReportRepository
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ModerationAssistantUseCasesTest {

    private class FakeEventRepository(initial: List<Event>) : EventRepository {
        private val _events = MutableStateFlow(initial)
        override val events: StateFlow<List<Event>> = _events
        override val images: StateFlow<List<ImageEvent>> = MutableStateFlow(emptyList())
        override suspend fun save(event: Event, imageUrls: List<String>) = _events.update { it + event }
        override suspend fun findById(id: String): Event? = _events.value.firstOrNull { it.id == id }
        override suspend fun update(event: Event, imageUrls: List<String>?) = _events.update { list -> list.map { if (it.id == event.id) event else it } }
        override suspend fun delete(eventId: String) = _events.update { list -> list.filterNot { it.id == eventId } }
    }

    private class FakeReportRepository(initial: List<Report>) : ReportRepository {
        private val _reports = MutableStateFlow(initial)
        override val reports: StateFlow<List<Report>> = _reports
        override suspend fun save(report: Report) = _reports.update { it + report }
        override suspend fun markResolved(eventId: String) = _reports.update { list ->
            list.map { if (it.event.id == eventId) it.copy(resolved = true) else it }
        }
    }

    private class FakeSessionRepository(private val userId: String, private val role: UserRole) : SessionRepository {
        override val session: Flow<UserSession?> = flowOf(UserSession(userId, role))
        override suspend fun saveSession(userId: String, role: UserRole) = Unit
        override suspend fun clearSession() = Unit
    }

    /** Cuenta las llamadas al modelo. */
    private class FakeAssistant : ModerationAssistantRepository {
        var verdictCalls = 0
        var summaryCalls = 0
        override suspend fun suggestEventVerdict(event: Event, published: List<Event>): VerdictSuggestion {
            verdictCalls++
            return VerdictSuggestion(VerdictRecommendation.RECHAZAR, "Vende boletas para una rifa ($verdictCalls).")
        }
        override suspend fun summarizeReports(event: Event, reports: List<Report>): String {
            summaryCalls++
            return "Resumen de ${reports.size} reportes."
        }
    }

    private val users = UserRepositoryImpl()
    private val admin = runBlocking { users.findById("u-moderacion")!! }
    private val author = runBlocking { users.findById("u-daniela")!! }

    private fun event(id: String, status: EventStatus, title: String = "Evento $id") = Event(
        id = id, title = title, description = "Descripción", startDate = Instant.EPOCH, endDate = Instant.EPOCH,
        place = "Salón", neighborhood = "Centro", location = Location(0.0, 0.0), status = status,
        organizer = author, category = Category.SOCIAL
    )

    private fun report(id: String, event: Event) = Report(id = id, event = event, reporter = author, reason = ReportReasons.FALSE_INFO)

    private class Env(
        val events: FakeEventRepository,
        val reports: FakeReportRepository,
        val verdicts: EventVerdictRepositoryImpl,
        val summaries: ReportSummaryRepositoryImpl,
        val assistant: FakeAssistant,
        val suggest: SuggestEventVerdictUseCase,
        val summarize: SummarizeReportsUseCase
    )

    private fun env(events: List<Event>, reports: List<Report> = emptyList(), role: UserRole = UserRole.ADMIN): Env {
        val eventRepo = FakeEventRepository(events)
        val reportRepo = FakeReportRepository(reports)
        val verdictRepo = EventVerdictRepositoryImpl()
        val summaryRepo = ReportSummaryRepositoryImpl()
        val assistant = FakeAssistant()
        val current = GetCurrentUserUseCase(FakeSessionRepository(if (role == UserRole.ADMIN) admin.id else author.id, role), users)
        return Env(
            eventRepo, reportRepo, verdictRepo, summaryRepo, assistant,
            SuggestEventVerdictUseCase(eventRepo, verdictRepo, assistant, current, testResources),
            SummarizeReportsUseCase(eventRepo, reportRepo, summaryRepo, assistant, current, testResources)
        )
    }

    @Test
    fun verdictIsSavedAndReusedUntilTheEventChanges() = runBlocking {
        val env = env(listOf(event("p", EventStatus.PENDIENTE)))

        assertTrue(env.suggest("p") is RequestResult.Success)
        assertTrue(env.suggest("p") is RequestResult.Success)
        assertEquals(1, env.assistant.verdictCalls)
        val saved = env.verdicts.verdicts.value.single()
        assertEquals(VerdictRecommendation.RECHAZAR, saved.recommendation)

        // El organizador edita el evento: el veredicto guardado queda desactualizado y se reemplaza
        env.events.update(event("p", EventStatus.PENDIENTE, title = "Otro título"))
        assertTrue(env.suggest("p") is RequestResult.Success)
        assertEquals(2, env.assistant.verdictCalls)
        assertEquals(1, env.verdicts.verdicts.value.size)
    }

    @Test
    fun verdictOnlyForAdminsAndPendingEvents() = runBlocking {
        assertTrue(env(listOf(event("a", EventStatus.ACTIVO))).suggest("a") is RequestResult.Failure)
        val notAdmin = env(listOf(event("p", EventStatus.PENDIENTE)), role = UserRole.USER)
        assertTrue(notAdmin.suggest("p") is RequestResult.Failure)
        assertEquals(0, notAdmin.assistant.verdictCalls)
    }

    @Test
    fun summaryIsReusedAndRefreshedWhenNewReportsArrive() = runBlocking {
        val active = event("a", EventStatus.ACTIVO)
        val env = env(listOf(active), listOf(report("r1", active)))

        assertTrue(env.summarize("a") is RequestResult.Success)
        assertTrue(env.summarize("a") is RequestResult.Success)
        assertEquals(1, env.assistant.summaryCalls)

        env.reports.save(report("r2", active))
        assertTrue(env.summarize("a") is RequestResult.Success)
        assertEquals(2, env.assistant.summaryCalls)
        val summary = env.summaries.summaries.value.single()
        assertEquals(setOf("r1", "r2"), summary.reportIds)
        assertEquals("Resumen de 2 reportes.", summary.text)
    }

    @Test
    fun summaryFailsWithoutOpenReports() = runBlocking {
        val env = env(listOf(event("a", EventStatus.ACTIVO)))

        assertTrue(env.summarize("a") is RequestResult.Failure)
        assertEquals(0, env.assistant.summaryCalls)
    }

    @Test
    fun casesOnlyCarryCurrentAssistantHelp() = runBlocking {
        val pending = event("p", EventStatus.PENDIENTE)
        val active = event("a", EventStatus.ACTIVO)
        val env = env(listOf(pending, active), listOf(report("r1", active)))
        env.suggest("p")
        env.summarize("a")

        fun cases(tab: ModerationTab) = moderationCases(
            tab, env.events.events.value, env.reports.reports.value, emptyList(),
            env.verdicts.verdicts.value, env.summaries.summaries.value
        )
        assertNotNull(cases(ModerationTab.PENDIENTES).single().verdict)
        assertNotNull(cases(ModerationTab.REPORTES).single().reportSummary)

        // Evento editado y reporte nuevo: ninguna de las dos ayudas sigue vigente
        env.events.update(pending.copy(description = "Ahora con rifa"))
        env.reports.save(report("r2", active))
        assertNull(cases(ModerationTab.PENDIENTES).single().verdict)
        assertNull(cases(ModerationTab.REPORTES).single().reportSummary)
    }
}
