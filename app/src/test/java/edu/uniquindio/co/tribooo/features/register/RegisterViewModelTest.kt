package edu.uniquindio.co.tribooo.features.register

import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.data.repository.memory.LocationRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import edu.uniquindio.co.tribooo.domain.usecase.auth.RegisterUseCase
import edu.uniquindio.co.tribooo.domain.usecase.location.GetCitiesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
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

    private fun viewModel() = RegisterViewModel(GetCitiesUseCase(locations), RegisterUseCase(UserRepositoryImpl()))

    @Test
    fun successfulRegistrationLeavesTheFormBlankWithTheFirstCity() = runTest {
        val vm = viewModel()
        vm.onNameChange("Ana Ruiz")
        vm.onCityChange(locations.cities.last())
        vm.onAddressChange("Calle 1")
        vm.onEmailChange("ana@mail.co")
        vm.onPasswordChange("secreto1")
        vm.onConfirmPasswordChange("secreto1")
        vm.onRegisterClick()
        vm.onConfirmRegister()
        assertTrue(vm.uiState.value.registerResult is RequestResult.Success)

        vm.resetForm()

        assertEquals(RegisterUiState(city = locations.cities.first()), vm.uiState.value)
    }
}
