package edu.uniquindio.co.tribooo.domain.repository

import edu.uniquindio.co.tribooo.domain.model.Interest
import kotlinx.coroutines.flow.StateFlow

interface InterestRepository {
    val interests: StateFlow<List<Interest>>
    suspend fun save(interest: Interest)
    suspend fun delete(userId: String, eventId: String)
}
