package edu.uniquindio.co.tribooo.domain.repository

import edu.uniquindio.co.tribooo.domain.model.WaitlistEntry
import kotlinx.coroutines.flow.StateFlow

interface WaitlistRepository {
    val entries: StateFlow<List<WaitlistEntry>>
    suspend fun save(entry: WaitlistEntry)
    suspend fun delete(userId: String, eventId: String)
}
