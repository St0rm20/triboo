package edu.uniquindio.co.tribooo.domain.repository

import edu.uniquindio.co.tribooo.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val users: StateFlow<List<User>>
    suspend fun save(user: User)
    suspend fun update(user: User)
    suspend fun findById(id: String): User?
    suspend fun findByEmail(email: String): User?
    suspend fun login(email: String, password: String): User?
}
