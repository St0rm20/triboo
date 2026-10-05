package edu.uniquindio.co.tribooo.domain.model

import java.time.Instant

data class Reporte(
    val id: String,
    val event: Event,
    val reporter: User,
    val reason: String,
    val detail: String? = null,
    val date: Instant = Instant.now(),
    val resolved: Boolean = false
)
