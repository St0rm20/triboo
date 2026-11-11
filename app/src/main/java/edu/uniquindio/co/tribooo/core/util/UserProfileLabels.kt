package edu.uniquindio.co.tribooo.core.util

import androidx.annotation.StringRes
import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.domain.model.UserRole
import java.util.Locale

/** Etiqueta de rol que se muestra en el perfil de una persona. */
enum class CommunityRole(@StringRes val label: Int) {
    ADMIN(R.string.role_admin),
    LEADER(R.string.role_leader),
    ORGANIZER(R.string.role_organizer),
    NEIGHBOR(R.string.role_neighbor)
}

/** Administrador; con 150 o más puntos de reputación, líder; con 30 o más, organizador; si no, vecino activo. */
fun communityRole(role: UserRole, points: Int): CommunityRole = when {
    role == UserRole.ADMIN -> CommunityRole.ADMIN
    points >= 150 -> CommunityRole.LEADER
    points >= 30 -> CommunityRole.ORGANIZER
    else -> CommunityRole.NEIGHBOR
}

/** "—" si no hay ninguno. */
fun countOrDash(count: Int): String = if (count == 0) "—" else count.formatThousands()

/** "4.7" o "—" si aún no hay calificaciones. */
fun ratingOrDash(rating: Double?): String = rating?.let { String.format(Locale.US, "%.1f", it) } ?: "—"
