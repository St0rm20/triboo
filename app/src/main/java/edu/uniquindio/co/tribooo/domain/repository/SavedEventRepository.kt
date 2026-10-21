package edu.uniquindio.co.tribooo.domain.repository

import edu.uniquindio.co.tribooo.domain.model.Saved
import kotlinx.coroutines.flow.StateFlow

interface SavedEventRepository {
    val saved: StateFlow<List<Saved>>
    suspend fun save(saved: Saved)
    suspend fun delete(userId: String, eventId: String)
}
