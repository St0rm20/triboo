package edu.uniquindio.co.tribooo.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.core.util.initials
import edu.uniquindio.co.tribooo.core.util.relativeLabel
import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.Follow
import edu.uniquindio.co.tribooo.domain.model.Notification
import edu.uniquindio.co.tribooo.domain.model.NotificationType
import edu.uniquindio.co.tribooo.domain.usecase.comment.ObserveCommentsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.ObserveEventsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.notification.MarkAllNotificationsReadUseCase
import edu.uniquindio.co.tribooo.domain.usecase.notification.MarkNotificationReadUseCase
import edu.uniquindio.co.tribooo.domain.usecase.notification.ObserveNotificationsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.user.ObserveFollowsUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Notificación lista para pintarse en una tarjeta. */
data class NotificationItem(
    val id: String,
    val type: NotificationType,
    /** Nombre de quien generó el aviso; null en los de moderación. */
    val who: String?,
    val initials: String,
    /** Título del evento al que se refiere (comentarios y moderación). */
    val eventTitle: String?,
    /** Texto del comentario (solo en los de comentario). */
    val quote: String?,
    val ago: String,
    val unread: Boolean,
    val eventId: String?,
    val commentId: String?,
    /** Quien empezó a seguir (solo en los de seguidor). */
    val userId: String?
)

data class NotificationsUiState(
    val items: List<NotificationItem> = emptyList(),
    val unreadCount: Int = 0
) {
    val summary: String
        get() = when (unreadCount) {
            0 -> "Todo al día"
            1 -> "1 aviso sin leer"
            else -> "$unreadCount avisos sin leer"
        }
}

/** Datos de los repositorios a los que apuntan las notificaciones (se agrupan por el límite de `combine`). */
private data class NotificationSources(
    val comments: List<Comment>,
    val follows: List<Follow>,
    val events: List<Event>
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    observeNotifications: ObserveNotificationsUseCase,
    observeComments: ObserveCommentsUseCase,
    observeFollows: ObserveFollowsUseCase,
    observeEvents: ObserveEventsUseCase,
    private val markNotificationRead: MarkNotificationReadUseCase,
    private val markAllNotificationsRead: MarkAllNotificationsReadUseCase
) : ViewModel() {

    private val sources = combine(observeComments(), observeFollows(), observeEvents()) { comments, follows, events ->
        NotificationSources(comments, follows, events)
    }

    val uiState: StateFlow<NotificationsUiState> = combine(observeNotifications(), sources) { notifications, sources ->
        NotificationsUiState(
            items = notifications.mapNotNull { it.toItem(sources) },
            unreadCount = notifications.count { !it.read }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationsUiState())

    fun onNotificationClick(item: NotificationItem) {
        if (item.unread) viewModelScope.launch { markNotificationRead(item.id) }
    }

    fun onMarkAllRead() {
        viewModelScope.launch { markAllNotificationsRead() }
    }
}

/** Arma la tarjeta; devuelve null si el dato al que apunta ya no existe. */
private fun Notification.toItem(sources: NotificationSources): NotificationItem? {
    val ago = date.relativeLabel()
    return when (type) {
        NotificationType.COMMENT -> {
            val comment = sources.comments.firstOrNull { it.id == reference } ?: return null
            NotificationItem(
                id = id, type = type, who = comment.author.name, initials = comment.author.name.initials(),
                eventTitle = comment.event.title, quote = comment.text, ago = ago, unread = !read,
                eventId = comment.event.id, commentId = comment.id, userId = null
            )
        }

        NotificationType.FOLLOWER -> {
            val follower = sources.follows.firstOrNull { it.follower.id == reference }?.follower ?: return null
            NotificationItem(
                id = id, type = type, who = follower.name, initials = follower.name.initials(),
                eventTitle = null, quote = null, ago = ago, unread = !read,
                eventId = null, commentId = null, userId = follower.id
            )
        }

        NotificationType.APPROVAL, NotificationType.REJECTION -> {
            val event = sources.events.firstOrNull { it.id == reference } ?: return null
            NotificationItem(
                id = id, type = type, who = null, initials = "",
                eventTitle = event.title, quote = null, ago = ago, unread = !read,
                eventId = event.id, commentId = null, userId = null
            )
        }
    }
}
