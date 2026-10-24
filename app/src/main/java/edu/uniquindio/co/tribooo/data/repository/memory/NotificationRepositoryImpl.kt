package edu.uniquindio.co.tribooo.data.repository.memory

import edu.uniquindio.co.tribooo.domain.model.Notification
import edu.uniquindio.co.tribooo.domain.repository.NotificationRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class NotificationRepositoryImpl @Inject constructor() : NotificationRepository {

    private val _notifications = MutableStateFlow(MockData.notifications)
    override val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    override suspend fun save(notification: Notification) {
        _notifications.update { it + notification }
    }

    override suspend fun markAsRead(id: String) {
        _notifications.update { current -> current.map { if (it.id == id) it.copy(read = true) else it } }
    }

    override suspend fun markAllAsRead(userId: String) {
        _notifications.update { current ->
            current.map { if (it.destinationUser.id == userId) it.copy(read = true) else it }
        }
    }
}
