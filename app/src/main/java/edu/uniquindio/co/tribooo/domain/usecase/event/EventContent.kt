package edu.uniquindio.co.tribooo.domain.usecase.event

import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus

/** Copia los campos que el organizador edita en el formulario; id, organizador y fecha de publicación no cambian. */
internal fun Event.withContentOf(edited: Event, status: EventStatus) = copy(
    title = edited.title,
    description = edited.description,
    startDate = edited.startDate,
    endDate = edited.endDate,
    multidia = edited.multidia,
    place = edited.place,
    neighborhood = edited.neighborhood,
    location = edited.location,
    cupo = edited.cupo,
    contribution = edited.contribution,
    category = edited.category,
    status = status,
    verified = false
)
