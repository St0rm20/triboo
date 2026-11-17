package edu.uniquindio.co.tribooo.domain.model

/**
 * Un evento visto por moderación: su estado actual, los reportes abiertos y, según el caso,
 * la revisión que lo rechazó o la que cerró su último reporte.
 * [verdict] y [reportSummary] son las ayudas del asistente, solo si siguen vigentes para el evento y sus reportes.
 */
data class ModerationCase(
    val event: Event,
    val openReports: List<Report> = emptyList(),
    val rejection: ModerationReview? = null,
    val resolution: ModerationReview? = null,
    val verdict: EventVerdict? = null,
    val reportSummary: ReportSummary? = null
)
