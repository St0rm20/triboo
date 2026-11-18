package edu.uniquindio.co.tribooo.navigation

import androidx.lifecycle.viewModelScope
import edu.uniquindio.co.tribooo.domain.model.AppLanguage
import edu.uniquindio.co.tribooo.domain.usecase.event.currentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.settings.FakeLanguageRepository
import edu.uniquindio.co.tribooo.domain.usecase.settings.ObserveAppLanguageUseCase
import edu.uniquindio.co.tribooo.domain.usecase.settings.SetAppLanguageUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    private val created = mutableListOf<AppViewModel>()

    @After
    fun tearDown() {
        // Su carga de la sesión sigue en curso: se cancela antes de soltar el dispatcher principal
        created.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }

    private fun viewModel(repository: FakeLanguageRepository) = AppViewModel(
        getCurrentUser = currentUserUseCase(null),
        observeAppLanguage = ObserveAppLanguageUseCase(repository),
        setAppLanguage = SetAppLanguageUseCase(repository)
    ).also(created::add)

    @Test
    fun exposesTheCurrentLanguage() {
        assertEquals(AppLanguage.ENGLISH, viewModel(FakeLanguageRepository(AppLanguage.ENGLISH)).language.value)
    }

    @Test
    fun setLanguageChangesTheExposedLanguage() {
        val vm = viewModel(FakeLanguageRepository())

        vm.setLanguage(AppLanguage.SPANISH)
        assertEquals(AppLanguage.SPANISH, vm.language.value)

        vm.setLanguage(AppLanguage.SYSTEM)
        assertEquals(AppLanguage.SYSTEM, vm.language.value)
    }

    @Test
    fun changingTheLanguageKeepsTheDarkTheme() {
        val vm = viewModel(FakeLanguageRepository())

        vm.setDarkTheme(true)
        vm.setLanguage(AppLanguage.ENGLISH)

        assertEquals(true, vm.darkTheme.value)
    }
}
