package edu.uniquindio.co.tribooo.domain.model

import java.time.Instant

/** Persona en la lista de espera de un evento con el cupo lleno. */
data class WaitlistEntry(
    val user: User,
    val event: Event,
    val joinedAt: Instant = Instant.now()
)
