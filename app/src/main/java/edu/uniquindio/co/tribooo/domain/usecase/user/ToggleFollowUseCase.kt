package edu.uniquindio.co.tribooo.domain.usecase.user

import edu.uniquindio.co.tribooo.domain.model.Follow
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.FollowRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject

class ToggleFollowUseCase @Inject constructor(
    private val followRepository: FollowRepository,
    private val getCurrentUser: GetCurrentUserUseCase
) {
    /** El usuario actual empieza o deja de seguir a [followed] (nadie puede seguirse a sí mismo). */
    suspend operator fun invoke(followed: User) {
        val follower = getCurrentUser() ?: return
        if (follower.id == followed.id) return

        val isFollowing = followRepository.follows.value.any {
            it.follower.id == follower.id && it.followed.id == followed.id
        }
        if (isFollowing) {
            followRepository.delete(followerId = follower.id, followedId = followed.id)
        } else {
            followRepository.save(Follow(follower = follower, followed = followed))
        }
    }
}
