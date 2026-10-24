package edu.uniquindio.co.tribooo.domain.usecase.saved

import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.usecase.auth.ObserveCurrentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.ObserveEventsUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveMySavedEventsUseCase @Inject constructor(
    private val observeSavedEvents: ObserveSavedEventsUseCase,
    private val observeEvents: ObserveEventsUseCase,
    private val observeCurrentUser: ObserveCurrentUserUseCase
) {
    /**
     * Eventos guardados por el usuario actual, de los que ocurren primero a los que ocurren después.
     * Cada guardado se resuelve contra la lista de eventos para mostrar el evento vigente
     * (el guardado conserva una copia); si el evento ya no existe, se omite.
     */
    operator fun invoke(): Flow<List<Event>> =
        combine(observeSavedEvents(), observeEvents(), observeCurrentUser()) { saved, events, user ->
            if (user == null) return@combine emptyList()
            val eventsById = events.associateBy { it.id }
            saved
                .filter { it.user.id == user.id }
                .mapNotNull { eventsById[it.event.id] }
                .distinctBy { it.id }
                .sortedBy { it.startDate }
        }
}
