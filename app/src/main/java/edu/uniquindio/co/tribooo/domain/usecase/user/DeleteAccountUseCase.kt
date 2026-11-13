package edu.uniquindio.co.tribooo.domain.usecase.user

import edu.uniquindio.co.tribooo.domain.model.AuthException
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.repository.AttendanceRepository
import edu.uniquindio.co.tribooo.domain.repository.AuthRepository
import edu.uniquindio.co.tribooo.domain.repository.CommentRepository
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.FollowRepository
import edu.uniquindio.co.tribooo.domain.repository.SavedEventRepository
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
import edu.uniquindio.co.tribooo.domain.repository.WaitlistRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.GetCurrentUserUseCase
import jakarta.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val attendanceRepository: AttendanceRepository,
    private val savedEventRepository: SavedEventRepository,
    private val waitlistRepository: WaitlistRepository,
    private val followRepository: FollowRepository,
    private val commentRepository: CommentRepository,
    private val sessionRepository: SessionRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val authRepository: AuthRepository
) {
    /**
     * Elimina la cuenta del usuario actual con todo lo que le pertenece (incluidos sus eventos, sin importar su estado)
     * y cierra la sesión. Retorna false, sin borrar nada, si no hay sesión, si es administrador o si la sesión no es
     * reciente (Firebase Authentication lo exige para eliminar la cuenta).
     *
     * Los datos se borran antes que la cuenta: las reglas de Firestore exigen sesión, y eliminar la cuenta la cierra.
     */
    suspend operator fun invoke(): Boolean {
        val user = getCurrentUser() ?: return false
        if (user.role == UserRole.ADMIN) return false

        // Antes de borrar nada: si Firebase no va a dejar eliminar la cuenta, el usuario puede reintentar tras iniciar sesión
        try {
            authRepository.ensureRecentLogin()
        } catch (e: AuthException) {
            return false
        }

        val ownEventIds = eventRepository.events.value.filter { it.organizer.id == user.id }.map { it.id }.toSet()

        attendanceRepository.attendances.value
            .filter { it.user.id == user.id || it.event.id in ownEventIds }
            .forEach { attendanceRepository.delete(it.user.id, it.event.id) }
        savedEventRepository.saved.value
            .filter { it.user.id == user.id || it.event.id in ownEventIds }
            .forEach { savedEventRepository.delete(it.user.id, it.event.id) }
        waitlistRepository.entries.value
            .filter { it.user.id == user.id || it.event.id in ownEventIds }
            .forEach { waitlistRepository.delete(it.user.id, it.event.id) }
        followRepository.follows.value
            .filter { it.follower.id == user.id || it.followed.id == user.id }
            .forEach { followRepository.delete(it.follower.id, it.followed.id) }

        // Sus comentarios, los de sus eventos y las respuestas que cuelgan de los suyos
        commentRepository.comments.value
            .filter { it.author.id == user.id || it.event.id in ownEventIds || it.parentComment?.author?.id == user.id }
            .forEach { commentRepository.delete(it.id) }

        ownEventIds.forEach { eventRepository.delete(it) }

        userRepository.delete(user.id)

        // Al final la cuenta, porque cierra la sesión que necesitan los borrados anteriores
        try {
            authRepository.deleteCurrentAccount()
        } catch (e: AuthException) {
            return false
        }
        sessionRepository.clearSession()
        return true
    }
}
