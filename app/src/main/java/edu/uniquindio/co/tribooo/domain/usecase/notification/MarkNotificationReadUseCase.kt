package edu.uniquindio.co.tribooo.domain.usecase.notification

import edu.uniquindio.co.tribooo.domain.repository.NotificationRepository
import jakarta.inject.Inject

class MarkNotificationReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: String) = notificationRepository.markAsRead(notificationId)
}
