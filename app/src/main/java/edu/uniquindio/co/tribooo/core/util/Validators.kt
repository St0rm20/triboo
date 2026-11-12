package edu.uniquindio.co.tribooo.core.util

import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.domain.usecase.moderation.RejectEventUseCase

/** Máximos de caracteres de los campos de texto de los formularios que aún no tenían uno. */
object FormLimits {
    const val EVENT_DESCRIPTION = 500
    const val COMMENT = 500
    const val REPLY = 500
    const val REJECT_REASON = 300
    const val CONTRIBUTION_CAUSE = 120
    const val MAX_IMAGES = 5
}

/**
 * Reglas de validación de los formularios. Cada función retorna el mensaje de error (de los recursos de
 * `form_*`) o null si el valor es válido; no conocen la pantalla ni cuándo mostrar el error.
 */
object Validators {
    const val MIN_NAME_LENGTH = 3
    const val MIN_PASSWORD_LENGTH = 6
    const val MIN_NEW_PASSWORD_LENGTH = 8

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email)

    fun name(rp: ResourceProvider, value: String): String? = when {
        value.isBlank() -> rp.getString(R.string.form_name_required)
        value.trim().length < MIN_NAME_LENGTH -> rp.getString(R.string.form_name_too_short)
        else -> null
    }

    fun city(rp: ResourceProvider, value: String): String? = required(value, rp.getString(R.string.form_city_required))

    fun address(rp: ResourceProvider, value: String): String? = required(value, rp.getString(R.string.form_address_required))

    fun neighborhood(rp: ResourceProvider, value: String): String? = required(value, rp.getString(R.string.form_neighborhood_required))

    fun email(rp: ResourceProvider, value: String): String? = when {
        value.isBlank() -> rp.getString(R.string.form_email_required)
        !isValidEmail(value) -> rp.getString(R.string.form_email_invalid)
        else -> null
    }

    fun password(rp: ResourceProvider, value: String): String? = when {
        value.isBlank() -> rp.getString(R.string.form_password_required)
        value.length < MIN_PASSWORD_LENGTH -> rp.getString(R.string.form_password_too_short)
        else -> null
    }

    /** Contraseña que se crea al registrarse: más exigente que la de [password], que solo valida el inicio de sesión. */
    fun newPassword(rp: ResourceProvider, value: String): String? = when {
        value.isBlank() -> rp.getString(R.string.form_password_required)
        value.length < MIN_NEW_PASSWORD_LENGTH -> rp.getString(R.string.form_new_password_too_short)
        value.none { it.isUpperCase() } -> rp.getString(R.string.form_new_password_needs_uppercase)
        else -> null
    }

    fun confirmPassword(rp: ResourceProvider, password: String, confirmation: String): String? = when {
        confirmation.isBlank() -> rp.getString(R.string.form_confirm_password_required)
        confirmation != password -> rp.getString(R.string.form_passwords_differ)
        else -> null
    }

    fun terms(rp: ResourceProvider, accepted: Boolean): String? = if (accepted) null else rp.getString(R.string.form_terms_required)

    fun eventTitle(rp: ResourceProvider, value: String): String? = required(value, rp.getString(R.string.form_event_title_required))

    fun category(rp: ResourceProvider, isSelected: Boolean): String? = if (isSelected) null else rp.getString(R.string.form_category_required)

    fun place(rp: ResourceProvider, isSelected: Boolean): String? = if (isSelected) null else rp.getString(R.string.form_place_required)

    fun placeName(rp: ResourceProvider, value: String): String? = required(value, rp.getString(R.string.form_place_name_required))

    fun schedule(rp: ResourceProvider, isRangeValid: Boolean): String? = if (isRangeValid) null else rp.getString(R.string.form_schedule_invalid)

    fun contributionAmount(rp: ResourceProvider, amount: Long): String? = if (amount > 0) null else rp.getString(R.string.form_contribution_amount_required)

    fun contributionCause(rp: ResourceProvider, value: String): String? = required(value, rp.getString(R.string.form_contribution_cause_required))

    fun comment(rp: ResourceProvider, value: String): String? = required(value, rp.getString(R.string.form_comment_required))

    fun reply(rp: ResourceProvider, value: String): String? = required(value, rp.getString(R.string.form_reply_required))

    fun reportReason(rp: ResourceProvider, reason: String?): String? = if (reason == null) rp.getString(R.string.form_report_reason_required) else null

    fun rejectReason(rp: ResourceProvider, value: String): String? = when {
        value.isBlank() -> rp.getString(R.string.form_reject_reason_required)
        value.trim().length < RejectEventUseCase.MIN_REASON_LENGTH -> rp.getString(R.string.usecase_reject_reason_too_short, RejectEventUseCase.MIN_REASON_LENGTH)
        else -> null
    }

    /** Aviso (no error) cuando se eligieron más imágenes de las permitidas; null si caben todas. */
    fun imageLimitNotice(rp: ResourceProvider, selected: Int): String? =
        if (selected > FormLimits.MAX_IMAGES) rp.getString(R.string.form_image_limit_exceeded, FormLimits.MAX_IMAGES) else null

    private fun required(value: String, message: String): String? = if (value.isBlank()) message else null
}
