package edu.uniquindio.co.tribooo.core.util

import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.domain.model.Event

/** Porcentaje de ocupación a partir del cual el cupo se resalta como "últimos cupos". */
const val CUPO_WARNING = 0.85f

/** El evento tiene cupo y ya se alcanzó. */
fun Event.isFull(attendees: Int): Boolean = cupo != null && attendees >= cupo

/** Fracción del cupo ocupada (0 si el evento no tiene cupo). */
fun Event.occupancy(attendees: Int): Float = cupo?.let { (attendees.toFloat() / it).coerceAtMost(1f) } ?: 0f

/** Quedan pocos cupos (o ninguno). */
fun Event.isCupoTight(attendees: Int): Boolean = cupo != null && occupancy(attendees) >= CUPO_WARNING

/** "Cupo completo", "Últimos cupos · 2 libres" o "87 de 120 confirmados" */
fun Event.cupoLabel(rp: ResourceProvider, attendees: Int): String {
    val cupo = cupo ?: return rp.getString(R.string.cupo_unlimited)
    return when {
        isFull(attendees) -> rp.getString(R.string.cupo_full)
        isCupoTight(attendees) -> rp.getQuantityString(R.plurals.cupo_last_spots, cupo - attendees, cupo - attendees)
        else -> rp.getQuantityString(R.plurals.cupo_confirmed_of, attendees, attendees, cupo)
    }
}
