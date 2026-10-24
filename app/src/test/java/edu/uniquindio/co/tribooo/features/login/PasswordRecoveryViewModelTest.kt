package edu.uniquindio.co.tribooo.features.login

import edu.uniquindio.co.tribooo.core.util.FormTexts
import edu.uniquindio.co.tribooo.core.util.MessageTarget
import edu.uniquindio.co.tribooo.core.util.SnackbarMessenger
import edu.uniquindio.co.tribooo.domain.usecase.auth.RequestPasswordRecoveryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PasswordRecoveryViewModelTest {

    private val messenger = SnackbarMessenger()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = PasswordRecoveryViewModel(RequestPasswordRecoveryUseCase(), messenger)

    @Test
    fun emptyEmailShowsTheErrorAndDoesNotAdvance() {
        val vm = viewModel()

        vm.onSend()

        val state = vm.uiState.value
        assertEquals(FormTexts.EMAIL_REQUIRED, state.emailError)
        assertEquals(FormTexts.SUBMIT_INVALID, state.message)
        assertTrue(state.focusEmail)
        assertNull(state.sentTo)
        assertFalse(state.isSending)
    }

    @Test
    fun invalidEmailUsesTheLoginMessage() {
        val vm = viewModel()
        vm.onEmailChange("ana@")

        vm.onSend()

        assertEquals(FormTexts.EMAIL_INVALID, vm.uiState.value.emailError)
        assertNull(vm.uiState.value.sentTo)
    }

    @Test
    fun untouchedFieldShowsNoErrorButTouchedAndEmptyDoes() {
        val vm = viewModel()
        assertNull(vm.uiState.value.emailError)

        vm.onEmailFocusLost()

        assertEquals(FormTexts.EMAIL_REQUIRED, vm.uiState.value.emailError)
    }

    @Test
    fun validEmailShowsSendingThenTheConfirmationWithThatEmail() = runTest {
        val vm = viewModel()
        vm.onEmailChange("ana@mail.co")

        vm.onSend()
        assertTrue(vm.uiState.value.isSending)
        assertNull(vm.uiState.value.sentTo)

        testScheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isSending)
        assertEquals("ana@mail.co", state.sentTo)
        assertEquals("Código enviado a ana@mail.co", state.message)
    }

    @Test
    fun secondTapWhileSendingIsIgnored() = runTest {
        val vm = viewModel()
        vm.onEmailChange("ana@mail.co")

        vm.onSend()
        vm.onSend()
        testScheduler.advanceUntilIdle()

        assertEquals("ana@mail.co", vm.uiState.value.sentTo)
    }

    @Test
    fun goingBackToLoginLeavesAReminderForThatScreenOnce() = runTest {
        val vm = viewModel()
        vm.onEmailChange("ana@mail.co")
        vm.onSend()
        testScheduler.advanceUntilIdle()

        vm.onBackToLogin()

        assertEquals(FormTexts.recoveryReminder("ana@mail.co"), messenger.messages(MessageTarget.LOGIN).first())
    }

    @Test
    fun useAnotherEmailReturnsToTheFormKeepingTheText() = runTest {
        val vm = viewModel()
        vm.onEmailChange("ana@mail.co")
        vm.onSend()
        testScheduler.advanceUntilIdle()

        vm.onUseAnotherEmail()

        assertNull(vm.uiState.value.sentTo)
        assertEquals("ana@mail.co", vm.uiState.value.email)
        assertNull(vm.uiState.value.emailError)
    }
}
