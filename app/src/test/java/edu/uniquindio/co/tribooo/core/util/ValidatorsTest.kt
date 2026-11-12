package edu.uniquindio.co.tribooo.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    @Test
    fun nameNeedsThreeCharactersAfterTrimming() {
        assertEquals(FormTexts.NAME_REQUIRED, Validators.name(testResources, ""))
        assertEquals(FormTexts.NAME_REQUIRED, Validators.name(testResources, "   "))
        assertEquals(FormTexts.NAME_TOO_SHORT, Validators.name(testResources, "Al"))
        assertEquals(FormTexts.NAME_TOO_SHORT, Validators.name(testResources, "  Al  "))
        assertNull(Validators.name(testResources, "Ana"))
        assertNull(Validators.name(testResources, "Ana Ruiz"))
    }

    @Test
    fun emailMustBeFilledAndWellFormed() {
        assertEquals(FormTexts.EMAIL_REQUIRED, Validators.email(testResources, ""))
        assertEquals(FormTexts.EMAIL_REQUIRED, Validators.email(testResources, "  "))
        assertEquals(FormTexts.EMAIL_INVALID, Validators.email(testResources, "ana"))
        assertEquals(FormTexts.EMAIL_INVALID, Validators.email(testResources, "ana@"))
        assertEquals(FormTexts.EMAIL_INVALID, Validators.email(testResources, "ana@mail"))
        assertEquals(FormTexts.EMAIL_INVALID, Validators.email(testResources, "ana@mail.c"))
        assertNull(Validators.email(testResources, "ana@mail.co"))
        assertNull(Validators.email(testResources, "ana.ruiz+tribu@mail.co"))
    }

    @Test
    fun passwordNeedsSixCharacters() {
        assertEquals(FormTexts.PASSWORD_REQUIRED, Validators.password(testResources, ""))
        assertEquals(FormTexts.PASSWORD_TOO_SHORT, Validators.password(testResources, "12345"))
        assertNull(Validators.password(testResources, "123456"))
        assertNull(Validators.password(testResources, "una clave larga"))
    }

    @Test
    fun newPasswordNeedsEightCharactersAndAnUppercase() {
        assertEquals(FormTexts.PASSWORD_REQUIRED, Validators.newPassword(testResources, ""))
        assertEquals(FormTexts.NEW_PASSWORD_TOO_SHORT, Validators.newPassword(testResources, "Abc123"))
        assertEquals(FormTexts.NEW_PASSWORD_TOO_SHORT, Validators.newPassword(testResources, "Abcdef1"))
        assertEquals(FormTexts.NEW_PASSWORD_NEEDS_UPPERCASE, Validators.newPassword(testResources, "abcdefg1"))
        assertNull(Validators.newPassword(testResources, "Abcdefg1"))
        assertNull(Validators.newPassword(testResources, "ABCDEFGH"))
    }

    @Test
    fun confirmationMustBeFilledAndEqual() {
        assertEquals(FormTexts.CONFIRM_PASSWORD_REQUIRED, Validators.confirmPassword(testResources, "123456", ""))
        assertEquals(FormTexts.PASSWORDS_DIFFER, Validators.confirmPassword(testResources, "123456", "123457"))
        assertNull(Validators.confirmPassword(testResources, "123456", "123456"))
    }

    @Test
    fun requiredTextFieldsRejectBlank() {
        assertEquals(FormTexts.CITY_REQUIRED, Validators.city(testResources, " "))
        assertEquals(FormTexts.ADDRESS_REQUIRED, Validators.address(testResources, ""))
        assertEquals(FormTexts.NEIGHBORHOOD_REQUIRED, Validators.neighborhood(testResources, ""))
        assertEquals(FormTexts.EVENT_TITLE_REQUIRED, Validators.eventTitle(testResources, "  "))
        assertEquals(FormTexts.PLACE_NAME_REQUIRED, Validators.placeName(testResources, ""))
        assertEquals(FormTexts.CONTRIBUTION_CAUSE_REQUIRED, Validators.contributionCause(testResources, " "))
        assertEquals(FormTexts.COMMENT_REQUIRED, Validators.comment(testResources, ""))
        assertEquals(FormTexts.REPLY_REQUIRED, Validators.reply(testResources, "   "))
        listOf(
            Validators.city(testResources, "Armenia"), Validators.address(testResources, "Calle 1"), Validators.neighborhood(testResources, "Centro"),
            Validators.eventTitle(testResources, "Bazar"), Validators.placeName(testResources, "Parque"), Validators.contributionCause(testResources, "Becas"),
            Validators.comment(testResources, "Genial"), Validators.reply(testResources, "Gracias")
        ).forEach { assertNull(it) }
    }

    @Test
    fun selectionsAndFlagsReportTheirOwnMessage() {
        assertEquals(FormTexts.TERMS_REQUIRED, Validators.terms(testResources, false))
        assertNull(Validators.terms(testResources, true))
        assertEquals(FormTexts.CATEGORY_REQUIRED, Validators.category(testResources, false))
        assertNull(Validators.category(testResources, true))
        assertEquals(FormTexts.PLACE_REQUIRED, Validators.place(testResources, false))
        assertNull(Validators.place(testResources, true))
        assertEquals(FormTexts.SCHEDULE_INVALID, Validators.schedule(testResources, false))
        assertNull(Validators.schedule(testResources, true))
        assertEquals(FormTexts.REPORT_REASON_REQUIRED, Validators.reportReason(testResources, null))
        assertNull(Validators.reportReason(testResources, "Spam"))
    }

    @Test
    fun contributionAmountMustBePositive() {
        assertEquals(FormTexts.CONTRIBUTION_AMOUNT_REQUIRED, Validators.contributionAmount(testResources, 0))
        assertEquals(FormTexts.CONTRIBUTION_AMOUNT_REQUIRED, Validators.contributionAmount(testResources, -1))
        assertNull(Validators.contributionAmount(testResources, 1))
    }

    @Test
    fun rejectReasonNeedsFifteenCharacters() {
        assertEquals(FormTexts.REJECT_REASON_REQUIRED, Validators.rejectReason(testResources, ""))
        assertNotNull(Validators.rejectReason(testResources, "corto"))
        assertNotNull(Validators.rejectReason(testResources, "  " + "x".repeat(14) + "  "))
        assertNull(Validators.rejectReason(testResources, "x".repeat(15)))
    }

    @Test
    fun imageNoticeAppearsOnlyAboveTheLimit() {
        assertNull(Validators.imageLimitNotice(testResources, 0))
        assertNull(Validators.imageLimitNotice(testResources, FormLimits.MAX_IMAGES))
        assertEquals("Máximo 5 imágenes; se usaron las primeras 5", Validators.imageLimitNotice(testResources, 6))
    }

    @Test
    fun everyErrorMessageIsConcrete() {
        assertTrue(FormTexts.SUBMIT_INVALID == "Revisa los campos marcados.")
        assertTrue(FormTexts.REQUIRED_LEGEND == "* Obligatorio")
    }
}
