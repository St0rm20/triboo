package edu.uniquindio.co.tribooo.domain.usecase.notification

import edu.uniquindio.co.tribooo.domain.model.Notification
import edu.uniquindio.co.tribooo.domain.repository.NotificationRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.ObserveCurrentUserUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val observeCurrentUser: ObserveCurrentUserUseCase
) {
    /** Notificaciones del usuario actual, de la más reciente a la más antigua. */
    operator fun invoke(): Flow<List<Notification>> =
        combine(notificationRepository.notifications, observeCurrentUser()) { notifications, user ->
            notifications
                .filter { it.destinationUser.id == user?.id }
                .sortedByDescending { it.date }
        }
}
