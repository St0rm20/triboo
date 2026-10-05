package edu.uniquindio.co.tribooo.domain.model

import java.time.Instant

data class ModerationReview(
    val id: String,
    val event: Event,
    val admin: User,
    val action: ModerationAction,
    val reason: String? = null,
    val date: Instant = Instant.now()
)
