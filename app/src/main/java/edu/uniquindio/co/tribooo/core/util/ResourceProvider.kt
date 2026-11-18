package edu.uniquindio.co.tribooo.core.util

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.annotation.ArrayRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

/** Acceso a los textos de `res/values*` desde código sin composable (ViewModels, casos de uso, utilidades). */
interface ResourceProvider {
    fun getString(@StringRes id: Int, vararg args: Any): String

    fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg args: Any): String

    fun getStringArray(@ArrayRes id: Int): List<String>
}

class ResourceProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ResourceProvider {

    // En API 28-32 AppCompat solo cambia el idioma de la Activity, no el del contexto de la aplicación:
    // se resuelve en cada llamada con el idioma vigente para que los textos sigan el cambio
    private val localizedContext: Context
        get() {
            val locales = AppCompatDelegate.getApplicationLocales()
            if (locales.isEmpty) return context
            val configuration = Configuration(context.resources.configuration).apply {
                setLocales(LocaleList.forLanguageTags(locales.toLanguageTags()))
            }
            return context.createConfigurationContext(configuration)
        }

    override fun getString(@StringRes id: Int, vararg args: Any): String = localizedContext.getString(id, *args)

    override fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg args: Any): String =
        localizedContext.resources.getQuantityString(id, quantity, *args)

    override fun getStringArray(@ArrayRes id: Int): List<String> = localizedContext.resources.getStringArray(id).toList()
}

/** [ResourceProvider] para composables; se recrea si cambia el contexto (p. ej. la configuración de idioma). */
@Composable
fun rememberResourceProvider(): ResourceProvider {
    val context = LocalContext.current
    return remember(context) { ResourceProviderImpl(context) }
}
