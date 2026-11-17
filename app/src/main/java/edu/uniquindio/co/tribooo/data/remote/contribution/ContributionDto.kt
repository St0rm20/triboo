package edu.uniquindio.co.tribooo.data.remote.contribution

import androidx.annotation.Keep
import edu.uniquindio.co.tribooo.domain.model.Contribution

// Aporte voluntario de un evento. No es una colección: se guarda como mapa anidado
// en el campo "contribution" del documento del evento (null si el evento es gratuito)
@Keep
data class ContributionDto(
    val amount: Double = 0.0,
    val cause: String? = null
) {
    // Convierte el DTO (datos de Firestore) al modelo de dominio
    fun toDomain(): Contribution = Contribution(amount = amount, cause = cause)

    companion object {
        // Convierte el modelo de dominio al DTO que se guardará en Firestore
        fun fromDomain(contribution: Contribution): ContributionDto = ContributionDto(
            amount = contribution.amount,
            cause = contribution.cause
        )
    }
}
