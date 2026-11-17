package edu.uniquindio.co.tribooo.features.moderation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.core.util.FormLimits
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.ResourceProvider
import edu.uniquindio.co.tribooo.core.util.Validators
import edu.uniquindio.co.tribooo.core.util.rejectedMessage
import edu.uniquindio.co.tribooo.core.util.resolvedMessage
import edu.uniquindio.co.tribooo.core.util.restoredMessage
import edu.uniquindio.co.tribooo.core.util.runRequest
import edu.uniquindio.co.tribooo.core.util.verifiedMessage
import edu.uniquindio.co.tribooo.domain.model.ModerationCase
import edu.uniquindio.co.tribooo.domain.model.ModerationTab
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.model.VerdictRecommendation
import edu.uniquindio.co.tribooo.domain.usecase.auth.ObserveCurrentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.moderation.ModerationCounts
import edu.uniquindio.co.tribooo.domain.usecase.moderation.ObserveModerationCasesUseCase
import edu.uniquindio.co.tribooo.domain.usecase.moderation.ObserveModerationCountsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.moderation.RejectEventUseCase
import edu.uniquindio.co.tribooo.domain.usecase.moderation.ResolveReportsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.moderation.RestoreEventUseCase
import edu.uniquindio.co.tribooo.domain.usecase.moderation.SuggestEventVerdictUseCase
import edu.uniquindio.co.tribooo.domain.usecase.moderation.SummarizeReportsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.moderation.VerifyEventUseCase
import edu.uniquindio.co.tribooo.domain.usecase.moderation.eventFingerprint
import edu.uniquindio.co.tribooo.domain.usecase.moderation.reportsFingerprint
import edu.uniquindio.co.tribooo.navigation.MainRoutes
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Hoja "Motivo del rechazo": la publicación que se rechaza, el texto escrito y su error. */
data class RejectSheetState(
    val eventId: String,
    val title: String,
    val authorName: String,
    val reason: String = "",
    val touched: Boolean = false,
    val submitAttempted: Boolean = false,
    /** Envío del rechazo; Loading mientras se procesa. */
    val submitResult: RequestResult? = null,
    /** El campo debe recibir el foco tras un envío inválido. */
    val focusRequested: Boolean = false,
    /** Motivo por el que moderación no pudo rechazar la publicación. */
    val failure: String? = null
) {
    val isSubmitting: Boolean get() = submitResult is RequestResult.Loading

    fun error(resources: ResourceProvider): String? =
        Validators.rejectReason(resources, reason).takeIf { touched || submitAttempted } ?: failure
}

/** Ayuda del asistente en la tarjeta: veredicto (Pendientes) o resumen de reportes (Reportes). */
sealed interface AssistantCardState {
    /** En cola o esperando al modelo. */
    data object Loading : AssistantCardState

    /** Sin ayuda: [message] es el motivo del fallo, o null si la generación automática está en pausa. */
    data class Unavailable(val message: String?) : AssistantCardState

    data class Verdict(val recommendation: VerdictRecommendation?, val explanation: String) : AssistantCardState

    data class Summary(val text: String) : AssistantCardState
}

/** Petición al asistente para un evento; [signature] identifica el contenido que se le envió. */
private data class AssistantRequest(val signature: String, val result: RequestResult)

/** Contenido que evalúa el asistente en la pestaña: cambia si el evento se edita o le llegan reportes. Null si la pestaña no usa el asistente. */
private fun assistantSignature(case: ModerationCase, tab: ModerationTab): String? = when (tab) {
    ModerationTab.PENDIENTES -> "verdict:" + eventFingerprint(case.event)
    ModerationTab.REPORTES -> "reports:" + reportsFingerprint(case.openReports)
    ModerationTab.RECHAZADAS, ModerationTab.RESUELTAS -> null
}

/** Estado de la ayuda del asistente para [case]; null si la pestaña no la muestra. */
private fun assistantCardState(case: ModerationCase, tab: ModerationTab, request: AssistantRequest?, paused: Boolean): AssistantCardState? {
    val signature = assistantSignature(case, tab) ?: return null
    case.verdict?.takeIf { tab == ModerationTab.PENDIENTES }?.let { return AssistantCardState.Verdict(it.recommendation, it.explanation) }
    case.reportSummary?.takeIf { tab == ModerationTab.REPORTES }?.let { return AssistantCardState.Summary(it.text) }
    val result = request?.takeIf { it.signature == signature }?.result
    return when {
        result is RequestResult.Failure -> AssistantCardState.Unavailable(result.errorMessage)
        // Success: ya se guardó y falta que llegue por el flujo
        result != null -> AssistantCardState.Loading
        paused -> AssistantCardState.Unavailable(null)
        else -> AssistantCardState.Loading
    }
}

data class ModerationUiState(
    val tab: ModerationTab = ModerationTab.PENDIENTES,
    val isAdmin: Boolean = false,
    val counts: ModerationCounts = ModerationCounts(),
    val cases: List<ModerationCase> = emptyList(),
    val rejectSheet: RejectSheetState? = null,
    /** Resultado de la última acción sobre un caso (verificar, restaurar, resolver). */
    val actionResult: RequestResult? = null,
    /** Evento cuya acción sigue en curso; sus botones esperan la respuesta. */
    val busyEventId: String? = null,
    /** Ayuda del asistente por id de evento, para las tarjetas de la pestaña actual. */
    val assistant: Map<String, AssistantCardState> = emptyMap(),
    val message: String? = null
)

/** Parte del estado que no viene de los repositorios. */
private data class ModerationLocal(
    val tab: ModerationTab,
    val rejectSheet: RejectSheetState? = null,
    val actionResult: RequestResult? = null,
    val busyEventId: String? = null,
    /** Última petición al asistente por id de evento. */
    val assistantRequests: Map<String, AssistantRequest> = emptyMap(),
    /** Tras un fallo se deja de pedir ayuda automáticamente (p. ej. límite de uso) hasta que el moderador reintente. */
    val assistantPaused: Boolean = false,
    val message: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ModerationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeCounts: ObserveModerationCountsUseCase,
    observeCases: ObserveModerationCasesUseCase,
    private val verifyEvent: VerifyEventUseCase,
    private val rejectEvent: RejectEventUseCase,
    private val restoreEvent: RestoreEventUseCase,
    private val resolveReports: ResolveReportsUseCase,
    private val suggestEventVerdict: SuggestEventVerdictUseCase,
    private val summarizeReports: SummarizeReportsUseCase,
    private val resources: ResourceProvider
) : ViewModel() {

    private val local = MutableStateFlow(
        ModerationLocal(
            tab = runCatching { ModerationTab.valueOf(savedStateHandle.toRoute<MainRoutes.Moderation>().tab) }
                .getOrDefault(ModerationTab.PENDIENTES)
        )
    )

    private val cases = local.map { it.tab }.distinctUntilChanged().flatMapLatest { observeCases(it) }

    val uiState: StateFlow<ModerationUiState> = combine(
        observeCurrentUser(), observeCounts(), cases, local
    ) { user, counts, cases, local ->
        val isAdmin = user?.role == UserRole.ADMIN
        ModerationUiState(
            tab = local.tab,
            isAdmin = isAdmin,
            counts = counts,
            cases = if (isAdmin) cases else emptyList(),
            rejectSheet = local.rejectSheet,
            actionResult = local.actionResult,
            busyEventId = local.busyEventId,
            assistant = if (isAdmin) {
                cases.mapNotNull { case ->
                    assistantCardState(case, local.tab, local.assistantRequests[case.event.id], local.assistantPaused)
                        ?.let { case.event.id to it }
                }.toMap()
            } else {
                emptyMap()
            },
            message = local.message
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ModerationUiState(tab = local.value.tab))

    fun onTabSelected(tab: ModerationTab) = local.update { it.copy(tab = tab) }

    fun onVerify(case: ModerationCase) = act(case, verifiedMessage(resources, case.event.title)) { verifyEvent(case.event.id) }

    fun onRestore(case: ModerationCase) = act(case, restoredMessage(resources, case.event.title)) { restoreEvent(case.event.id) }

    fun onResolve(case: ModerationCase) = act(case, resolvedMessage(resources, case.event.title)) { resolveReports(case.event.id) }

    /** Marca Loading, ejecuta la acción y muestra [successMessage] o, si falló, el motivo. Una acción a la vez. */
    private fun act(case: ModerationCase, successMessage: String, action: suspend () -> RequestResult) {
        if (local.value.actionResult is RequestResult.Loading) return
        viewModelScope.launch {
            local.update { it.copy(actionResult = RequestResult.Loading, busyEventId = case.event.id) }
            val result = runRequest(resources, onSuccess = { result: RequestResult ->
                if (result is RequestResult.Failure) result else RequestResult.Success(successMessage)
            }, block = action)
            local.update {
                when (result) {
                    is RequestResult.Success -> it.copy(actionResult = result, busyEventId = null, message = result.message)
                    is RequestResult.Failure -> it.copy(actionResult = result, busyEventId = null, message = result.errorMessage)
                    RequestResult.Loading -> it
                }
            }
        }
    }

    // Asistente (OpenRouter)

    private var assistantJob: Job? = null

    /**
     * Pide, de a una y en orden, la ayuda del asistente para las tarjetas de la pestaña que aún no la tienen
     * vigente. Las que ya la tienen guardada no llaman al modelo. Se detiene con el primer fallo.
     */
    fun onCasesShown() {
        if (assistantJob?.isActive == true) return
        assistantJob = viewModelScope.launch {
            while (true) {
                val state = uiState.value
                if (!state.isAdmin || local.value.assistantPaused) break
                val next = state.cases.firstNotNullOfOrNull { case ->
                    val signature = assistantSignature(case, state.tab) ?: return@firstNotNullOfOrNull null
                    val needsHelp = state.assistant[case.event.id] is AssistantCardState.Loading
                    val alreadyAsked = local.value.assistantRequests[case.event.id]?.signature == signature
                    if (needsHelp && !alreadyAsked) Triple(case, state.tab, signature) else null
                } ?: break
                val (case, tab, signature) = next
                if (!askAssistant(case, tab, signature)) local.update { it.copy(assistantPaused = true) }
            }
        }
    }

    /** Vuelve a pedir la ayuda de [case] y reanuda la generación automática. */
    fun onRetryAssistant(case: ModerationCase) {
        local.update { it.copy(assistantRequests = it.assistantRequests - case.event.id, assistantPaused = false) }
        onCasesShown()
    }

    /** Marca Loading y espera al asistente; retorna false si falló. */
    private suspend fun askAssistant(case: ModerationCase, tab: ModerationTab, signature: String): Boolean {
        val eventId = case.event.id
        local.update { it.copy(assistantRequests = it.assistantRequests + (eventId to AssistantRequest(signature, RequestResult.Loading))) }
        val result = runRequest(resources, onSuccess = { result: RequestResult -> result }) {
            if (tab == ModerationTab.PENDIENTES) suggestEventVerdict(eventId) else summarizeReports(eventId)
        }
        local.update { it.copy(assistantRequests = it.assistantRequests + (eventId to AssistantRequest(signature, result))) }
        return result !is RequestResult.Failure
    }

    // Hoja de rechazo

    /** Si el asistente sugiere rechazar, su explicación llega como borrador del motivo (el moderador la puede editar). */
    fun onRejectClick(case: ModerationCase) {
        val draft = case.verdict?.takeIf { it.recommendation == VerdictRecommendation.RECHAZAR }?.explanation.orEmpty()
        local.update {
            it.copy(
                rejectSheet = RejectSheetState(
                    case.event.id, case.event.title, case.event.organizer.name,
                    reason = draft.take(FormLimits.REJECT_REASON)
                )
            )
        }
    }

    fun onReasonChange(value: String) = local.update { state ->
        state.copy(rejectSheet = state.rejectSheet?.copy(reason = value.take(FormLimits.REJECT_REASON), failure = null))
    }

    fun onReasonFocusLost() =
        local.update { state -> state.copy(rejectSheet = state.rejectSheet?.copy(touched = true)) }

    fun onReasonFocusHandled() =
        local.update { state -> state.copy(rejectSheet = state.rejectSheet?.copy(focusRequested = false)) }

    fun onDismissRejectSheet() = local.update { it.copy(rejectSheet = null) }

    fun onConfirmReject() {
        val sheet = local.value.rejectSheet ?: return
        if (sheet.isSubmitting) return
        if (Validators.rejectReason(resources, sheet.reason) != null) {
            local.update { it.copy(rejectSheet = it.rejectSheet?.copy(submitAttempted = true, focusRequested = true)) }
            return
        }
        viewModelScope.launch {
            local.update { it.copy(rejectSheet = it.rejectSheet?.copy(submitResult = RequestResult.Loading)) }
            val result = runRequest(resources, onSuccess = { result: RequestResult ->
                if (result is RequestResult.Failure) result else RequestResult.Success(rejectedMessage(resources, sheet.title))
            }) { rejectEvent(sheet.eventId, sheet.reason) }
            when (result) {
                // La hoja sigue abierta con el motivo del fallo
                is RequestResult.Failure -> local.update {
                    it.copy(rejectSheet = it.rejectSheet?.copy(submitResult = result, failure = result.errorMessage))
                }
                is RequestResult.Success -> local.update {
                    it.copy(rejectSheet = null, tab = ModerationTab.RECHAZADAS, message = result.message)
                }
                RequestResult.Loading -> Unit
            }
        }
    }

    fun onMessageShown() = local.update { it.copy(message = null) }
}
