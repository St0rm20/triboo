package edu.uniquindio.co.tribooo.core.util

import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.domain.model.User

/** "1 persona sigue tus eventos" o "3 personas siguen tus eventos" */
fun followersSummary(rp: ResourceProvider, count: Int): String =
    rp.getQuantityString(R.plurals.followers_summary, count, count)

/** "Granada · te sigue y lo sigues" o "Granada · desde marzo 2025" */
fun followerSubtitle(rp: ResourceProvider, user: User, followedByMe: Boolean): String =
    rp.getString(
        R.string.common_dot_join,
        user.neighborhood,
        if (followedByMe) {
            rp.getString(R.string.followers_mutual)
        } else {
            rp.getString(R.string.followers_since, user.dateRegistered.monthYearLabel(rp))
        }
    )
