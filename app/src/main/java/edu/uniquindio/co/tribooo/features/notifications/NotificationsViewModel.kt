package edu.uniquindio.co.tribooo.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.ResourceProvider
import edu.uniquindio.co.tribooo.core.util.initials
import edu.uniquindio.co.tribooo.core.util.relativeLabel
import edu.uniquindio.co.tribooo.core.util.runRequest
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
    val unreadCount: Int = 0,
    /** Marcar todas como leídas; Loading mientras se procesa. */
    val markAllResult: RequestResult? = null,
    /** Motivo por el que no se pudo marcar como leído. */
    val message: String? = null
) {
    val isMarkingAll: Boolean get() = markAllResult is RequestResult.Loading

    fun summary(resources: ResourceProvider): String =
        if (unreadCount == 0) {
            resources.getString(R.string.notifications_all_read)
        } else {
            resources.getQuantityString(R.plurals.notifications_unread, unreadCount, unreadCount)
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
    private val markAllNotificationsRead: MarkAllNotificationsReadUseCase,
    private val resources: ResourceProvider
) : ViewModel() {

    private val sources = combine(observeComments(), observeFollows(), observeEvents()) { comments, follows, events ->
        NotificationSources(comments, follows, events)
    }

    private val markAllResult = MutableStateFlow<RequestResult?>(null)

    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<NotificationsUiState> = combine(
        observeNotifications(), sources, markAllResult, message
    ) { notifications, sources, markAllResult, message ->
        NotificationsUiState(
            items = notifications.mapNotNull { it.toItem(sources, resources) },
            unreadCount = notifications.count { !it.read },
            markAllResult = markAllResult,
            message = message
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationsUiState())

    /** Se navega de inmediato; marcarla como leída sigue en segundo plano y solo un fallo se avisa. */
    fun onNotificationClick(item: NotificationItem) {
        if (!item.unread) return
        viewModelScope.launch {
            val result = runRequest(resources, onSuccess = { _: Unit -> RequestResult.Success("") }) { markNotificationRead(item.id) }
            if (result is RequestResult.Failure) message.value = result.errorMessage
        }
    }

    fun onMarkAllRead() {
        if (markAllResult.value is RequestResult.Loading) return
        viewModelScope.launch {
            markAllResult.value = RequestResult.Loading
            val result = runRequest(resources, onSuccess = { _: Unit -> RequestResult.Success("") }) { markAllNotificationsRead() }
            markAllResult.value = result
            if (result is RequestResult.Failure) message.value = result.errorMessage
        }
    }

    fun onMessageShown() = message.update { null }
}

/** Arma la tarjeta; devuelve null si el dato al que apunta ya no existe. */
private fun Notification.toItem(sources: NotificationSources, resources: ResourceProvider): NotificationItem? {
    val ago = date.relativeLabel(resources)
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
