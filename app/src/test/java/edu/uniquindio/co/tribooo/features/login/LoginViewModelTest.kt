package edu.uniquindio.co.tribooo.features.login

import edu.uniquindio.co.tribooo.core.util.FormTexts
import edu.uniquindio.co.tribooo.core.util.MessageTarget
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.SnackbarMessenger
import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.model.UserSession
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import edu.uniquindio.co.tribooo.domain.usecase.auth.LoginUseCase
import edu.uniquindio.co.tribooo.domain.usecase.auth.LoginWithGoogleUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
class LoginViewModelTest {

    private class FakeSession : SessionRepository {
        val current = MutableStateFlow<UserSession?>(null)
        override val session: Flow<UserSession?> = current
        override suspend fun saveSession(userId: String, role: UserRole) {
            current.value = UserSession(userId, role)
        }
        override suspend fun clearSession() {
            current.value = null
        }
    }

    private val messenger = SnackbarMessenger()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): LoginViewModel {
        val users = UserRepositoryImpl()
        val session = FakeSession()
        return LoginViewModel(LoginUseCase(users, session), LoginWithGoogleUseCase(users, session), messenger)
    }

    @Test
    fun openingTheFormShowsNoErrors() {
        val state = viewModel().uiState.value
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.message)
    }

    @Test
    fun typingInOneFieldDoesNotRevealTheOthers() {
        val vm = viewModel()
        vm.onEmailChange("ana")
        assertNull(vm.uiState.value.emailError)
        assertNull(vm.uiState.value.passwordError)
    }

    @Test
    fun touchedAndEmptyFieldShowsItsError() {
        val vm = viewModel()
        vm.onFieldFocusLost(LoginField.EMAIL)

        assertEquals(FormTexts.EMAIL_REQUIRED, vm.uiState.value.emailError)
        assertNull(vm.uiState.value.passwordError)
    }

    @Test
    fun emptyFormSubmitShowsAllErrorsAtOnceAndTheGeneralNotice() {
        val vm = viewModel()

        vm.login()

        val state = vm.uiState.value
        assertEquals(FormTexts.EMAIL_REQUIRED, state.emailError)
        assertEquals(FormTexts.PASSWORD_REQUIRED, state.passwordError)
        assertEquals("Revisa los campos marcados.", state.message)
        assertEquals(LoginField.EMAIL, state.focusField)
        assertNull(state.loginResult)
    }

    @Test
    fun focusGoesToTheFirstInvalidField() {
        val vm = viewModel()
        vm.onEmailChange("ana@mail.co")

        vm.login()

        assertEquals(LoginField.PASSWORD, vm.uiState.value.focusField)
        vm.onFocusHandled()
        assertNull(vm.uiState.value.focusField)
    }

    @Test
    fun fixingOneFieldClearsOnlyItsError() {
        val vm = viewModel()
        vm.login()

        vm.onEmailChange("ana@mail.co")

        assertNull(vm.uiState.value.emailError)
        assertEquals(FormTexts.PASSWORD_REQUIRED, vm.uiState.value.passwordError)
    }

    @Test
    fun successfulLoginPostsTheNoticeForTheFeedOnce() = runTest {
        val vm = viewModel()
        vm.onEmailChange("daniela.restrepo@triboo.co")
        vm.onPasswordChange("123456")

        vm.login()

        assertTrue(vm.uiState.value.loginResult is RequestResult.Success)
        assertEquals(FormTexts.LOGIN_SUCCESS, messenger.messages(MessageTarget.FEED).first())
    }

    @Test
    fun wrongCredentialsFailWithoutPostingANotice() = runTest {
        val vm = viewModel()
        vm.onEmailChange("daniela.restrepo@triboo.co")
        vm.onPasswordChange("incorrecta")

        vm.login()

        assertEquals(RequestResult.Failure("Email o contraseña incorrectos"), vm.uiState.value.loginResult)
        assertFalse(vm.uiState.value.message != null)
    }
}
