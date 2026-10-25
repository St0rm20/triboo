package edu.uniquindio.co.tribooo.features.register

import edu.uniquindio.co.tribooo.core.util.FormTexts
import edu.uniquindio.co.tribooo.core.util.MessageTarget
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.SnackbarMessenger
import edu.uniquindio.co.tribooo.data.repository.memory.LocationRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import edu.uniquindio.co.tribooo.domain.usecase.auth.RegisterUseCase
import edu.uniquindio.co.tribooo.domain.usecase.location.GetCitiesUseCase
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private val locations = LocationRepositoryImpl()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val messenger = SnackbarMessenger()

    private fun viewModel() =
        RegisterViewModel(GetCitiesUseCase(locations), RegisterUseCase(UserRepositoryImpl()), messenger)

    @Test
    fun successfulRegistrationLeavesTheFormBlankWithTheFirstCity() = runTest {
        val vm = viewModel()
        vm.onNameChange("Ana Ruiz")
        vm.onCityChange(locations.cities.last())
        vm.onAddressChange("Calle 1")
        vm.onEmailChange("ana@mail.co")
        vm.onPasswordChange("Secreto1")
        vm.onConfirmPasswordChange("Secreto1")
        vm.onTermsChange(true)
        vm.onRegisterClick()
        vm.onConfirmRegister()
        assertTrue(vm.uiState.value.registerResult is RequestResult.Success)

        vm.resetForm()

        assertEquals(RegisterUiState(city = locations.cities.first()), vm.uiState.value)
    }

    private fun RegisterViewModel.fillValidForm() {
        onNameChange("Ana Ruiz")
        onAddressChange("Calle 1")
        onEmailChange("ana@mail.co")
        onPasswordChange("Secreto1")
        onConfirmPasswordChange("Secreto1")
    }

    @Test
    fun registrationIsBlockedUntilTheTermsAreAccepted() = runTest {
        val vm = viewModel()
        vm.fillValidForm()

        assertFalse(vm.uiState.value.isFormValid)
        // Sin tocar los términos todavía no se muestra el error; al intentar enviar sí
        assertNull(vm.uiState.value.termsError)
        vm.onRegisterClick()
        assertEquals("Debes aceptar los términos para continuar", vm.uiState.value.termsError)
        assertEquals(RegisterField.TERMS, vm.uiState.value.focusField)
        assertFalse(vm.uiState.value.showConfirmDialog)

        vm.onTermsChange(true)

        assertTrue(vm.uiState.value.isFormValid)
        assertNull(vm.uiState.value.termsError)
        vm.onRegisterClick()
        assertTrue(vm.uiState.value.showConfirmDialog)
    }

    @Test
    fun unacceptingTheTermsBlocksAgain() = runTest {
        val vm = viewModel()
        vm.fillValidForm()
        vm.onTermsChange(true)

        vm.onTermsChange(false)

        assertFalse(vm.uiState.value.isFormValid)
        assertNotNull(vm.uiState.value.termsError)
    }

    @Test
    fun resetFormClearsTheTerms() = runTest {
        val vm = viewModel()
        vm.onTermsChange(true)

        vm.resetForm()

        assertFalse(vm.uiState.value.termsAccepted)
        assertNull(vm.uiState.value.termsError)
    }

    @Test
    fun openingTheFormShowsNoErrors() {
        val state = viewModel().uiState.value
        assertNull(state.nameError)
        assertNull(state.addressError)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.confirmPasswordError)
        assertNull(state.termsError)
        assertNull(state.message)
    }

    @Test
    fun emptyFormSubmitShowsAllErrorsAtOnceAndTheGeneralNotice() {
        val vm = viewModel()

        vm.onRegisterClick()

        val state = vm.uiState.value
        assertEquals(FormTexts.NAME_REQUIRED, state.nameError)
        assertNull(state.cityError)
        assertEquals(FormTexts.ADDRESS_REQUIRED, state.addressError)
        assertEquals(FormTexts.EMAIL_REQUIRED, state.emailError)
        assertEquals(FormTexts.PASSWORD_REQUIRED, state.passwordError)
        assertEquals(FormTexts.CONFIRM_PASSWORD_REQUIRED, state.confirmPasswordError)
        assertEquals(FormTexts.TERMS_REQUIRED, state.termsError)
        assertEquals("Revisa los campos marcados.", state.message)
        assertEquals(RegisterField.NAME, state.focusField)
        assertFalse(state.showConfirmDialog)
    }

    @Test
    fun touchedEmptyFieldShowsItsErrorAndUntouchedOnesDoNot() {
        val vm = viewModel()

        vm.onFieldFocusLost(RegisterField.NAME)

        assertEquals(FormTexts.NAME_REQUIRED, vm.uiState.value.nameError)
        assertNull(vm.uiState.value.emailError)
        assertNull(vm.uiState.value.passwordError)
    }

    @Test
    fun typingInAFieldDoesNotRevealTheOthersAndFixingClearsOnlyItsError() {
        val vm = viewModel()
        vm.onEmailChange("ana")
        assertNull(vm.uiState.value.nameError)
        assertNull(vm.uiState.value.emailError)

        vm.onRegisterClick()
        assertEquals(FormTexts.EMAIL_INVALID, vm.uiState.value.emailError)

        vm.onEmailChange("ana@mail.co")

        assertNull(vm.uiState.value.emailError)
        assertEquals(FormTexts.NAME_REQUIRED, vm.uiState.value.nameError)
    }

    @Test
    fun shortNameAndPasswordKeepTheirThresholds() {
        val vm = viewModel()
        vm.onNameChange("Al")
        vm.onPasswordChange("Abc123")
        vm.onRegisterClick()

        assertEquals(FormTexts.NAME_TOO_SHORT, vm.uiState.value.nameError)
        assertEquals(FormTexts.NEW_PASSWORD_TOO_SHORT, vm.uiState.value.passwordError)
    }

    @Test
    fun newPasswordNeedsAnUppercaseLetter() {
        val vm = viewModel()
        vm.onPasswordChange("secreto12")
        vm.onRegisterClick()
        assertEquals(FormTexts.NEW_PASSWORD_NEEDS_UPPERCASE, vm.uiState.value.passwordError)

        vm.onPasswordChange("Secreto12")

        assertNull(vm.uiState.value.passwordError)
    }

    @Test
    fun passwordMatchGivesPositiveFeedbackAndMismatchGivesAnError() {
        val vm = viewModel()
        vm.onPasswordChange("Secreto1")
        vm.onConfirmPasswordChange("Secreto2")
        vm.onFieldFocusLost(RegisterField.CONFIRM_PASSWORD)
        assertFalse(vm.uiState.value.passwordsMatch)
        assertEquals(FormTexts.PASSWORDS_DIFFER, vm.uiState.value.confirmPasswordError)

        vm.onConfirmPasswordChange("Secreto1")

        assertTrue(vm.uiState.value.passwordsMatch)
        assertNull(vm.uiState.value.confirmPasswordError)
    }

    @Test
    fun validFormOpensTheConfirmDialogWithoutErrors() {
        val vm = viewModel()
        vm.fillValidForm()
        vm.onTermsChange(true)

        vm.onRegisterClick()

        assertTrue(vm.uiState.value.showConfirmDialog)
        assertNull(vm.uiState.value.message)
    }

    @Test
    fun successfulRegistrationLeavesTheAccountCreatedNoticeForLoginOnce() = runTest {
        val vm = viewModel()
        vm.fillValidForm()
        vm.onTermsChange(true)
        vm.onRegisterClick()
        vm.onConfirmRegister()

        assertEquals("Cuenta creada. Inicia sesión.", messenger.messages(MessageTarget.LOGIN).first())
    }
}
