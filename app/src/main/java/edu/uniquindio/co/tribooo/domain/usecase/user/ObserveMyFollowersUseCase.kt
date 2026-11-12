package edu.uniquindio.co.tribooo.domain.usecase.user

import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.ObserveCurrentUserUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Persona que sigue al usuario actual; [followedByMe] indica si él también la sigue. */
data class MyFollower(val user: User, val followedByMe: Boolean)

class ObserveMyFollowersUseCase @Inject constructor(
    private val observeFollows: ObserveFollowsUseCase,
    private val userRepository: UserRepository,
    private val observeCurrentUser: ObserveCurrentUserUseCase
) {
    /**
     * Quienes siguen al usuario actual, en orden alfabético. Cada uno se resuelve contra la lista de
     * usuarios para mostrar sus datos vigentes (el seguimiento guarda una copia); si ya no existe, se omite.
     */
    operator fun invoke(): Flow<List<MyFollower>> =
        combine(observeFollows(), userRepository.users, observeCurrentUser()) { follows, users, me ->
            if (me == null) return@combine emptyList()
            val usersById = users.associateBy { it.id }
            val followedByMe = follows.filter { it.follower.id == me.id }.map { it.followed.id }.toSet()

            follows
                .filter { it.followed.id == me.id }
                .mapNotNull { usersById[it.follower.id] }
                .distinctBy { it.id }
                .map { MyFollower(user = it, followedByMe = it.id in followedByMe) }
                .sortedBy { it.user.name.lowercase() }
        }
}
