package edu.uniquindio.co.tribooo.domain.usecase.notification

import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class ObserveUnreadNotificationsCountUseCase @Inject constructor(
    private val observeNotifications: ObserveNotificationsUseCase
) {
    /** Cantidad de notificaciones sin leer del usuario actual. */
    operator fun invoke(): Flow<Int> =
        observeNotifications().map { list -> list.count { !it.read } }.distinctUntilChanged()
}
