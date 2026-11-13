package edu.uniquindio.co.tribooo.domain.model

import java.time.Instant

/**
 * Resumen del asistente de los reportes abiertos de un evento (uno por evento).
 * [reportIds] son los reportes que resumió: si llegan otros, el resumen queda desactualizado.
 */
data class ReportSummary(
    val eventId: String,
    val text: String,
    val reportIds: Set<String>,
    val date: Instant = Instant.now()
)
