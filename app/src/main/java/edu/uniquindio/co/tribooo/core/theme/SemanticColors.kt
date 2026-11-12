package edu.uniquindio.co.tribooo.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Colores de estado que no existen en el esquema Material 3. */
data class SemanticColors(val amber: Color, val green: Color)

@Composable
fun semanticColors(darkTheme: Boolean = isSystemInDarkTheme()): SemanticColors =
    if (darkTheme) SemanticColors(AmberDark, GreenDark) else SemanticColors(AmberLight, GreenLight)
