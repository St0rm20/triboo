package edu.uniquindio.co.tribooo.core.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SnackbarMessengerTest {

    @Test
    fun aMessagePostedBeforeTheScreenExistsIsDeliveredOnce() = runTest {
        val messenger = SnackbarMessenger()
        messenger.post(MessageTarget.FEED, "Inicio de sesión exitoso")

        assertEquals("Inicio de sesión exitoso", messenger.messages(MessageTarget.FEED).first())

        // Una segunda pantalla que se suscribe después no lo recibe de nuevo
        val again = mutableListOf<String>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { messenger.messages(MessageTarget.FEED).collect { again += it } }
        assertEquals(emptyList<String>(), again)
        job.cancel()
    }

    @Test
    fun otherTargetsDoNotConsumeTheMessage() = runTest {
        val messenger = SnackbarMessenger()
        val login = mutableListOf<String>()
        val loginJob = launch(UnconfinedTestDispatcher(testScheduler)) { messenger.messages(MessageTarget.LOGIN).collect { login += it } }

        messenger.post(MessageTarget.FEED, "para el feed")

        assertEquals(emptyList<String>(), login)
        assertEquals("para el feed", messenger.messages(MessageTarget.FEED).first())
        loginJob.cancel()
    }

    @Test
    fun liveCollectorReceivesMessagesInOrderWithoutRepeats() = runTest {
        val messenger = SnackbarMessenger()
        val received = mutableListOf<String>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { messenger.messages(MessageTarget.LOGIN).collect { received += it } }

        messenger.post(MessageTarget.LOGIN, "Cuenta creada. Inicia sesión.")
        messenger.post(MessageTarget.LOGIN, "Revisa tu correo")

        assertEquals(listOf("Cuenta creada. Inicia sesión.", "Revisa tu correo"), received)
        job.cancel()
    }
}
