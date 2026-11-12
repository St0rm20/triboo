package edu.uniquindio.co.tribooo.data.repository.memory

import edu.uniquindio.co.tribooo.domain.model.EventFilters
import edu.uniquindio.co.tribooo.domain.repository.EventFilterRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class EventFilterRepositoryImpl @Inject constructor() : EventFilterRepository {

    private val _filters = MutableStateFlow(EventFilters())
    override val filters: StateFlow<EventFilters> = _filters.asStateFlow()

    override fun update(transform: (EventFilters) -> EventFilters) = _filters.update(transform)

    override fun clear() {
        _filters.value = EventFilters()
    }
}
