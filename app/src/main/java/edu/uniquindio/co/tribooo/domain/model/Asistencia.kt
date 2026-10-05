package edu.uniquindio.co.tribooo.domain.model

import java.time.Instant

data class Asistencia(
    val user: User,
    val event: Event,
    val confirmationDate: Instant = Instant.now()
)
