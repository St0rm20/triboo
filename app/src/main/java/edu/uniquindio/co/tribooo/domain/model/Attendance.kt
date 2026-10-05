package edu.uniquindio.co.tribooo.domain.model

import java.time.Instant

data class Attendance(
    val user: User,
    val event: Event,
    val confirmationDate: Instant = Instant.now()
)
