package edu.uniquindio.co.tribooo.navigation

import edu.uniquindio.co.tribooo.features.createevent.EventFormMode
import kotlinx.serialization.Serializable

sealed class MainRoutes {

    @Serializable
    data object Onboarding : MainRoutes()

    @Serializable
    data object Login : MainRoutes()

    @Serializable
    data object Register : MainRoutes()

    @Serializable
    data object PasswordRecovery : MainRoutes()

    @Serializable
    data object Feed : MainRoutes()

    @Serializable
    data object Profile : MainRoutes()

    @Serializable
    data object Explore : MainRoutes()

    @Serializable
    data object Agenda : MainRoutes()

    @Serializable
    data object CreateEvent : MainRoutes()

    /** Crear evento sobre uno existente: editarlo, reenviarlo tras un rechazo o duplicarlo. */
    @Serializable
    data class EditEvent(val eventId: String, val mode: EventFormMode) : MainRoutes()

    @Serializable
    data class EventDetail(val eventId: String, val commentId: String? = null) : MainRoutes()

    @Serializable
    data object Notifications : MainRoutes()

    @Serializable
    data object MyEvents : MainRoutes()

    @Serializable
    data object Saved : MainRoutes()

    @Serializable
    data object Followers : MainRoutes()

    @Serializable
    data class UserProfile(val userId: String) : MainRoutes()

    @Serializable
    data class EventStatus(val eventId: String) : MainRoutes()

    /** [tab] es el nombre de un valor de `ModerationTab`. */
    @Serializable
    data class Moderation(val tab: String = "PENDIENTES") : MainRoutes()

}
