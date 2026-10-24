package edu.uniquindio.co.tribooo.core.util

import edu.uniquindio.co.tribooo.domain.model.User

/** "1 persona sigue tus eventos" o "3 personas siguen tus eventos" */
fun followersSummary(count: Int): String =
    if (count == 1) "1 persona sigue tus eventos" else "$count personas siguen tus eventos"

/** "Granada · te sigue y lo sigues" o "Granada · desde marzo 2025" */
fun followerSubtitle(user: User, followedByMe: Boolean): String =
    "${user.neighborhood} · " + if (followedByMe) "te sigue y lo sigues" else "desde ${user.dateRegistered.monthYearLabel()}"
