package edu.uniquindio.co.tribooo.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.core.graphics.toColorInt
import edu.uniquindio.co.tribooo.domain.model.Category

/** Par de colores (fondo, contenido) usado en chips e insignias de categoría. */
data class CategoryColors(val container: Color, val content: Color)

// Tonos del prototipo por categoría: claro (fondo, contenido) y oscuro (fondo, contenido)
private val categoryTones = mapOf(
    "deportes" to listOf(0xFFCAF0D3, 0xFF074A24, 0xFF1A3E26, 0xFFB0E7BE),
    "cultura" to listOf(0xFFFAD8F8, 0xFF562A55, 0xFF462A45, 0xFFF5C5F3),
    "academico" to listOf(0xFFD5E4FF, 0xFF29396C, 0xFF283454, 0xFFC1D6FF),
    "voluntariado" to listOf(0xFFFFDDC0, 0xFF5D3000, 0xFF4B2E11, 0xFFFFCCA1),
    "social" to listOf(0xFFFFD8D0, 0xFF642721, 0xFF502824, 0xFFFFC4BA)
)

@Composable
fun categoryColors(category: Category, darkTheme: Boolean = isSystemInDarkTheme()): CategoryColors {
    val tones = categoryTones[category.key]
    if (tones != null) {
        return if (darkTheme) {
            CategoryColors(Color(tones[2]), Color(tones[3]))
        } else {
            CategoryColors(Color(tones[0]), Color(tones[1]))
        }
    }
    // Categoría sin tono definido: se deriva del color guardado en el modelo
    val base = Color(category.color.toColorInt())
    return if (darkTheme) {
        CategoryColors(lerp(base, Color.Black, 0.6f), lerp(base, Color.White, 0.75f))
    } else {
        CategoryColors(lerp(base, Color.White, 0.82f), base)
    }
}
