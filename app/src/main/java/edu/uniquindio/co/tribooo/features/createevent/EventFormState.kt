package edu.uniquindio.co.tribooo.features.createevent

import edu.uniquindio.co.tribooo.core.util.APP_ZONE
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.PlaceSuggestion
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.temporal.ChronoUnit

/** Qué hace el formulario de Crear evento cuando se abre sobre un evento existente. */
@Serializable
enum class EventFormMode { EDIT, RESUBMIT, DUPLICATE }

/**
 * Estado inicial del formulario para este evento. Si el inicio ya pasó, o se duplica, el evento se desplaza
 * a mañana conservando la hora de inicio y la duración; si no, las fechas se conservan.
 */
fun Event.toFormState(
    mode: EventFormMode,
    images: List<String>,
    suggestions: List<PlaceSuggestion>,
    now: Instant
): CreateEventUiState {
    val start = startDate.atZone(APP_ZONE).toLocalDateTime()
    val end = endDate.atZone(APP_ZONE).toLocalDateTime()
    val shiftDays = if (mode == EventFormMode.DUPLICATE || startDate.isBefore(now)) {
        ChronoUnit.DAYS.between(start.toLocalDate(), now.atZone(APP_ZONE).toLocalDate().plusDays(1))
    } else {
        0
    }
    val shiftedStart = start.plusDays(shiftDays)
    val shiftedEnd = end.plusDays(shiftDays)
    val defaults = CreateEventUiState()

    return CreateEventUiState(
        mode = mode,
        title = title,
        category = category,
        description = description,
        multiDay = multidia,
        startDate = shiftedStart.toLocalDate(),
        startTime = shiftedStart.toLocalTime(),
        endDate = if (multidia) shiftedEnd.toLocalDate() else shiftedStart.toLocalDate(),
        endTime = shiftedEnd.toLocalTime(),
        place = suggestions.firstOrNull { it.name == place && it.neighborhood == neighborhood }
            ?: PlaceSuggestion(name = place, neighborhood = neighborhood, detail = neighborhood, location = location),
        cupoEnabled = cupo != null,
        cupo = cupo ?: defaults.cupo,
        contributionEnabled = contribution != null,
        contributionAmount = contribution?.amount?.toLong() ?: 0,
        imageUris = images
    )
}
