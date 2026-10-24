package edu.uniquindio.co.tribooo.domain.repository

import edu.uniquindio.co.tribooo.domain.model.Notification
import kotlinx.coroutines.flow.StateFlow

interface NotificationRepository {
    val notifications: StateFlow<List<Notification>>
    suspend fun save(notification: Notification)
    suspend fun markAsRead(id: String)
    suspend fun markAllAsRead(userId: String)
}
