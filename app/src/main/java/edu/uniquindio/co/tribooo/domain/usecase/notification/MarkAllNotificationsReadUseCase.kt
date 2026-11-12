package edu.uniquindio.co.tribooo.domain.usecase.notification

import edu.uniquindio.co.tribooo.domain.repository.NotificationRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject

class MarkAllNotificationsReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val getCurrentUser: GetCurrentUserUseCase
) {
    /** Marca como leídas todas las notificaciones del usuario actual. */
    suspend operator fun invoke() {
        val user = getCurrentUser() ?: return
        notificationRepository.markAllAsRead(user.id)
    }
}
