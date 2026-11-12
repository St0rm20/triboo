package edu.uniquindio.co.tribooo.data.repository.memory

import edu.uniquindio.co.tribooo.domain.model.WaitlistEntry
import edu.uniquindio.co.tribooo.domain.repository.WaitlistRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class WaitlistRepositoryImpl @Inject constructor() : WaitlistRepository {

    private val _entries = MutableStateFlow(MockData.waitlist)
    override val entries: StateFlow<List<WaitlistEntry>> = _entries.asStateFlow()

    override suspend fun save(entry: WaitlistEntry) {
        _entries.update { it + entry }
    }

    override suspend fun delete(userId: String, eventId: String) {
        _entries.update { current -> current.filterNot { it.user.id == userId && it.event.id == eventId } }
    }
}
