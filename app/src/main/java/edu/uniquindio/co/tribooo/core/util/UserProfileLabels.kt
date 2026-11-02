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

/** Administrador; con 10 o más eventos creados, líder; de 1 a 9, organizador; si no, vecino activo. */
fun communityRole(role: UserRole, createdEvents: Int): CommunityRole = when {
    role == UserRole.ADMIN -> CommunityRole.ADMIN
    createdEvents >= 10 -> CommunityRole.LEADER
    createdEvents >= 1 -> CommunityRole.ORGANIZER
    else -> CommunityRole.NEIGHBOR
}

/** "—" si no hay ninguno. */
fun countOrDash(count: Int): String = if (count == 0) "—" else count.formatThousands()

/** "4.7" o "—" si aún no hay calificaciones. */
fun ratingOrDash(rating: Double?): String = rating?.let { String.format(Locale.US, "%.1f", it) } ?: "—"
