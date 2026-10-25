package edu.uniquindio.co.tribooo.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    @Test
    fun nameNeedsThreeCharactersAfterTrimming() {
        assertEquals(FormTexts.NAME_REQUIRED, Validators.name(""))
        assertEquals(FormTexts.NAME_REQUIRED, Validators.name("   "))
        assertEquals(FormTexts.NAME_TOO_SHORT, Validators.name("Al"))
        assertEquals(FormTexts.NAME_TOO_SHORT, Validators.name("  Al  "))
        assertNull(Validators.name("Ana"))
        assertNull(Validators.name("Ana Ruiz"))
    }

    @Test
    fun emailMustBeFilledAndWellFormed() {
        assertEquals(FormTexts.EMAIL_REQUIRED, Validators.email(""))
        assertEquals(FormTexts.EMAIL_REQUIRED, Validators.email("  "))
        assertEquals(FormTexts.EMAIL_INVALID, Validators.email("ana"))
        assertEquals(FormTexts.EMAIL_INVALID, Validators.email("ana@"))
        assertEquals(FormTexts.EMAIL_INVALID, Validators.email("ana@mail"))
        assertEquals(FormTexts.EMAIL_INVALID, Validators.email("ana@mail.c"))
        assertNull(Validators.email("ana@mail.co"))
        assertNull(Validators.email("ana.ruiz+tribu@mail.co"))
    }

    @Test
    fun passwordNeedsSixCharacters() {
        assertEquals(FormTexts.PASSWORD_REQUIRED, Validators.password(""))
        assertEquals(FormTexts.PASSWORD_TOO_SHORT, Validators.password("12345"))
        assertNull(Validators.password("123456"))
        assertNull(Validators.password("una clave larga"))
    }

    @Test
    fun newPasswordNeedsEightCharactersAndAnUppercase() {
        assertEquals(FormTexts.PASSWORD_REQUIRED, Validators.newPassword(""))
        assertEquals(FormTexts.NEW_PASSWORD_TOO_SHORT, Validators.newPassword("Abc123"))
        assertEquals(FormTexts.NEW_PASSWORD_TOO_SHORT, Validators.newPassword("Abcdef1"))
        assertEquals(FormTexts.NEW_PASSWORD_NEEDS_UPPERCASE, Validators.newPassword("abcdefg1"))
        assertNull(Validators.newPassword("Abcdefg1"))
        assertNull(Validators.newPassword("ABCDEFGH"))
    }

    @Test
    fun confirmationMustBeFilledAndEqual() {
        assertEquals(FormTexts.CONFIRM_PASSWORD_REQUIRED, Validators.confirmPassword("123456", ""))
        assertEquals(FormTexts.PASSWORDS_DIFFER, Validators.confirmPassword("123456", "123457"))
        assertNull(Validators.confirmPassword("123456", "123456"))
    }

    @Test
    fun requiredTextFieldsRejectBlank() {
        assertEquals(FormTexts.CITY_REQUIRED, Validators.city(" "))
        assertEquals(FormTexts.ADDRESS_REQUIRED, Validators.address(""))
        assertEquals(FormTexts.NEIGHBORHOOD_REQUIRED, Validators.neighborhood(""))
        assertEquals(FormTexts.EVENT_TITLE_REQUIRED, Validators.eventTitle("  "))
        assertEquals(FormTexts.PLACE_NAME_REQUIRED, Validators.placeName(""))
        assertEquals(FormTexts.CONTRIBUTION_CAUSE_REQUIRED, Validators.contributionCause(" "))
        assertEquals(FormTexts.COMMENT_REQUIRED, Validators.comment(""))
        assertEquals(FormTexts.REPLY_REQUIRED, Validators.reply("   "))
        listOf(
            Validators.city("Armenia"), Validators.address("Calle 1"), Validators.neighborhood("Centro"),
            Validators.eventTitle("Bazar"), Validators.placeName("Parque"), Validators.contributionCause("Becas"),
            Validators.comment("Genial"), Validators.reply("Gracias")
        ).forEach { assertNull(it) }
    }

    @Test
    fun selectionsAndFlagsReportTheirOwnMessage() {
        assertEquals(FormTexts.TERMS_REQUIRED, Validators.terms(false))
        assertNull(Validators.terms(true))
        assertEquals(FormTexts.CATEGORY_REQUIRED, Validators.category(false))
        assertNull(Validators.category(true))
        assertEquals(FormTexts.PLACE_REQUIRED, Validators.place(false))
        assertNull(Validators.place(true))
        assertEquals(FormTexts.SCHEDULE_INVALID, Validators.schedule(false))
        assertNull(Validators.schedule(true))
        assertEquals(FormTexts.REPORT_REASON_REQUIRED, Validators.reportReason(null))
        assertNull(Validators.reportReason("Spam"))
    }

    @Test
    fun contributionAmountMustBePositive() {
        assertEquals(FormTexts.CONTRIBUTION_AMOUNT_REQUIRED, Validators.contributionAmount(0))
        assertEquals(FormTexts.CONTRIBUTION_AMOUNT_REQUIRED, Validators.contributionAmount(-1))
        assertNull(Validators.contributionAmount(1))
    }

    @Test
    fun rejectReasonNeedsFifteenCharacters() {
        assertEquals(FormTexts.REJECT_REASON_REQUIRED, Validators.rejectReason(""))
        assertNotNull(Validators.rejectReason("corto"))
        assertNotNull(Validators.rejectReason("  " + "x".repeat(14) + "  "))
        assertNull(Validators.rejectReason("x".repeat(15)))
    }

    @Test
    fun imageNoticeAppearsOnlyAboveTheLimit() {
        assertNull(Validators.imageLimitNotice(0))
        assertNull(Validators.imageLimitNotice(FormLimits.MAX_IMAGES))
        assertEquals("Máximo 5 imágenes; se usaron las primeras 5", Validators.imageLimitNotice(6))
    }

    @Test
    fun everyErrorMessageIsConcrete() {
        assertTrue(FormTexts.SUBMIT_INVALID == "Revisa los campos marcados.")
        assertTrue(FormTexts.REQUIRED_LEGEND == "* Obligatorio")
    }
}
