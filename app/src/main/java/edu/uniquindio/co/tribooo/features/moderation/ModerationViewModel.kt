package edu.uniquindio.co.tribooo.features.moderation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.rejectedMessage
import edu.uniquindio.co.tribooo.core.util.resolvedMessage
import edu.uniquindio.co.tribooo.core.util.restoredMessage
import edu.uniquindio.co.tribooo.core.util.verifiedMessage
import edu.uniquindio.co.tribooo.domain.model.ModerationCase
import edu.uniquindio.co.tribooo.domain.model.ModerationTab
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.usecase.auth.ObserveCurrentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.moderation.ModerationCounts
import edu.uniquindio.co.tribooo.domain.usecase.moderation.ObserveModerationCasesUseCase
import edu.uniquindio.co.tribooo.domain.usecase.moderation.ObserveModerationCountsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.moderation.RejectEventUseCase
import edu.uniquindio.co.tribooo.domain.usecase.moderation.ResolveReportsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.moderation.RestoreEventUseCase
import edu.uniquindio.co.tribooo.domain.usecase.moderation.VerifyEventUseCase
import edu.uniquindio.co.tribooo.navigation.MainRoutes
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    val error: String? = null
)

data class ModerationUiState(
    val tab: ModerationTab = ModerationTab.PENDIENTES,
    val isAdmin: Boolean = false,
    val counts: ModerationCounts = ModerationCounts(),
    val cases: List<ModerationCase> = emptyList(),
    val rejectSheet: RejectSheetState? = null,
    val message: String? = null
)

/** Parte del estado que no viene de los repositorios. */
private data class ModerationLocal(
    val tab: ModerationTab,
    val rejectSheet: RejectSheetState? = null,
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
    private val resolveReports: ResolveReportsUseCase
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
            message = local.message
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ModerationUiState(tab = local.value.tab))

    fun onTabSelected(tab: ModerationTab) = local.update { it.copy(tab = tab) }

    fun onVerify(case: ModerationCase) = act(verifiedMessage(case.event.title)) { verifyEvent(case.event.id) }

    fun onRestore(case: ModerationCase) = act(restoredMessage(case.event.title)) { restoreEvent(case.event.id) }

    fun onResolve(case: ModerationCase) = act(resolvedMessage(case.event.title)) { resolveReports(case.event.id) }

    /** Ejecuta la acción y muestra [successMessage] o, si falló, el motivo. */
    private fun act(successMessage: String, action: suspend () -> RequestResult) {
        viewModelScope.launch {
            val message = when (val result = action()) {
                is RequestResult.Failure -> result.errorMessage
                else -> successMessage
            }
            local.update { it.copy(message = message) }
        }
    }

    // Hoja de rechazo

    fun onRejectClick(case: ModerationCase) {
        local.update {
            it.copy(rejectSheet = RejectSheetState(case.event.id, case.event.title, case.event.organizer.name))
        }
    }

    fun onReasonChange(value: String) =
        local.update { state -> state.copy(rejectSheet = state.rejectSheet?.copy(reason = value, error = null)) }

    fun onDismissRejectSheet() = local.update { it.copy(rejectSheet = null) }

    fun onConfirmReject() {
        val sheet = local.value.rejectSheet ?: return
        viewModelScope.launch {
            when (val result = rejectEvent(sheet.eventId, sheet.reason)) {
                // La hoja sigue abierta con el motivo del fallo
                is RequestResult.Failure -> local.update {
                    it.copy(rejectSheet = it.rejectSheet?.copy(error = result.errorMessage))
                }
                else -> local.update {
                    it.copy(rejectSheet = null, tab = ModerationTab.RECHAZADAS, message = rejectedMessage(sheet.title))
                }
            }
        }
    }

    fun onMessageShown() = local.update { it.copy(message = null) }
}
