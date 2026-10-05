package edu.uniquindio.co.tribooo.domain.model

import java.time.Instant

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val startDate: Instant,
    val endDate: Instant,
    val multidia: Boolean = false,
    val place: String,
    val neighborhood: String,
    val location: Location,
    val cupo: Int? = null,
    val contribution: Contribution? = null,
    val status: EventStatus = EventStatus.PENDIENTE,
    val verified: Boolean = false,
    val publicationDate: Instant = Instant.now(),
    val organizer: User,
    val category: Category
)
