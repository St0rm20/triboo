package edu.uniquindio.co.tribooo.features.eventdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.core.util.FormLimits
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.ResourceProvider
import edu.uniquindio.co.tribooo.core.util.Validators
import edu.uniquindio.co.tribooo.core.util.distanceKmTo
import edu.uniquindio.co.tribooo.core.util.initials
import edu.uniquindio.co.tribooo.core.util.isFull
import edu.uniquindio.co.tribooo.core.util.messageOrNull
import edu.uniquindio.co.tribooo.core.util.runRequest
import edu.uniquindio.co.tribooo.core.util.waitlistLeftMessage
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
import edu.uniquindio.co.tribooo.domain.usecase.event.GetEventUseCase
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
    /** Resultado del envío; Loading mientras se publica. */
    val sendResult: RequestResult? = null,
    /** El campo debe recibir el foco tras un envío inválido. */
    val focusRequested: Boolean = false
) {
    val isSending: Boolean get() = sendResult is RequestResult.Loading

    fun error(resources: ResourceProvider): String? = Validators.comment(resources, text).takeIf { submitAttempted }
}

/** Reporte que el usuario está llenando; [error] aparece si intenta enviarlo sin motivo. */
data class ReportDraft(
    val reason: String? = null,
    val detail: String = "",
    val error: String? = null,
    /** Resultado del envío; Loading mientras se envía. */
    val sendResult: RequestResult? = null,
    /** La lista de motivos debe traerse a la vista tras un envío inválido. */
    val focusRequested: Boolean = false
) {
    val isSending: Boolean get() = sendResult is RequestResult.Loading
}

data class EventDetailUiState(
    val detail: EventDetail? = null,
    /** Aún no se sabe si el evento existe; nunca se confunde con [notFound]. */
    val isLoading: Boolean = true,
    /** Lectura del evento al abrir la pantalla; Failure si no existe o no se pudo leer. */
    val loadResult: RequestResult = RequestResult.Loading,
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
    val finalizeResult: RequestResult? = null,
    /** Resultado de la última acción rápida (asistencia, interés, guardar, seguir). */
    val actionResult: RequestResult? = null
) {
    val isFinalizing: Boolean get() = finalizeResult is RequestResult.Loading

    /** Hay una acción rápida en curso: sus botones esperan la respuesta. */
    val isBusy: Boolean get() = actionResult is RequestResult.Loading
}

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
    private val finalizeEventUseCase: FinalizeEventUseCase,
    private val getEventUseCase: GetEventUseCase,
    private val resources: ResourceProvider
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

    // Mientras la lista de eventos llega, la lectura directa del evento decide si existe
    val uiState: StateFlow<EventDetailUiState> = combine(detail, localState) { detail, local ->
        local.copy(
            detail = detail,
            isLoading = detail == null && local.loadResult !is RequestResult.Failure,
            notFound = detail == null && local.loadResult is RequestResult.Failure
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EventDetailUiState())

    init {
        loadEvent()
    }

    private fun loadEvent() {
        viewModelScope.launch {
            localState.update { it.copy(loadResult = RequestResult.Loading) }
            val result = runRequest(resources, onSuccess = { event: Event? ->
                if (event != null) RequestResult.Success("") else RequestResult.Failure(resources.getString(R.string.error_not_found))
            }) { getEventUseCase(eventId) }
            localState.update { it.copy(loadResult = result) }
        }
    }

    fun onAction(action: EventDetailAction) {
        val detail = uiState.value.detail ?: return
        val event = detail.event

        when (action) {
            EventDetailAction.ToggleAttendance -> {
                when {
                    !detail.canToggleAttendance -> Unit
                    detail.isAttending -> confirmAttendance(event, attending = true)
                    detail.isWaitlisted -> request({ leaveWaitlist(event) }) {
                        RequestResult.Success(waitlistLeftMessage(resources))
                    }
                    detail.isFull -> request({ joinWaitlist(event) }) { position ->
                        RequestResult.Success(position?.let { waitlistJoinedMessage(resources, it) }.orEmpty())
                    }
                    else -> requestAttendance(event)
                }
            }

            EventDetailAction.ToggleInterest -> request({ toggleInterestUseCase(event) }) { RequestResult.Success("") }

            EventDetailAction.ToggleSaved -> {
                val message = resources.getString(if (detail.isSaved) R.string.detail_message_unsaved else R.string.detail_message_saved)
                request({ toggleSavedEventUseCase(event) }) { RequestResult.Success(message) }
            }

            EventDetailAction.ToggleFollow -> {
                if (detail.isMine) return
                val message = if (detail.isFollowingOrganizer) {
                    resources.getString(R.string.detail_message_unfollowed, event.organizer.name)
                } else {
                    resources.getString(R.string.detail_message_followed, event.organizer.name)
                }
                request({ toggleFollowUseCase(event.organizer) }) { RequestResult.Success(message) }
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
                if (detail.hasReported) showMessage(resources.getString(R.string.detail_message_already_reported)) else localState.update { it.copy(report = ReportDraft()) }
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
        request({ findScheduleConflict(event) }) { conflict ->
            localState.update { it.copy(attendanceStep = startAttendanceFlow(event, conflict)) }
            RequestResult.Success("")
        }
    }

    private fun finalizeEvent(event: Event) {
        val state = localState.value
        if (!state.showFinalizeDialog || state.isFinalizing) return
        localState.update { it.copy(showFinalizeDialog = false) }
        viewModelScope.launch {
            localState.update { it.copy(finalizeResult = RequestResult.Loading) }
            // El caso de uso ya responde con un RequestResult; solo falta cubrir los fallos de red
            val result = runRequest(resources, onSuccess = { it }) { finalizeEventUseCase(event.id) }
            localState.update {
                when (result) {
                    is RequestResult.Success, is RequestResult.Failure ->
                        it.copy(finalizeResult = result, message = result.messageOrNull() ?: it.message)
                    RequestResult.Loading -> it
                }
            }
        }
    }

    private fun confirmAttendance(event: Event, attending: Boolean) {
        val message = resources.getString(
            if (attending) R.string.detail_message_attendance_cancelled else R.string.detail_message_attendance_confirmed
        )
        request({ toggleAttendanceUseCase(event) }) { RequestResult.Success(message) }
    }

    /**
     * Acción rápida en línea: marca Loading antes de empezar y, al terminar, guarda el éxito o el fallo
     * (sin conexión, sin permiso…) y muestra su mensaje. Mientras una sigue en curso se ignoran las demás.
     */
    private fun <T> request(block: suspend () -> T, onSuccess: (T) -> RequestResult) {
        if (localState.value.isBusy) return
        viewModelScope.launch {
            localState.update { it.copy(actionResult = RequestResult.Loading) }
            val result = runRequest(resources, onSuccess = onSuccess, block = block)
            localState.update {
                when (result) {
                    is RequestResult.Success, is RequestResult.Failure ->
                        it.copy(actionResult = result, message = result.messageOrNull() ?: it.message)
                    RequestResult.Loading -> it
                }
            }
        }
    }

    private fun updateReport(transform: (ReportDraft) -> ReportDraft) {
        localState.update { state -> state.report?.let { state.copy(report = transform(it)) } ?: state }
    }

    private fun sendReport(event: Event) {
        val draft = localState.value.report ?: return
        if (draft.isSending) return
        val reason = draft.reason
        if (reason == null) {
            updateReport { it.copy(error = Validators.reportReason(resources, null), focusRequested = true) }
            return
        }
        viewModelScope.launch {
            updateReport { it.copy(sendResult = RequestResult.Loading) }
            // Toda respuesta del caso de uso cierra la hoja; solo un fallo de la petición la deja abierta para reintentar
            val result = runRequest(resources, onSuccess = { sent: SubmitReportResult ->
                RequestResult.Success(
                    when (sent) {
                        SubmitReportResult.SENT -> resources.getString(R.string.detail_message_report_sent, event.title)
                        SubmitReportResult.ALREADY_REPORTED -> resources.getString(R.string.detail_message_already_reported)
                        SubmitReportResult.OWN_EVENT -> resources.getString(R.string.detail_message_report_own_event)
                        SubmitReportResult.NOT_SIGNED_IN -> resources.getString(R.string.detail_message_report_sign_in)
                        SubmitReportResult.INVALID_REASON -> resources.getString(R.string.form_report_reason_required)
                    }
                )
            }) { submitReportUseCase(event, reason, draft.detail) }

            localState.update {
                when (result) {
                    is RequestResult.Success -> it.copy(report = null, message = result.message)
                    is RequestResult.Failure ->
                        it.copy(report = it.report?.copy(sendResult = result), message = result.errorMessage)
                    RequestResult.Loading -> it
                }
            }
        }
    }

    private fun sendComment(event: Event) {
        val draft = localState.value.draft
        if (draft.isSending) return
        val text = draft.text.trim()
        if (Validators.comment(resources, text) != null) {
            localState.update {
                it.copy(
                    draft = it.draft.copy(submitAttempted = true, focusRequested = true),
                    message = resources.getString(R.string.form_submit_invalid)
                )
            }
            return
        }

        val rating = draft.rating.takeIf { it > 0 }
        viewModelScope.launch {
            localState.update { it.copy(draft = it.draft.copy(sendResult = RequestResult.Loading)) }
            val result = runRequest(resources, onSuccess = { published: Boolean ->
                when {
                    !published -> RequestResult.Failure(resources.getString(R.string.detail_message_comment_failed))
                    rating != null -> RequestResult.Success(resources.getString(R.string.detail_message_comment_posted_rated, rating))
                    else -> RequestResult.Success(resources.getString(R.string.detail_message_comment_posted))
                }
            }) { addCommentUseCase(event = event, text = text, rating = rating) }

            // Si falla, el borrador se conserva para reintentar
            localState.update {
                when (result) {
                    is RequestResult.Success -> it.copy(draft = CommentDraft(), message = result.message)
                    is RequestResult.Failure -> it.copy(draft = it.draft.copy(sendResult = result), message = result.errorMessage)
                    RequestResult.Loading -> it
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
            name = if (isCurrentUser) resources.getString(R.string.common_you) else name,
            initials = this.name.initials(),
            neighborhood = neighborhood,
            isCurrentUser = isCurrentUser,
            photo = if (isCurrentUser) currentUser?.photo else photo
        )
    }
}
