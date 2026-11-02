package edu.uniquindio.co.tribooo.domain.model

/** Claves estables de las causas con las que un vecino puede reportar un evento; el texto sale de los recursos. */
object ReportReasons {
    const val FALSE_INFO = "false_info"
    const val NO_LONGER_EXISTS = "no_longer_exists"
    const val CHARGES_OR_ADS = "charges_or_ads"
    const val WRONG_PLACE_OR_CONTACT = "wrong_place_or_contact"
    const val OFFENSIVE_OR_UNSAFE = "offensive_or_unsafe"
    const val SPAM_OR_DUPLICATE = "spam_or_duplicate"

    val all: List<String> = listOf(
        FALSE_INFO, NO_LONGER_EXISTS, CHARGES_OR_ADS, WRONG_PLACE_OR_CONTACT, OFFENSIVE_OR_UNSAFE, SPAM_OR_DUPLICATE
    )
}
