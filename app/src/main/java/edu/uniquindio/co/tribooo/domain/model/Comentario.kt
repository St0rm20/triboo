package edu.uniquindio.co.tribooo.domain.model

import java.time.Instant

data class Comentario(
    val id: String,
    val event: Event,
    val author: User,
    val text: String,
    val date: Instant = Instant.now(),
    val rating: Int? = null,
    val parentComment: Comentario? = null
)
