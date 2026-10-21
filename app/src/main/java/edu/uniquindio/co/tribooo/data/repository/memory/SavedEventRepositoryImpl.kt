package edu.uniquindio.co.tribooo.data.repository.memory

import edu.uniquindio.co.tribooo.domain.model.Saved
import edu.uniquindio.co.tribooo.domain.repository.SavedEventRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class SavedEventRepositoryImpl @Inject constructor() : SavedEventRepository {

    private val _saved = MutableStateFlow(MockData.saved)
    override val saved: StateFlow<List<Saved>> = _saved.asStateFlow()

    override suspend fun save(saved: Saved) {
        _saved.update { it + saved }
    }

    override suspend fun delete(userId: String, eventId: String) {
        _saved.update { current -> current.filterNot { it.user.id == userId && it.event.id == eventId } }
    }
}
