package edu.uniquindio.co.tribooo.features.followers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.core.util.followerSubtitle
import edu.uniquindio.co.tribooo.core.util.followersSummary
import edu.uniquindio.co.tribooo.core.util.initials
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.usecase.user.MyFollower
import edu.uniquindio.co.tribooo.domain.usecase.user.ObserveMyFollowersUseCase
import edu.uniquindio.co.tribooo.domain.usecase.user.ToggleFollowUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Seguidor listo para mostrarse en una tarjeta. */
data class FollowerItem(
    val user: User,
    val initials: String,
    val subtitle: String,
    val isFollowing: Boolean
)

data class FollowersUiState(
    val summary: String = followersSummary(0),
    val followers: List<FollowerItem> = emptyList(),
    val message: String? = null
)

internal fun MyFollower.toItem() = FollowerItem(
    user = user,
    initials = user.name.initials(),
    subtitle = followerSubtitle(user, followedByMe),
    isFollowing = followedByMe
)

@HiltViewModel
class FollowersViewModel @Inject constructor(
    observeMyFollowers: ObserveMyFollowersUseCase,
    private val toggleFollowUseCase: ToggleFollowUseCase
) : ViewModel() {

    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<FollowersUiState> = combine(observeMyFollowers(), message) { followers, message ->
        FollowersUiState(
            summary = followersSummary(followers.size),
            followers = followers.map { it.toItem() },
            message = message
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FollowersUiState())

    /** Sigue o deja de seguir a la persona; sigue en la lista porque continúa siendo seguidor. */
    fun onToggleFollow(item: FollowerItem) {
        viewModelScope.launch { toggleFollowUseCase(item.user) }
        message.value = if (item.isFollowing) {
            "Dejaste de seguir a ${item.user.name}."
        } else {
            "Ahora sigues a ${item.user.name} · verás sus eventos en tu feed."
        }
    }

    fun onMessageShown() = message.update { null }
}
