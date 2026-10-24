package edu.uniquindio.co.tribooo.core.util

/** Puntaje máximo del medidor de seguridad de la contraseña. */
const val MAX_PASSWORD_SCORE = 4

private val STRENGTH_LABELS = listOf("Seguridad", "Débil", "Aceptable", "Buena", "Excelente")
private val STRENGTH_FRACTIONS = listOf(0f, 0.34f, 0.58f, 0.8f, 1f)

/**
 * Puntaje de 0 a 4: suma 1 por cada criterio (8 o más caracteres, 12 o más, mayúscula y minúscula,
 * dígito, símbolo) con tope en [MAX_PASSWORD_SCORE]. Es solo informativo.
 */
fun passwordScore(password: String): Int {
    val criteria = listOf(
        password.length >= 8,
        password.length >= 12,
        password.any { it.isUpperCase() } && password.any { it.isLowerCase() },
        password.any { it.isDigit() },
        password.any { !it.isLetterOrDigit() }
    )
    return criteria.count { it }.coerceAtMost(MAX_PASSWORD_SCORE)
}

/** Etiqueta del medidor para [score]: "Seguridad" (vacío), "Débil", "Aceptable", "Buena" o "Excelente". */
fun passwordStrengthLabel(score: Int): String = STRENGTH_LABELS[score.coerceIn(0, MAX_PASSWORD_SCORE)]

/** Fracción del ancho de la barra para [score]: 0, 0.34, 0.58, 0.8 o 1. */
fun passwordStrengthFraction(score: Int): Float = STRENGTH_FRACTIONS[score.coerceIn(0, MAX_PASSWORD_SCORE)]
