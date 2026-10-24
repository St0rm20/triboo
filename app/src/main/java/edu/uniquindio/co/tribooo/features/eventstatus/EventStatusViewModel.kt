package edu.uniquindio.co.tribooo.features.eventstatus

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.core.util.FormLimits
import edu.uniquindio.co.tribooo.core.util.FormTexts
import edu.uniquindio.co.tribooo.core.util.Validators
import edu.uniquindio.co.tribooo.core.util.averageRating
import edu.uniquindio.co.tribooo.core.util.initials
import edu.uniquindio.co.tribooo.core.util.ratingOrDash
import edu.uniquindio.co.tribooo.core.util.relativeLabel
import edu.uniquindio.co.tribooo.core.util.removeDialogTexts
import edu.uniquindio.co.tribooo.core.util.reviewsOf
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.ModerationAction
import edu.uniquindio.co.tribooo.domain.model.ModerationReview
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.usecase.attendance.ObserveAttendancesUseCase
import edu.uniquindio.co.tribooo.domain.usecase.auth.ObserveCurrentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.comment.DeleteReplyUseCase
import edu.uniquindio.co.tribooo.domain.usecase.comment.ObserveCommentsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.comment.ReplyToReviewUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.DeleteEventUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.ObserveEventsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.moderation.ObserveModerationReviewsUseCase
import edu.uniquindio.co.tribooo.navigation.MainRoutes
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

/** Respuesta del organizador a una reseña. */
data class ReplyItem(val id: String, val text: String)

/** Reseña de un asistente lista para mostrarse. */
data class ReviewItem(
    val comment: Comment,
    val authorId: String,
    val authorName: String,
    val initials: String,
    val ago: String,
    val rating: Int,
    val text: String,
    val reply: ReplyItem?
)

data class EventStatusUiState(
    /** Mientras llegan los primeros datos de los repositorios. */
    val isLoading: Boolean = true,
    /** Null si el evento no existe, no es del usuario actual o está activo. */
    val event: Event? = null,
    val attendees: Int = 0,
    val ratingLabel: String = "—",
    val reviews: List<ReviewItem> = emptyList(),
    /** Última revisión de moderación que rechazó el evento. */
    val rejection: ModerationReview? = null,
    // Estado propio de la pantalla (editor de respuesta, confirmación y mensajes)
    val editingReviewId: String? = null,
    val replyDraft: String = "",
    val replyTouched: Boolean = false,
    val replySubmitAttempted: Boolean = false,
    val isPublishingReply: Boolean = false,
    /** El editor de la respuesta debe recibir el foco tras un envío inválido. */
    val replyFocusRequested: Boolean = false,
    val showRemoveDialog: Boolean = false,
    val message: String? = null,
    /** Mensaje para Mis eventos cuando el evento se eliminó y la pantalla debe cerrarse. */
    val exitMessage: String? = null
) {
    val status: EventStatus? get() = event?.status

    val replyError: String? get() = Validators.reply(replyDraft).takeIf { replyTouched || replySubmitAttempted }
}

/** Datos de los repositorios que alimentan la pantalla (se agrupan porque `combine` admite máximo 5 flujos). */
internal data class EventStatusSources(
    val events: List<Event>,
    val moderation: List<ModerationReview>,
    val attendances: List<Attendance>,
    val comments: List<Comment>
)

/** Arma el estado de [eventId] visto por [currentUser]; solo los eventos propios no activos son visibles. */
internal fun buildEventStatusState(
    eventId: String,
    currentUser: User?,
    sources: EventStatusSources,
    now: Instant = Instant.now()
): EventStatusUiState {
    val event = sources.events.firstOrNull { it.id == eventId }
        ?.takeIf { it.organizer.id == currentUser?.id && it.status != EventStatus.ACTIVO }
        ?: return EventStatusUiState(isLoading = false)

    val reviews = if (event.status == EventStatus.FINALIZADO) {
        reviewsOf(eventId, sources.comments).sortedByDescending { it.date }.map { review ->
            val reply = sources.comments.firstOrNull {
                it.parentComment?.id == review.id && it.author.id == event.organizer.id
            }
            ReviewItem(
                comment = review,
                authorId = review.author.id,
                authorName = review.author.name,
                initials = review.author.name.initials(),
                ago = review.date.relativeLabel(now),
                rating = review.rating ?: 0,
                text = review.text,
                reply = reply?.let { ReplyItem(it.id, it.text) }
            )
        }
    } else {
        emptyList()
    }

    return EventStatusUiState(
        isLoading = false,
        event = event,
        attendees = sources.attendances.count { it.event.id == eventId },
        ratingLabel = ratingOrDash(averageRating(eventId, reviewsOf(eventId, sources.comments))),
        reviews = reviews,
        rejection = sources.moderation
            .filter { it.event.id == eventId && it.action == ModerationAction.RECHAZAR }
            .maxByOrNull { it.date }
    )
}

@HiltViewModel
class EventStatusViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeEvents: ObserveEventsUseCase,
    observeModerationReviews: ObserveModerationReviewsUseCase,
    observeAttendances: ObserveAttendancesUseCase,
    observeComments: ObserveCommentsUseCase,
    observeCurrentUser: ObserveCurrentUserUseCase,
    private val replyToReviewUseCase: ReplyToReviewUseCase,
    private val deleteReplyUseCase: DeleteReplyUseCase,
    private val deleteEventUseCase: DeleteEventUseCase
) : ViewModel() {

    private val eventId = savedStateHandle.toRoute<MainRoutes.EventStatus>().eventId

    /** Estado propio de la pantalla; lo demás viene de los repositorios. */
    private val local = MutableStateFlow(EventStatusUiState())

    private val sources = combine(
        observeEvents(),
        observeModerationReviews(),
        observeAttendances(),
        observeComments(),
        ::EventStatusSources
    )

    val uiState: StateFlow<EventStatusUiState> = combine(
        observeCurrentUser(),
        sources,
        local
    ) { currentUser, sources, local ->
        buildEventStatusState(eventId, currentUser, sources).copy(
            editingReviewId = local.editingReviewId,
            replyDraft = local.replyDraft,
            replyTouched = local.replyTouched,
            replySubmitAttempted = local.replySubmitAttempted,
            isPublishingReply = local.isPublishingReply,
            replyFocusRequested = local.replyFocusRequested,
            showRemoveDialog = local.showRemoveDialog,
            message = local.message,
            exitMessage = local.exitMessage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EventStatusUiState())

    // Respuestas a reseñas

    /** Abre el editor de esa reseña (solo uno a la vez) con la respuesta actual como borrador. */
    fun onReplyClick(review: ReviewItem) {
        local.update {
            it.copy(
                editingReviewId = review.comment.id,
                replyDraft = review.reply?.text.orEmpty(),
                replyTouched = false,
                replySubmitAttempted = false
            )
        }
    }

    fun onReplyDraftChange(value: String) = local.update { it.copy(replyDraft = value.take(FormLimits.REPLY)) }

    fun onReplyFocusLost() = local.update { it.copy(replyTouched = true) }

    fun onReplyFocusHandled() = local.update { it.copy(replyFocusRequested = false) }

    fun onReplyCancel() = local.update {
        it.copy(editingReviewId = null, replyDraft = "", replyTouched = false, replySubmitAttempted = false)
    }

    fun onReplyPublish(review: ReviewItem) {
        val state = local.value
        if (state.isPublishingReply) return
        if (Validators.reply(state.replyDraft) != null) {
            local.update {
                it.copy(replySubmitAttempted = true, replyFocusRequested = true, message = FormTexts.SUBMIT_INVALID)
            }
            return
        }
        local.update { it.copy(isPublishingReply = true) }
        viewModelScope.launch {
            val published = replyToReviewUseCase(review.comment, state.replyDraft)
            local.update {
                if (published) {
                    it.copy(
                        editingReviewId = null,
                        replyDraft = "",
                        replyTouched = false,
                        replySubmitAttempted = false,
                        isPublishingReply = false,
                        message = "Respuesta publicada como organizador."
                    )
                } else {
                    it.copy(isPublishingReply = false, message = "No se pudo publicar la respuesta.")
                }
            }
        }
    }

    fun onReplyDelete(review: ReviewItem) {
        val reply = review.reply ?: return
        viewModelScope.launch {
            deleteReplyUseCase(reply.id)
            local.update {
                it.copy(
                    editingReviewId = null,
                    replyDraft = "",
                    replyTouched = false,
                    replySubmitAttempted = false,
                    message = "Respuesta eliminada."
                )
            }
        }
    }

    // Eliminar (rechazado) o retirar (pendiente) el evento

    fun onRemoveClick() = local.update { it.copy(showRemoveDialog = true) }

    fun onDismissRemoveDialog() = local.update { it.copy(showRemoveDialog = false) }

    fun onConfirmRemove() {
        val status = uiState.value.status ?: return
        local.update { it.copy(showRemoveDialog = false) }
        viewModelScope.launch {
            val removed = deleteEventUseCase(eventId)
            local.update {
                if (removed) {
                    it.copy(exitMessage = removeDialogTexts(status).doneMessage)
                } else {
                    it.copy(message = "No se pudo eliminar el evento.")
                }
            }
        }
    }

    fun onDownloadAttendance() = showMessage("Descargando la lista de asistencia (CSV).")

    fun onMessageShown() = local.update { it.copy(message = null) }

    private fun showMessage(message: String) = local.update { it.copy(message = message) }
}
