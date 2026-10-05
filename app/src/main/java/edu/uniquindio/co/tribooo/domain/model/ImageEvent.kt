package edu.uniquindio.co.tribooo.domain.model

data class ImageEvent(
    val id: String,
    val event: Event,
    val url: String,
    val order: Int
)
