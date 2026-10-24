package edu.uniquindio.co.tribooo.domain.usecase.event

import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.usecase.auth.ObserveCurrentUserUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveMyEventsUseCase @Inject constructor(
    private val observeEvents: ObserveEventsUseCase,
    private val observeCurrentUser: ObserveCurrentUserUseCase
) {
    /** Eventos que organiza el usuario actual, en cualquier estado. Vacío si no hay sesión. */
    operator fun invoke(): Flow<List<Event>> = combine(observeEvents(), observeCurrentUser()) { events, user ->
        if (user == null) emptyList() else events.filter { it.organizer.id == user.id }
    }
}
