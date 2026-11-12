package edu.uniquindio.co.tribooo.domain.model

/**
 * Un evento visto por moderación: su estado actual, los reportes abiertos y, según el caso,
 * la revisión que lo rechazó o la que cerró su último reporte.
 */
data class ModerationCase(
    val event: Event,
    val openReports: List<Report> = emptyList(),
    val rejection: ModerationReview? = null,
    val resolution: ModerationReview? = null
)
