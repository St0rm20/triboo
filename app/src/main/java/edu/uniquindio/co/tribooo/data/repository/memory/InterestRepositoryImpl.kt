package edu.uniquindio.co.tribooo.data.repository.memory

import edu.uniquindio.co.tribooo.domain.model.Interest
import edu.uniquindio.co.tribooo.domain.repository.InterestRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class InterestRepositoryImpl @Inject constructor() : InterestRepository {

    private val _interests = MutableStateFlow(MockData.interests)
    override val interests: StateFlow<List<Interest>> = _interests.asStateFlow()

    override suspend fun save(interest: Interest) {
        _interests.update { it + interest }
    }

    override suspend fun delete(userId: String, eventId: String) {
        _interests.update { current -> current.filterNot { it.user.id == userId && it.event.id == eventId } }
    }
}
