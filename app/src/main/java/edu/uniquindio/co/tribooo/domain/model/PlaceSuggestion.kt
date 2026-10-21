package edu.uniquindio.co.tribooo.domain.model

/** Lugar sugerido para el selector de ubicación al crear un evento. */
data class PlaceSuggestion(
    val name: String,
    val neighborhood: String,
    val detail: String,
    val location: Location
)
