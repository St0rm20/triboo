package edu.uniquindio.co.tribooo.data.repository.memory

import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class UserRepositoryImpl @Inject constructor() : UserRepository {

    // Usuarios en memoria con los datos de prueba
    private val _users = MutableStateFlow(MockData.users)
    override val users: StateFlow<List<User>> = _users.asStateFlow()

    override suspend fun save(user: User) {
        _users.update { it + user }
    }

    override suspend fun update(user: User) {
        _users.update { current -> current.map { if (it.id == user.id) user else it } }
    }

    override suspend fun findById(id: String): User? {
        return _users.value.firstOrNull { it.id == id }
    }

    override suspend fun findByEmail(email: String): User? {
        return _users.value.firstOrNull { it.email.equals(email, ignoreCase = true) }
    }

    override suspend fun login(email: String, password: String): User? {
        return _users.value.firstOrNull { it.email.equals(email, ignoreCase = true) && it.password == password }
    }
}
