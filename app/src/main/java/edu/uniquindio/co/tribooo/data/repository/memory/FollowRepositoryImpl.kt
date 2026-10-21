package edu.uniquindio.co.tribooo.data.repository.memory

import edu.uniquindio.co.tribooo.domain.model.Follow
import edu.uniquindio.co.tribooo.domain.repository.FollowRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class FollowRepositoryImpl @Inject constructor() : FollowRepository {

    private val _follows = MutableStateFlow(MockData.follows)
    override val follows: StateFlow<List<Follow>> = _follows.asStateFlow()

    override suspend fun save(follow: Follow) {
        _follows.update { it + follow }
    }

    override suspend fun delete(followerId: String, followedId: String) {
        _follows.update { current -> current.filterNot { it.follower.id == followerId && it.followed.id == followedId } }
    }
}
