package edu.uniquindio.co.tribooo.domain.repository

import edu.uniquindio.co.tribooo.domain.model.Follow
import kotlinx.coroutines.flow.StateFlow

interface FollowRepository {
    val follows: StateFlow<List<Follow>>
    suspend fun save(follow: Follow)
    suspend fun delete(followerId: String, followedId: String)
}
