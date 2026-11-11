package edu.uniquindio.co.tribooo.domain.usecase.user

import androidx.annotation.StringRes
import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.Follow
import edu.uniquindio.co.tribooo.domain.model.User

/** Insignia que se gana al cumplir una condición de participación en la comunidad. */
enum class Badge(@StringRes val label: Int, @StringRes val description: Int) {
    FIRST_EVENT(R.string.badge_first_event, R.string.badge_first_event_desc),
    VERIFIED_HOST(R.string.badge_verified_host, R.string.badge_verified_host_desc),
    FREQUENT_ATTENDEE(R.string.badge_frequent_attendee, R.string.badge_frequent_attendee_desc),
    WELL_RATED(R.string.badge_well_rated, R.string.badge_well_rated_desc),
    POPULAR(R.string.badge_popular, R.string.badge_popular_desc)
}

/** Puntos e insignias de una persona. */
data class Reputation(val points: Int, val badges: List<Badge>)

/** Calcula la reputación a partir de los datos existentes; no guarda nada. */
object ReputationCalculator {
    const val POINTS_PER_EVENT = 10
    const val POINTS_PER_VERIFIED_EVENT = 5
    const val POINTS_PER_ATTENDANCE = 2
    const val POINTS_PER_GOOD_REVIEW = 3
    const val POINTS_PER_FOLLOWER = 1

    const val GOOD_REVIEW_MIN_RATING = 4
    const val FREQUENT_ATTENDEE_MIN = 10
    const val WELL_RATED_MIN_AVERAGE = 4.5
    const val WELL_RATED_MIN_REVIEWS = 3
    const val POPULAR_MIN_FOLLOWERS = 20

    /**
     * Reputación de [user]. Cuentan sus eventos activos o finalizados, sus asistencias a eventos de otras personas,
     * las reseñas (comentarios raíz con calificación, sin contar las propias) de sus eventos y sus seguidores.
     */
    fun calculate(
        user: User,
        events: List<Event>,
        attendances: List<Attendance>,
        comments: List<Comment>,
        follows: List<Follow>
    ): Reputation {
        val ownEvents = events.filter {
            it.organizer.id == user.id && (it.status == EventStatus.ACTIVO || it.status == EventStatus.FINALIZADO)
        }
        val verifiedEvents = ownEvents.count { it.verified }
        val othersAttendances = attendances.count { it.user.id == user.id && it.event.organizer.id != user.id }
        val ratings = comments
            .filter { it.parentComment == null && it.event.organizer.id == user.id && it.author.id != user.id }
            .mapNotNull { it.rating }
        val goodReviews = ratings.count { it >= GOOD_REVIEW_MIN_RATING }
        val followers = follows.count { it.followed.id == user.id }

        val points = ownEvents.size * POINTS_PER_EVENT +
            verifiedEvents * POINTS_PER_VERIFIED_EVENT +
            othersAttendances * POINTS_PER_ATTENDANCE +
            goodReviews * POINTS_PER_GOOD_REVIEW +
            followers * POINTS_PER_FOLLOWER

        val badges = buildList {
            if (ownEvents.isNotEmpty()) add(Badge.FIRST_EVENT)
            if (verifiedEvents >= 1) add(Badge.VERIFIED_HOST)
            if (othersAttendances >= FREQUENT_ATTENDEE_MIN) add(Badge.FREQUENT_ATTENDEE)
            if (ratings.size >= WELL_RATED_MIN_REVIEWS && ratings.average() >= WELL_RATED_MIN_AVERAGE) add(Badge.WELL_RATED)
            if (followers >= POPULAR_MIN_FOLLOWERS) add(Badge.POPULAR)
        }
        return Reputation(points, badges)
    }
}
