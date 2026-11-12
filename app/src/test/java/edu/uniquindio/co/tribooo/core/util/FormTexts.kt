package edu.uniquindio.co.tribooo.core.util

import edu.uniquindio.co.tribooo.R

/** Textos de los formularios tal como se leen del `strings.xml` en español, para comparar en las pruebas. */
object FormTexts {
    private val resources = FakeResourceProvider()
    private fun text(id: Int) = resources.getString(id)

    val REQUIRED_LEGEND get() = text(R.string.form_required_legend)
    val SUBMIT_INVALID get() = text(R.string.form_submit_invalid)
    val NAME_REQUIRED get() = text(R.string.form_name_required)
    val NAME_TOO_SHORT get() = text(R.string.form_name_too_short)
    val CITY_REQUIRED get() = text(R.string.form_city_required)
    val ADDRESS_REQUIRED get() = text(R.string.form_address_required)
    val NEIGHBORHOOD_REQUIRED get() = text(R.string.form_neighborhood_required)
    val EMAIL_REQUIRED get() = text(R.string.form_email_required)
    val EMAIL_INVALID get() = text(R.string.form_email_invalid)
    val PASSWORD_REQUIRED get() = text(R.string.form_password_required)
    val PASSWORD_TOO_SHORT get() = text(R.string.form_password_too_short)
    val NEW_PASSWORD_TOO_SHORT get() = text(R.string.form_new_password_too_short)
    val NEW_PASSWORD_NEEDS_UPPERCASE get() = text(R.string.form_new_password_needs_uppercase)
    val CONFIRM_PASSWORD_REQUIRED get() = text(R.string.form_confirm_password_required)
    val PASSWORDS_DIFFER get() = text(R.string.form_passwords_differ)
    val TERMS_REQUIRED get() = text(R.string.form_terms_required)
    val EVENT_TITLE_REQUIRED get() = text(R.string.form_event_title_required)
    val CATEGORY_REQUIRED get() = text(R.string.form_category_required)
    val PLACE_REQUIRED get() = text(R.string.form_place_required)
    val PLACE_NAME_REQUIRED get() = text(R.string.form_place_name_required)
    val SCHEDULE_INVALID get() = text(R.string.form_schedule_invalid)
    val CONTRIBUTION_AMOUNT_REQUIRED get() = text(R.string.form_contribution_amount_required)
    val CONTRIBUTION_CAUSE_REQUIRED get() = text(R.string.form_contribution_cause_required)
    val COMMENT_REQUIRED get() = text(R.string.form_comment_required)
    val REPLY_REQUIRED get() = text(R.string.form_reply_required)
    val REPORT_REASON_REQUIRED get() = text(R.string.form_report_reason_required)
    val REJECT_REASON_REQUIRED get() = text(R.string.form_reject_reason_required)
    val LOGIN_SUCCESS get() = text(R.string.form_login_success)
    val ACCOUNT_CREATED get() = text(R.string.form_account_created)
    val AUTH_NETWORK_ERROR get() = text(R.string.auth_error_network)

    fun recoveryReminder(email: String) = resources.getString(R.string.form_recovery_reminder, email)
}
