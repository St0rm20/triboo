package edu.uniquindio.co.tribooo.domain.usecase.moderation

import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.ModerationAction
import edu.uniquindio.co.tribooo.domain.model.ModerationReview
import edu.uniquindio.co.tribooo.domain.model.Notification
import edu.uniquindio.co.tribooo.domain.model.NotificationType
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.ModerationReviewRepository
import edu.uniquindio.co.tribooo.domain.repository.NotificationRepository
import jakarta.inject.Inject
import java.util.UUID

/** Deja constancia de una decisión de moderación y, si corresponde, avisa al organizador. */
class ModerationRecorder @Inject constructor(
    private val moderationReviewRepository: ModerationReviewRepository,
    private val notificationRepository: NotificationRepository
) {
    suspend fun record(event: Event, admin: User, action: ModerationAction, reason: String? = null, notify: NotificationType? = null) {
        moderationReviewRepository.save(
            ModerationReview(id = UUID.randomUUID().toString(), event = event, admin = admin, action = action, reason = reason)
        )
        if (notify != null) {
            // La referencia es el id del evento: Notificaciones abre su detalle
            notificationRepository.save(
                Notification(
                    id = UUID.randomUUID().toString(),
                    destinationUser = event.organizer,
                    type = notify,
                    reference = event.id
                )
            )
        }
    }
}
