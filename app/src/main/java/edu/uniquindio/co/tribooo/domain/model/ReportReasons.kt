package edu.uniquindio.co.tribooo.domain.model

/** Causas con las que un vecino puede reportar un evento (las usará la función "Reportar evento"). */
object ReportReasons {
    const val FALSE_INFO = "Información falsa o engañosa"
    const val NO_LONGER_EXISTS = "El evento ya no existe o cambió"
    const val CHARGES_OR_ADS = "Cobro, reventa o publicidad"
    const val WRONG_PLACE_OR_CONTACT = "Lugar o contacto incorrecto"
    const val OFFENSIVE_OR_UNSAFE = "Contenido ofensivo o inseguro"
    const val SPAM_OR_DUPLICATE = "Es spam o está duplicado"

    val all: List<String> = listOf(
        FALSE_INFO, NO_LONGER_EXISTS, CHARGES_OR_ADS, WRONG_PLACE_OR_CONTACT, OFFENSIVE_OR_UNSAFE, SPAM_OR_DUPLICATE
    )
}
