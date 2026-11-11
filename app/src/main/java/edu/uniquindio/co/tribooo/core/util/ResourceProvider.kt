package edu.uniquindio.co.tribooo.core.util

import android.content.Context
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

    override fun getString(@StringRes id: Int, vararg args: Any): String = context.getString(id, *args)

    override fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg args: Any): String =
        context.resources.getQuantityString(id, quantity, *args)

    override fun getStringArray(@ArrayRes id: Int): List<String> = context.resources.getStringArray(id).toList()
}

/** [ResourceProvider] para composables; se recrea si cambia el contexto (p. ej. la configuración de idioma). */
@Composable
fun rememberResourceProvider(): ResourceProvider {
    val context = LocalContext.current
    return remember(context) { ResourceProviderImpl(context) }
}
