package edu.uniquindio.co.tribooo.domain.usecase.report

import edu.uniquindio.co.tribooo.domain.model.ReportReasons
import edu.uniquindio.co.tribooo.domain.usecase.event.TestWorld
import edu.uniquindio.co.tribooo.domain.usecase.event.currentUserUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SubmitReportUseCaseTest {

    private val world = TestWorld()
    private val submit = SubmitReportUseCase(world.reports, world.getCurrentUser)
    private val event = world.publish("e1", 10, 12)

    private fun mine() = world.reports.reports.value.filter { it.reporter.id == world.me.id }

    @Test
    fun invalidOrMissingReasonIsRejected() = runBlocking {
        assertEquals(SubmitReportResult.INVALID_REASON, submit(event, null, null))
        assertEquals(SubmitReportResult.INVALID_REASON, submit(event, "Porque sí", null))
        assertEquals(0, mine().size)
    }

    @Test
    fun ownEventCannotBeReported() = runBlocking {
        val own = world.publish("propio", 10, 12, organizer = world.me)
        assertEquals(SubmitReportResult.OWN_EVENT, submit(own, ReportReasons.SPAM_OR_DUPLICATE, null))
        assertEquals(0, mine().size)
    }

    @Test
    fun unresolvedDuplicateIsBlockedButResolvedOneIsAllowed() = runBlocking {
        assertEquals(SubmitReportResult.SENT, submit(event, ReportReasons.FALSE_INFO, null))
        assertEquals(SubmitReportResult.ALREADY_REPORTED, submit(event, ReportReasons.SPAM_OR_DUPLICATE, null))
        assertEquals(1, mine().size)

        world.reports.markResolved(event.id)
        assertEquals(SubmitReportResult.SENT, submit(event, ReportReasons.SPAM_OR_DUPLICATE, null))
        assertEquals(2, mine().size)
    }

    @Test
    fun blankDetailIsStoredAsNullAndTextIsTrimmedAndLimited() = runBlocking {
        submit(event, ReportReasons.FALSE_INFO, "   ")
        assertNull(mine().single().detail)

        val other = world.publish("e2", 10, 12)
        submit(other, ReportReasons.FALSE_INFO, "  enlace raro  ")
        assertEquals("enlace raro", mine().first { it.event.id == "e2" }.detail)

        val third = world.publish("e3", 10, 12)
        submit(third, ReportReasons.FALSE_INFO, "x".repeat(900))
        assertEquals(SubmitReportUseCase.MAX_DETAIL_LENGTH, mine().first { it.event.id == "e3" }.detail?.length)
    }

    @Test
    fun successSavesReportWithTheCurrentUser() = runBlocking {
        assertEquals(SubmitReportResult.SENT, submit(event, ReportReasons.CHARGES_OR_ADS, "Cobran entrada"))
        val report = mine().single()
        assertEquals(event.id, report.event.id)
        assertEquals(ReportReasons.CHARGES_OR_ADS, report.reason)
        assertEquals("Cobran entrada", report.detail)
        assertEquals(false, report.resolved)
        assertEquals(setOf("e1"), ObserveReportedEventIdsUseCase(world.reports, world.observeCurrentUser)().first())
    }

    @Test
    fun withoutSessionNothingIsSaved() = runBlocking {
        val signedOut = currentUserUseCase(null)
        val result = SubmitReportUseCase(world.reports, signedOut)(event, ReportReasons.FALSE_INFO, null)
        assertEquals(SubmitReportResult.NOT_SIGNED_IN, result)
        assertEquals(0, mine().size)
    }
}
