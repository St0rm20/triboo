package edu.uniquindio.co.tribooo.features.userprofile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.core.util.CommunityRole
import edu.uniquindio.co.tribooo.core.util.communityRole
import edu.uniquindio.co.tribooo.core.util.countOrDash
import edu.uniquindio.co.tribooo.core.util.ratingOrDash
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.Follow
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.usecase.attendance.ObserveAttendancesUseCase
import edu.uniquindio.co.tribooo.domain.usecase.auth.ObserveCurrentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.comment.ObserveCommentsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.ObserveEventsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.user.ObserveFollowsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.user.ObserveUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.user.ToggleFollowUseCase
import edu.uniquindio.co.tribooo.navigation.MainRoutes
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserProfileUiState(
    /** Mientras llegan los primeros datos de los repositorios. */
    val isLoading: Boolean = true,
    /** Null cuando el usuario ya no existe. */
    val user: User? = null,
    val isCurrentUser: Boolean = false,
    val isFollowing: Boolean = false,
    val role: CommunityRole = CommunityRole.NEIGHBOR,
    val followers: Int = 0,
    val attendances: Int = 0,
    val createdEvents: Int = 0,
    val rating: Double? = null,
    /** Eventos activos que organiza, del que ocurre primero al que ocurre después. */
    val events: List<Event> = emptyList(),
    val message: String? = null
) {
    val createdEventsLabel: String get() = countOrDash(createdEvents)
    val ratingLabel: String get() = ratingOrDash(rating)

    /** Seguir y reportar solo tienen sentido en el perfil de otra persona. */
    val canInteract: Boolean get() = user != null && !isCurrentUser
}

/** Datos de los repositorios que alimentan el perfil (se agrupan porque `combine` admite máximo 5 flujos). */
internal data class UserProfileSources(
    val events: List<Event>,
    val attendances: List<Attendance>,
    val follows: List<Follow>,
    val comments: List<Comment>
)

/** Arma el estado del perfil de [user] visto por [currentUser]. */
internal fun buildUserProfileState(
    user: User?,
    currentUser: User?,
    sources: UserProfileSources
): UserProfileUiState {
    if (user == null) return UserProfileUiState(isLoading = false)

    // Los eventos pendientes o rechazados no son públicos: no cuentan
    val publicEvents = sources.events.filter {
        it.organizer.id == user.id && (it.status == EventStatus.ACTIVO || it.status == EventStatus.FINALIZADO)
    }
    val publicIds = publicEvents.map { it.id }.toSet()
    val ratings = sources.comments
        .filter { it.event.id in publicIds && it.author.id != user.id }
        .mapNotNull { it.rating }

    return UserProfileUiState(
        isLoading = false,
        user = user,
        isCurrentUser = user.id == currentUser?.id,
        isFollowing = sources.follows.any { it.follower.id == currentUser?.id && it.followed.id == user.id },
        role = communityRole(user.role, publicEvents.size),
        followers = sources.follows.count { it.followed.id == user.id },
        attendances = sources.attendances.count { it.user.id == user.id },
        createdEvents = publicEvents.size,
        rating = if (ratings.isEmpty()) null else ratings.average(),
        events = publicEvents.filter { it.status == EventStatus.ACTIVO }.sortedBy { it.startDate }
    )
}

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeUser: ObserveUserUseCase,
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeEvents: ObserveEventsUseCase,
    observeAttendances: ObserveAttendancesUseCase,
    observeFollows: ObserveFollowsUseCase,
    observeComments: ObserveCommentsUseCase,
    private val toggleFollowUseCase: ToggleFollowUseCase
) : ViewModel() {

    private val userId = savedStateHandle.toRoute<MainRoutes.UserProfile>().userId

    private val message = MutableStateFlow<String?>(null)

    private val sources = combine(
        observeEvents(),
        observeAttendances(),
        observeFollows(),
        observeComments(),
        ::UserProfileSources
    )

    val uiState: StateFlow<UserProfileUiState> = combine(
        observeUser(userId),
        observeCurrentUser(),
        sources,
        message
    ) { user, currentUser, sources, message ->
        buildUserProfileState(user, currentUser, sources).copy(message = message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserProfileUiState())

    /** Sigue o deja de seguir a la persona; el número de seguidores cambia solo. */
    fun onToggleFollow() {
        val state = uiState.value
        val user = state.user ?: return
        if (!state.canInteract) return

        viewModelScope.launch { toggleFollowUseCase(user) }
        message.value = if (state.isFollowing) {
            "Dejaste de seguir a ${user.name}."
        } else {
            "Ahora sigues a ${user.name} · verás sus eventos en tu feed."
        }
    }

    fun onReport() {
        if (!uiState.value.canInteract) return
        message.value = "Perfil reportado a moderación."
    }

    fun onMessageShown() = message.update { null }
}
