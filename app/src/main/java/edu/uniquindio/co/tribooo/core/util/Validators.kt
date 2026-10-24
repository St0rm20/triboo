package edu.uniquindio.co.tribooo.core.util

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
 * Reglas de validación de los formularios. Cada función retorna el mensaje de error de
 * [FormTexts] o null si el valor es válido; no conocen la pantalla ni cuándo mostrar el error.
 */
object Validators {
    const val MIN_NAME_LENGTH = 3
    const val MIN_PASSWORD_LENGTH = 6
    const val MIN_NEW_PASSWORD_LENGTH = 8

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email)

    fun name(value: String): String? = when {
        value.isBlank() -> FormTexts.NAME_REQUIRED
        value.trim().length < MIN_NAME_LENGTH -> FormTexts.NAME_TOO_SHORT
        else -> null
    }

    fun city(value: String): String? = required(value, FormTexts.CITY_REQUIRED)

    fun address(value: String): String? = required(value, FormTexts.ADDRESS_REQUIRED)

    fun neighborhood(value: String): String? = required(value, FormTexts.NEIGHBORHOOD_REQUIRED)

    fun email(value: String): String? = when {
        value.isBlank() -> FormTexts.EMAIL_REQUIRED
        !isValidEmail(value) -> FormTexts.EMAIL_INVALID
        else -> null
    }

    fun password(value: String): String? = when {
        value.isBlank() -> FormTexts.PASSWORD_REQUIRED
        value.length < MIN_PASSWORD_LENGTH -> FormTexts.PASSWORD_TOO_SHORT
        else -> null
    }

    /** Contraseña que se crea al registrarse: más exigente que la de [password], que solo valida el inicio de sesión. */
    fun newPassword(value: String): String? = when {
        value.isBlank() -> FormTexts.PASSWORD_REQUIRED
        value.length < MIN_NEW_PASSWORD_LENGTH -> FormTexts.NEW_PASSWORD_TOO_SHORT
        value.none { it.isUpperCase() } -> FormTexts.NEW_PASSWORD_NEEDS_UPPERCASE
        else -> null
    }

    fun confirmPassword(password: String, confirmation: String): String? = when {
        confirmation.isBlank() -> FormTexts.CONFIRM_PASSWORD_REQUIRED
        confirmation != password -> FormTexts.PASSWORDS_DIFFER
        else -> null
    }

    fun terms(accepted: Boolean): String? = if (accepted) null else FormTexts.TERMS_REQUIRED

    fun eventTitle(value: String): String? = required(value, FormTexts.EVENT_TITLE_REQUIRED)

    fun category(isSelected: Boolean): String? = if (isSelected) null else FormTexts.CATEGORY_REQUIRED

    fun place(isSelected: Boolean): String? = if (isSelected) null else FormTexts.PLACE_REQUIRED

    fun placeName(value: String): String? = required(value, FormTexts.PLACE_NAME_REQUIRED)

    fun schedule(isRangeValid: Boolean): String? = if (isRangeValid) null else FormTexts.SCHEDULE_INVALID

    fun contributionAmount(amount: Long): String? = if (amount > 0) null else FormTexts.CONTRIBUTION_AMOUNT_REQUIRED

    fun contributionCause(value: String): String? = required(value, FormTexts.CONTRIBUTION_CAUSE_REQUIRED)

    fun comment(value: String): String? = required(value, FormTexts.COMMENT_REQUIRED)

    fun reply(value: String): String? = required(value, FormTexts.REPLY_REQUIRED)

    fun reportReason(reason: String?): String? = if (reason == null) FormTexts.REPORT_REASON_REQUIRED else null

    fun rejectReason(value: String): String? = when {
        value.isBlank() -> FormTexts.REJECT_REASON_REQUIRED
        value.trim().length < RejectEventUseCase.MIN_REASON_LENGTH -> RejectEventUseCase.REASON_TOO_SHORT_MESSAGE
        else -> null
    }

    /** Aviso (no error) cuando se eligieron más imágenes de las permitidas; null si caben todas. */
    fun imageLimitNotice(selected: Int): String? =
        if (selected > FormLimits.MAX_IMAGES) FormTexts.imageLimitExceeded(FormLimits.MAX_IMAGES) else null

    private fun required(value: String, message: String): String? = if (value.isBlank()) message else null
}
