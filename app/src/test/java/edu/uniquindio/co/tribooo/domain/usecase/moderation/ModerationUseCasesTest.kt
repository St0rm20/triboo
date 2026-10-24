package edu.uniquindio.co.tribooo.domain.usecase.moderation

import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.ImageEvent
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.ModerationAction
import edu.uniquindio.co.tribooo.domain.model.ModerationReview
import edu.uniquindio.co.tribooo.domain.model.ModerationTab
import edu.uniquindio.co.tribooo.domain.model.Notification
import edu.uniquindio.co.tribooo.domain.model.NotificationType
import edu.uniquindio.co.tribooo.domain.model.Report
import edu.uniquindio.co.tribooo.domain.model.ReportReasons
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.model.UserSession
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.ModerationReviewRepository
import edu.uniquindio.co.tribooo.domain.repository.NotificationRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ModerationUseCasesTest {

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

    private class FakeReviewRepository : ModerationReviewRepository {
        private val _reviews = MutableStateFlow<List<ModerationReview>>(emptyList())
        override val reviews: StateFlow<List<ModerationReview>> = _reviews
        override suspend fun save(review: ModerationReview) = _reviews.update { it + review }
    }

    private class FakeNotificationRepository : NotificationRepository {
        private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
        override val notifications: StateFlow<List<Notification>> = _notifications
        override suspend fun save(notification: Notification) = _notifications.update { it + notification }
        override suspend fun markAsRead(id: String) = Unit
        override suspend fun markAllAsRead(userId: String) = Unit
    }

    private class FakeSessionRepository(private val userId: String, private val role: UserRole) : SessionRepository {
        override val session: Flow<UserSession?> = flowOf(UserSession(userId, role))
        override suspend fun saveSession(userId: String, role: UserRole) = Unit
        override suspend fun clearSession() = Unit
    }

    private val users = UserRepositoryImpl()
    private val admin = runBlocking { users.findById("u-moderacion")!! }
    private val author = runBlocking { users.findById("u-daniela")!! }
    private val neighbor = User("v1", "Vecino Uno", "v1@mail.co", "123456", "Armenia", "Centro")
    private val neighbor2 = User("v2", "Vecino Dos", "v2@mail.co", "123456", "Armenia", "Centro")

    private fun event(id: String, status: EventStatus, minutesAgo: Long = 0) = Event(
        id = id, title = "Evento $id", description = "", startDate = Instant.now(), endDate = Instant.now(),
        place = "", neighborhood = "", location = Location(0.0, 0.0), status = status,
        verified = status == EventStatus.ACTIVO,
        publicationDate = Instant.now().minusSeconds(minutesAgo * 60),
        organizer = author, category = Category.SOCIAL
    )

    private fun report(id: String, event: Event, reporter: User, minutesAgo: Long, resolved: Boolean = false) = Report(
        id = id, event = event, reporter = reporter, reason = ReportReasons.FALSE_INFO,
        date = Instant.now().minusSeconds(minutesAgo * 60), resolved = resolved
    )

    private class Env(
        val events: FakeEventRepository,
        val reports: FakeReportRepository,
        val reviews: FakeReviewRepository,
        val notifications: FakeNotificationRepository,
        val verify: VerifyEventUseCase,
        val reject: RejectEventUseCase,
        val restore: RestoreEventUseCase,
        val resolve: ResolveReportsUseCase
    )

    private fun env(events: List<Event>, reports: List<Report> = emptyList(), role: UserRole = UserRole.ADMIN): Env {
        val eventRepo = FakeEventRepository(events)
        val reportRepo = FakeReportRepository(reports)
        val reviewRepo = FakeReviewRepository()
        val notificationRepo = FakeNotificationRepository()
        val current = GetCurrentUserUseCase(FakeSessionRepository(if (role == UserRole.ADMIN) admin.id else author.id, role), users)
        val recorder = ModerationRecorder(reviewRepo, notificationRepo)
        return Env(
            eventRepo, reportRepo, reviewRepo, notificationRepo,
            VerifyEventUseCase(eventRepo, current, recorder),
            RejectEventUseCase(eventRepo, current, recorder),
            RestoreEventUseCase(eventRepo, current, recorder),
            ResolveReportsUseCase(eventRepo, reportRepo, current, recorder)
        )
    }

    @Test
    fun verifyActivatesEventRecordsReviewAndNotifiesAuthor() = runBlocking {
        val env = env(listOf(event("e", EventStatus.PENDIENTE)))

        assertTrue(env.verify("e") is RequestResult.Success)

        val saved = env.events.findById("e")!!
        assertEquals(EventStatus.ACTIVO, saved.status)
        assertTrue(saved.verified)
        assertEquals(listOf(ModerationAction.VERIFICAR), env.reviews.reviews.value.map { it.action })
        val notification = env.notifications.notifications.value.single()
        assertEquals(NotificationType.APPROVAL, notification.type)
        assertEquals(author.id, notification.destinationUser.id)
        assertEquals("e", notification.reference)
    }

    @Test
    fun verifyFailsIfEventIsNotPending() = runBlocking {
        val env = env(listOf(event("e", EventStatus.ACTIVO)))

        assertTrue(env.verify("e") is RequestResult.Failure)
        assertTrue(env.reviews.reviews.value.isEmpty())
    }

    @Test
    fun rejectSetsStateSavesReasonAndNotifiesRejection() = runBlocking {
        val env = env(listOf(event("p", EventStatus.PENDIENTE), event("a", EventStatus.ACTIVO)))
        val reason = "  Falta el lugar exacto del evento.  "

        assertTrue(env.reject("p", reason) is RequestResult.Success)
        assertTrue(env.reject("a", reason) is RequestResult.Success)

        listOf("p", "a").forEach {
            val saved = env.events.findById(it)!!
            assertEquals(EventStatus.RECHAZADO, saved.status)
            assertFalse(saved.verified)
        }
        assertEquals("Falta el lugar exacto del evento.", env.reviews.reviews.value.first().reason)
        assertEquals(setOf(ModerationAction.RECHAZAR), env.reviews.reviews.value.map { it.action }.toSet())
        assertEquals(listOf(NotificationType.REJECTION, NotificationType.REJECTION), env.notifications.notifications.value.map { it.type })
    }

    @Test
    fun rejectNeedsAtLeast15CharsAfterTrim() = runBlocking {
        val env = env(listOf(event("p", EventStatus.PENDIENTE)))

        // 14 caracteres útiles rodeados de espacios
        val result = env.reject("p", "     12345678901234     ")

        assertEquals(RejectEventUseCase.REASON_TOO_SHORT_MESSAGE, (result as RequestResult.Failure).errorMessage)
        assertEquals(EventStatus.PENDIENTE, env.events.findById("p")!!.status)
        assertTrue(env.reviews.reviews.value.isEmpty())
        assertTrue(env.notifications.notifications.value.isEmpty())
        assertTrue(env.reject("p", "123456789012345") is RequestResult.Success)
    }

    @Test
    fun restoreBringsRejectedEventBack() = runBlocking {
        val env = env(listOf(event("r", EventStatus.RECHAZADO)))

        assertTrue(env.restore("r") is RequestResult.Success)

        val saved = env.events.findById("r")!!
        assertEquals(EventStatus.ACTIVO, saved.status)
        assertTrue(saved.verified)
        assertEquals(ModerationAction.RESTAURAR, env.reviews.reviews.value.single().action)
        assertEquals(NotificationType.APPROVAL, env.notifications.notifications.value.single().type)
        assertTrue(env.restore("r") is RequestResult.Failure)
    }

    @Test
    fun nonAdminCannotModerate() = runBlocking {
        val env = env(
            listOf(event("p", EventStatus.PENDIENTE), event("r", EventStatus.RECHAZADO), event("a", EventStatus.ACTIVO)),
            listOf(report("x", event("a", EventStatus.ACTIVO), neighbor, 5)),
            role = UserRole.USER
        )

        assertTrue(env.verify("p") is RequestResult.Failure)
        assertTrue(env.reject("p", "Un motivo suficientemente largo") is RequestResult.Failure)
        assertTrue(env.restore("r") is RequestResult.Failure)
        assertTrue(env.resolve("a") is RequestResult.Failure)

        assertEquals(EventStatus.PENDIENTE, env.events.findById("p")!!.status)
        assertTrue(env.reviews.reviews.value.isEmpty())
        assertTrue(env.notifications.notifications.value.isEmpty())
        assertFalse(env.reports.reports.value.single().resolved)
    }

    @Test
    fun resolveClosesAllReportsOfTheEventAndRecordsReview() = runBlocking {
        val a = event("a", EventStatus.ACTIVO)
        val b = event("b", EventStatus.ACTIVO)
        val env = env(
            listOf(a, b),
            listOf(report("1", a, neighbor, 5), report("2", a, neighbor2, 50), report("3", b, neighbor, 10))
        )

        assertTrue(env.resolve("a") is RequestResult.Success)

        assertEquals(listOf(true, true, false), env.reports.reports.value.map { it.resolved })
        assertEquals(ModerationAction.RESOLVER, env.reviews.reviews.value.single().action)
        // Sin reportes abiertos ya no hay nada que resolver
        assertTrue(env.resolve("a") is RequestResult.Failure)
    }

    @Test
    fun countsAndTabsFollowTheCases() {
        val pendingOld = event("p-old", EventStatus.PENDIENTE, minutesAgo = 600)
        val pendingNew = event("p-new", EventStatus.PENDIENTE, minutesAgo = 20)
        val rejectedOld = event("r-old", EventStatus.RECHAZADO)
        val rejectedNew = event("r-new", EventStatus.RECHAZADO)
        val reported = event("rep", EventStatus.ACTIVO)
        val closed = event("closed", EventStatus.ACTIVO)
        val events = listOf(pendingNew, pendingOld, rejectedOld, rejectedNew, reported, closed)
        val reports = listOf(
            report("1", reported, neighbor, 300),
            report("2", reported, neighbor2, 15),
            report("3", closed, neighbor, 800, resolved = true)
        )
        fun review(id: String, event: Event, action: ModerationAction, minutesAgo: Long) = ModerationReview(
            id = id, event = event, admin = admin, action = action, date = Instant.now().minusSeconds(minutesAgo * 60)
        )
        val reviews = listOf(
            review("a", rejectedOld, ModerationAction.RECHAZAR, 3000),
            review("b", rejectedNew, ModerationAction.RECHAZAR, 100),
            review("c", closed, ModerationAction.RESOLVER, 700)
        )

        fun ids(tab: ModerationTab) = moderationCases(tab, events, reports, reviews).map { it.event.id }

        // Cola por orden de llegada; rechazadas, de la más reciente a la más antigua
        assertEquals(listOf("p-old", "p-new"), ids(ModerationTab.PENDIENTES))
        assertEquals(listOf("r-new", "r-old"), ids(ModerationTab.RECHAZADAS))
        assertEquals(listOf("rep"), ids(ModerationTab.REPORTES))
        assertEquals(listOf("closed"), ids(ModerationTab.RESUELTAS))
        assertEquals(ModerationCounts(pending = 2, rejected = 2, reports = 1, resolved = 1), moderationCounts(events, reports, reviews))
        assertEquals(2, moderationCases(ModerationTab.REPORTES, events, reports, reviews).single().openReports.size)
    }

    @Test
    fun resolvedEventLeavesReportsAndEntersResolved() = runBlocking {
        val a = event("a", EventStatus.ACTIVO)
        val env = env(listOf(a), listOf(report("1", a, neighbor, 5)))
        fun counts() = moderationCounts(env.events.events.value, env.reports.reports.value, env.reviews.reviews.value)

        assertEquals(ModerationCounts(reports = 1), counts())

        env.resolve("a")

        assertEquals(ModerationCounts(resolved = 1), counts())
        val resolved = moderationCases(
            ModerationTab.RESUELTAS, env.events.events.value, env.reports.reports.value, env.reviews.reviews.value
        )
        assertEquals(listOf("a"), resolved.map { it.event.id })
        assertTrue(resolved.single().openReports.isEmpty())
    }
}
