package edu.uniquindio.co.tribooo.domain.model

import java.time.Instant

data class Notification(
    val id: String,
    val destinationUser: User,
    val type: NotificationType,
    val reference: String? = null,
    val read: Boolean = false,
    val date: Instant = Instant.now()
)
