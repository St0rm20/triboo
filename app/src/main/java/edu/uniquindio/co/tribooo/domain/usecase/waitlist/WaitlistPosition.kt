package edu.uniquindio.co.tribooo.domain.usecase.waitlist

import edu.uniquindio.co.tribooo.domain.model.WaitlistEntry

/** Lista de espera de [eventId] en orden de llegada. */
fun List<WaitlistEntry>.queueOf(eventId: String): List<WaitlistEntry> =
    filter { it.event.id == eventId }.sortedBy { it.joinedAt }

/** Puesto de [userId] en la lista de espera de [eventId], empezando en 1; null si no está en la lista. */
fun waitlistPosition(entries: List<WaitlistEntry>, userId: String, eventId: String): Int? =
    entries.queueOf(eventId).indexOfFirst { it.user.id == userId }.takeIf { it >= 0 }?.plus(1)
