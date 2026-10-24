package edu.uniquindio.co.tribooo.navigation

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
    data object CreateEvent : MainRoutes()

    @Serializable
    data class EventDetail(val eventId: String, val commentId: String? = null) : MainRoutes()

    @Serializable
    data object Notifications : MainRoutes()

    @Serializable
    data object MyEvents : MainRoutes()

    @Serializable
    data object Saved : MainRoutes()

}
