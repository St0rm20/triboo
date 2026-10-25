package edu.uniquindio.co.tribooo.features.eventdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.uniquindio.co.tribooo.core.util.FormLimits
import edu.uniquindio.co.tribooo.core.util.FormTexts
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.Validators
import edu.uniquindio.co.tribooo.core.util.distanceKmTo
import edu.uniquindio.co.tribooo.core.util.initials
import edu.uniquindio.co.tribooo.core.util.isFull
import edu.uniquindio.co.tribooo.core.util.WAITLIST_LEFT_MESSAGE
import edu.uniquindio.co.tribooo.core.util.shortName
import edu.uniquindio.co.tribooo.core.util.waitlistJoinedMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.Follow
import edu.uniquindio.co.tribooo.domain.model.Interest
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.Saved
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.model.WaitlistEntry
import edu.uniquindio.co.tribooo.domain.usecase.attendance.AttendanceStep
import edu.uniquindio.co.tribooo.domain.usecase.attendance.FindScheduleConflictUseCase
import edu.uniquindio.co.tribooo.domain.usecase.attendance.ObserveAttendancesUseCase
import edu.uniquindio.co.tribooo.domain.usecase.attendance.ToggleAttendanceUseCase
import edu.uniquindio.co.tribooo.domain.usecase.attendance.nextAttendanceStep
import edu.uniquindio.co.tribooo.domain.usecase.attendance.startAttendanceFlow
import edu.uniquindio.co.tribooo.domain.usecase.auth.ObserveCurrentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.comment.AddCommentUseCase
import edu.uniquindio.co.tribooo.domain.usecase.comment.ObserveCommentsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.FinalizeEventUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.ObserveEventImagesUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.ObserveEventsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.interest.ObserveInterestsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.interest.ToggleInterestUseCase
import edu.uniquindio.co.tribooo.domain.usecase.location.ObserveCurrentLocationUseCase
import edu.uniquindio.co.tribooo.domain.usecase.report.ObserveReportedEventIdsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.report.SubmitReportResult
import edu.uniquindio.co.tribooo.domain.usecase.report.SubmitReportUseCase
import edu.uniquindio.co.tribooo.domain.usecase.saved.ObserveSavedEventsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.saved.ToggleSavedEventUseCase
import edu.uniquindio.co.tribooo.domain.usecase.user.ObserveFollowsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.user.ToggleFollowUseCase
import edu.uniquindio.co.tribooo.domain.usecase.waitlist.JoinWaitlistUseCase
import edu.uniquindio.co.tribooo.domain.usecase.waitlist.LeaveWaitlistUseCase
import edu.uniquindio.co.tribooo.domain.usecase.waitlist.ObserveWaitlistUseCase
import edu.uniquindio.co.tribooo.domain.usecase.waitlist.waitlistPosition
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

private const val ALREADY_REPORTED_MESSAGE = "Ya reportaste este evento · moderación está revisando el caso."

/** Cuántos asistentes se muestran en la fila de avatares. */
private const val ATTENDEES_PREVIEW = 12

/** Persona (asistente o autor de un comentario) lista para mostrarse con su avatar. */
data class PersonItem(
    val id: String,
    val name: String,
    val initials: String,
    val neighborhood: String,
    val isCurrentUser: Boolean,
    val photo: String? = null
)

data class CommentItem(
    val id: String,
    val author: PersonItem,
    val text: String,
    val date: Instant,
    val rating: Int?
)

/** Qué muestra el botón principal de la barra inferior. */
enum class MainAction { ATTENDANCE, FINALIZE, FINALIZED }

/** Toda la información que pinta la pantalla de detalle de un evento. */
data class EventDetail(
    val event: Event,
    /** Fotos de la galería del evento (sin la portada, que va en [Event.imageUrl]). */
    val gallery: List<String>,
    val distanceKm: Double,
    val attendeesCount: Int,
    /** Todos los asistentes confirmados; el usuario actual va primero si confirmó. */
    val attendees: List<PersonItem>,
    val isAttending: Boolean,
    /** Foto del organizador; null si no tiene. */
    val organizerPhoto: String?,
    /** Puesto en la lista de espera, desde 1; null si el usuario no está en ella. */
    val waitlistPosition: Int?,
    val isMine: Boolean,
    /** El usuario ya reportó el evento y moderación aún no resuelve el caso. */
    val hasReported: Boolean,
    val isSaved: Boolean,
    val interestCount: Int,
    val isInterested: Boolean,
    val organizerEventsCount: Int,
    val isFollowingOrganizer: Boolean,
    val comments: List<CommentItem>
) {
    val isFull: Boolean get() = event.isFull(attendeesCount)

    /** Asistentes de la fila de avatares; el resto se ve en "Ver todos". */
    val attendeesPreview: List<PersonItem> get() = attendees.take(ATTENDEES_PREVIEW)

    val isWaitlisted: Boolean get() = waitlistPosition != null

    /** El cupo está lleno y el usuario no asiste: en vez de confirmar, se anota o sale de la lista de espera. */
    val isWaitlistMode: Boolean get() = isFull && !isAttending

    /** El botón principal hace algo: confirmar, cancelar, o anotarse o salir de la lista de espera. */
    val canToggleAttendance: Boolean get() = !isWaitlistMode || isWaitlisted || !isMine

    val mainAction: MainAction
        get() = when {
            event.status == EventStatus.FINALIZADO -> MainAction.FINALIZED
            isMine && event.status == EventStatus.ACTIVO -> MainAction.FINALIZE
            else -> MainAction.ATTENDANCE
        }

    val ratingsCount: Int get() = comments.count { it.rating != null }

    val averageRating: Double? get() = comments.mapNotNull { it.rating }.takeIf { it.isNotEmpty() }?.average()
}

/** Comentario que el usuario está escribiendo; [error] aparece si intenta publicarlo vacío. */
data class CommentDraft(
    val text: String = "",
    val rating: Int = 0,
    val submitAttempted: Boolean = false,
    val isSending: Boolean = false,
    /** El campo debe recibir el foco tras un envío inválido. */
    val focusRequested: Boolean = false
) {
    val error: String? get() = Validators.comment(text).takeIf { submitAttempted }
}

/** Reporte que el usuario está llenando; [error] aparece si intenta enviarlo sin motivo. */
data class ReportDraft(
    val reason: String? = null,
    val detail: String = "",
    val error: String? = null,
    val isSending: Boolean = false,
    /** La lista de motivos debe traerse a la vista tras un envío inválido. */
    val focusRequested: Boolean = false
)

data class EventDetailUiState(
    val detail: EventDetail? = null,
    /** Aún no llega la primera emisión de los repositorios; nunca se confunde con [notFound]. */
    val isLoading: Boolean = true,
    /** Ya llegaron los datos y el evento no existe (se eliminó o dejó de estar disponible). */
    val notFound: Boolean = false,
    val draft: CommentDraft = CommentDraft(),
    val message: String? = null,
    val showAttendees: Boolean = false,
    /** Null si la hoja de reporte está cerrada. */
    val report: ReportDraft? = null,
    /** Diálogo que el usuario debe aceptar antes de confirmar su asistencia. */
    val attendanceStep: AttendanceStep? = null,
    /** Diálogo que pide confirmar que el organizador finaliza su evento. */
    val showFinalizeDialog: Boolean = false,
    val isFinalizing: Boolean = false
)

/** Acciones que el usuario puede hacer desde el detalle. */
sealed interface EventDetailAction {
    data object ToggleAttendance : EventDetailAction
    data object ToggleInterest : EventDetailAction
    data object ToggleSaved : EventDetailAction
    data object ToggleFollow : EventDetailAction
    data class CommentChange(val text: String) : EventDetailAction
    data class RatingChange(val rating: Int) : EventDetailAction
    data object SendComment : EventDetailAction
    data object CommentFocusHandled : EventDetailAction
    data object ShowAttendees : EventDetailAction
    data object HideAttendees : EventDetailAction
    data object ShowReport : EventDetailAction
    data object HideReport : EventDetailAction
    data class ReportReasonChange(val reason: String) : EventDetailAction
    data class ReportDetailChange(val detail: String) : EventDetailAction
    data object SendReport : EventDetailAction
    data object ReportFocusHandled : EventDetailAction
    data object ContinueAttendance : EventDetailAction
    data object CancelAttendance : EventDetailAction
    data object FinalizeEvent : EventDetailAction
    data object ConfirmFinalize : EventDetailAction
    data object DismissFinalize : EventDetailAction
}

/** Interacciones de los usuarios con eventos y personas (se agrupan porque `combine` admite máximo 5 flujos). */
private data class Engagement(
    val interests: List<Interest>,
    val saved: List<Saved>,
    val follows: List<Follow>,
    val comments: List<Comment>,
    val reportedEventIds: Set<String>
)

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeEvents: ObserveEventsUseCase,
    observeEventImages: ObserveEventImagesUseCase,
    observeAttendances: ObserveAttendancesUseCase,
    observeInterests: ObserveInterestsUseCase,
    observeSavedEvents: ObserveSavedEventsUseCase,
    observeFollows: ObserveFollowsUseCase,
    observeComments: ObserveCommentsUseCase,
    observeWaitlist: ObserveWaitlistUseCase,
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeCurrentLocation: ObserveCurrentLocationUseCase,
    private val toggleAttendanceUseCase: ToggleAttendanceUseCase,
    private val toggleInterestUseCase: ToggleInterestUseCase,
    private val toggleSavedEventUseCase: ToggleSavedEventUseCase,
    private val toggleFollowUseCase: ToggleFollowUseCase,
    observeReportedEventIds: ObserveReportedEventIdsUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val findScheduleConflict: FindScheduleConflictUseCase,
    private val submitReportUseCase: SubmitReportUseCase,
    private val joinWaitlist: JoinWaitlistUseCase,
    private val leaveWaitlist: LeaveWaitlistUseCase,
    private val finalizeEventUseCase: FinalizeEventUseCase
) : ViewModel() {

    // El argumento de la ruta EventDetail llega con el nombre de su propiedad
    private val eventId: String = checkNotNull(savedStateHandle["eventId"])

    /** Estado propio de la pantalla (borrador del comentario y mensajes). */
    private val localState = MutableStateFlow(EventDetailUiState())

    private val engagement = combine(
        observeInterests(),
        observeSavedEvents(),
        observeFollows(),
        observeComments(),
        observeReportedEventIds(),
        ::Engagement
    )

    /** Quién está viendo el detalle y desde dónde. */
    private val viewer: Flow<Pair<User?, Location>> = combine(observeCurrentUser(), observeCurrentLocation(), ::Pair)

    private val attendancesAndWaitlist: Flow<Pair<List<Attendance>, List<WaitlistEntry>>> =
        combine(observeAttendances(), observeWaitlist(), ::Pair)

    private val detail: Flow<EventDetail?> = combine(
        observeEvents(),
        attendancesAndWaitlist,
        observeEventImages(),
        engagement,
        viewer
    ) { events, (attendances, waitlist), images, engagement, (currentUser, location) ->
        val event = events.find { it.id == eventId } ?: return@combine null
        val attendees = attendances.filter { it.event.id == eventId }.map { it.user }
        val interested = engagement.interests.filter { it.event.id == eventId }
        val isAttending = attendees.any { it.id == currentUser?.id }

        EventDetail(
            event = event,
            gallery = images.filter { it.event.id == eventId && it.url != event.imageUrl }
                .sortedBy { it.order }
                .map { it.url },
            distanceKm = location.distanceKmTo(event.location),
            attendeesCount = attendees.size,
            // El usuario actual aparece primero si confirmó
            attendees = attendees.sortedByDescending { it.id == currentUser?.id }
                .map { it.toPersonItem(name = it.name, currentUser = currentUser) },
            isAttending = isAttending,
            organizerPhoto = if (event.organizer.id == currentUser?.id) currentUser.photo else event.organizer.photo,
            waitlistPosition = currentUser?.let { waitlistPosition(waitlist, it.id, eventId) },
            isMine = event.organizer.id == currentUser?.id,
            hasReported = eventId in engagement.reportedEventIds,
            isSaved = engagement.saved.any { it.event.id == eventId && it.user.id == currentUser?.id },
            interestCount = interested.size,
            isInterested = interested.any { it.user.id == currentUser?.id },
            organizerEventsCount = events.count {
                it.organizer.id == event.organizer.id && it.status == EventStatus.ACTIVO
            },
            isFollowingOrganizer = engagement.follows.any {
                it.follower.id == currentUser?.id && it.followed.id == event.organizer.id
            },
            comments = engagement.comments.filter { it.event.id == eventId && it.parentComment == null }
                .sortedBy { it.date }
                .map { comment ->
                    CommentItem(
                        id = comment.id,
                        author = comment.author.toPersonItem(
                            name = comment.author.name.shortName(),
                            currentUser = currentUser
                        ),
                        text = comment.text,
                        date = comment.date,
                        rating = comment.rating
                    )
                }
        )
    }

    val uiState: StateFlow<EventDetailUiState> = combine(detail, localState) { detail, local ->
        local.copy(detail = detail, isLoading = false, notFound = detail == null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EventDetailUiState())

    fun onAction(action: EventDetailAction) {
        val detail = uiState.value.detail ?: return
        val event = detail.event

        when (action) {
            EventDetailAction.ToggleAttendance -> {
                when {
                    !detail.canToggleAttendance -> Unit
                    detail.isAttending -> confirmAttendance(event, attending = true)
                    detail.isWaitlisted -> viewModelScope.launch {
                        leaveWaitlist(event)
                        showMessage(WAITLIST_LEFT_MESSAGE)
                    }
                    detail.isFull -> viewModelScope.launch {
                        joinWaitlist(event)?.let { showMessage(waitlistJoinedMessage(it)) }
                    }
                    else -> requestAttendance(event)
                }
            }

            EventDetailAction.ToggleInterest -> viewModelScope.launch { toggleInterestUseCase(event) }

            EventDetailAction.ToggleSaved -> {
                viewModelScope.launch { toggleSavedEventUseCase(event) }
                showMessage(if (detail.isSaved) "Quitaste el evento de tus guardados." else "Evento guardado.")
            }

            EventDetailAction.ToggleFollow -> {
                if (detail.isMine) return
                viewModelScope.launch { toggleFollowUseCase(event.organizer) }
                showMessage(
                    if (detail.isFollowingOrganizer) {
                        "Dejaste de seguir a ${event.organizer.name}."
                    } else {
                        "Ahora sigues a ${event.organizer.name} · verás sus eventos en tu feed."
                    }
                )
            }

            is EventDetailAction.CommentChange -> localState.update {
                it.copy(draft = it.draft.copy(text = action.text.take(FormLimits.COMMENT)))
            }

            EventDetailAction.CommentFocusHandled -> localState.update {
                it.copy(draft = it.draft.copy(focusRequested = false))
            }

            // Tocar de nuevo la misma estrella quita la calificación
            is EventDetailAction.RatingChange -> localState.update {
                it.copy(draft = it.draft.copy(rating = if (it.draft.rating == action.rating) 0 else action.rating))
            }

            EventDetailAction.SendComment -> sendComment(event)

            EventDetailAction.ShowAttendees -> localState.update { it.copy(showAttendees = true) }

            EventDetailAction.HideAttendees -> localState.update { it.copy(showAttendees = false) }

            EventDetailAction.ShowReport -> {
                if (detail.isMine) return
                if (detail.hasReported) showMessage(ALREADY_REPORTED_MESSAGE) else localState.update { it.copy(report = ReportDraft()) }
            }

            EventDetailAction.HideReport -> localState.update { it.copy(report = null) }

            is EventDetailAction.ReportReasonChange -> updateReport { it.copy(reason = action.reason, error = null) }

            is EventDetailAction.ReportDetailChange ->
                updateReport { it.copy(detail = action.detail.take(SubmitReportUseCase.MAX_DETAIL_LENGTH)) }

            EventDetailAction.ReportFocusHandled -> updateReport { it.copy(focusRequested = false) }

            EventDetailAction.SendReport -> sendReport(event)

            EventDetailAction.ContinueAttendance -> {
                val step = localState.value.attendanceStep ?: return
                val next = nextAttendanceStep(step)
                localState.update { it.copy(attendanceStep = next) }
                if (next == null) confirmAttendance(step.event, attending = false)
            }

            EventDetailAction.CancelAttendance -> localState.update { it.copy(attendanceStep = null) }

            EventDetailAction.FinalizeEvent -> {
                if (detail.mainAction == MainAction.FINALIZE) localState.update { it.copy(showFinalizeDialog = true) }
            }

            EventDetailAction.DismissFinalize -> localState.update { it.copy(showFinalizeDialog = false) }

            EventDetailAction.ConfirmFinalize -> finalizeEvent(event)
        }
    }

    fun onMessageShown() {
        localState.update { it.copy(message = null) }
    }

    /** Confirmar asistencia: antes pide aceptar el aviso de choque, si lo hay, y el del aporte o la entrada libre. */
    private fun requestAttendance(event: Event) {
        viewModelScope.launch {
            val step = startAttendanceFlow(event, findScheduleConflict(event))
            localState.update { it.copy(attendanceStep = step) }
        }
    }

    private fun finalizeEvent(event: Event) {
        val state = localState.value
        if (!state.showFinalizeDialog || state.isFinalizing) return
        localState.update { it.copy(showFinalizeDialog = false, isFinalizing = true) }
        viewModelScope.launch {
            val message = when (val result = finalizeEventUseCase(event.id)) {
                is RequestResult.Success -> result.message
                is RequestResult.Failure -> result.errorMessage
                RequestResult.Loading -> null
            }
            localState.update { it.copy(isFinalizing = false, message = message ?: it.message) }
        }
    }

    private fun confirmAttendance(event: Event, attending: Boolean) {
        viewModelScope.launch { toggleAttendanceUseCase(event) }
        showMessage(if (attending) "Cancelaste tu asistencia." else "¡Listo! Tu lugar está reservado.")
    }

    private fun updateReport(transform: (ReportDraft) -> ReportDraft) {
        localState.update { state -> state.report?.let { state.copy(report = transform(it)) } ?: state }
    }

    private fun sendReport(event: Event) {
        val draft = localState.value.report ?: return
        if (draft.isSending) return
        val reason = draft.reason
        if (reason == null) {
            updateReport { it.copy(error = Validators.reportReason(null), focusRequested = true) }
            return
        }
        updateReport { it.copy(isSending = true) }
        viewModelScope.launch {
            val message = when (submitReportUseCase(event, reason, draft.detail)) {
                SubmitReportResult.SENT -> "Reporte enviado · moderación revisará “${event.title}”."
                SubmitReportResult.ALREADY_REPORTED -> ALREADY_REPORTED_MESSAGE
                SubmitReportResult.OWN_EVENT -> "No puedes reportar tu propio evento."
                SubmitReportResult.NOT_SIGNED_IN -> "Inicia sesión para reportar eventos."
                SubmitReportResult.INVALID_REASON -> FormTexts.REPORT_REASON_REQUIRED
            }
            localState.update { it.copy(report = null, message = message) }
        }
    }

    private fun sendComment(event: Event) {
        val draft = localState.value.draft
        if (draft.isSending) return
        val text = draft.text.trim()
        if (Validators.comment(text) != null) {
            localState.update {
                it.copy(
                    draft = it.draft.copy(submitAttempted = true, focusRequested = true),
                    message = FormTexts.SUBMIT_INVALID
                )
            }
            return
        }

        val rating = draft.rating.takeIf { it > 0 }
        localState.update { it.copy(draft = it.draft.copy(isSending = true)) }
        viewModelScope.launch {
            val published = addCommentUseCase(event = event, text = text, rating = rating)
            localState.update {
                when {
                    !published -> it.copy(
                        draft = it.draft.copy(isSending = false),
                        message = "No se pudo publicar el comentario."
                    )
                    rating != null -> it.copy(draft = CommentDraft(), message = "Comentario publicado con $rating ★.")
                    else -> it.copy(draft = CommentDraft(), message = "Comentario publicado.")
                }
            }
        }
    }

    private fun showMessage(message: String) {
        localState.update { it.copy(message = message) }
    }

    /** El usuario actual se toma de [currentUser] para que su foto recién cambiada se vea en lo ya publicado. */
    private fun User.toPersonItem(name: String, currentUser: User?): PersonItem {
        val isCurrentUser = id == currentUser?.id
        return PersonItem(
            id = id,
            name = if (isCurrentUser) "Tú" else name,
            initials = this.name.initials(),
            neighborhood = neighborhood,
            isCurrentUser = isCurrentUser,
            photo = if (isCurrentUser) currentUser?.photo else photo
        )
    }
}
